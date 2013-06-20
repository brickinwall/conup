package cn.edu.nju.moon.conup.trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeComponentReferenceImpl;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeComponentServiceImpl;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeEndpointImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.datamodel.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;


public class TracePolicyInterceptor implements PhasedInterceptor {

	public static final String tracePolicy = "TracePolicy";
	public static final QName policySetQName = new QName(TracePolicy.SCA11_NS, tracePolicy);
	private final static Logger LOGGER = Logger.getLogger(TracePolicyInterceptor.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	} 
	
	/** The identifier of root transaction in Message header. */
//	private static String rootIdentifier = "RootVcTransaction";
	/** The identifier of parent transaction in Message header. */
//	private static String parentIdentifier = "ParentVcTransaction";
	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
	private static String HOSTIDENTIFIER = "HostIdentifier";
	private static String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";
	/**
	 * it's used to identify a ended sub tx id in the response message
	 */
	private static final String ENDED_SUB_TX_TAG = "ENDED_SUB_TX_TAG";
	private static final String ROOT_TX = "ROOT_TX";
	private static final String ROOT_COMP = "ROOT_COMP";
	private static final String PARENT_TX = "PARENT_TX";
	private static final String PARENT_COMP = "PARENT_COMP";
	private static final String SUB_TX = "SUB_TX";
	private static final String SUB_COMP = "SUB_COMP";

	private Invoker next;
	private Operation operation;
	private List<TracePolicy> policies;
	private PolicySubject subject;
	private String context;
	private String phase;

	public TracePolicyInterceptor(PolicySubject subject, String context,
			Operation operation, List<TracePolicy> policies, String phase) {
		super();
		this.operation = operation;
		this.policies = policies;
		this.subject = subject;
		this.phase = phase;
		this.context = getContext();
		
		init();
	}

	/**
	 * This method will be called by both service interceptor and reference interceptor.
	 * When a message is passing through the service interceptor, it will add root and parent 
	 * transaction's id to the  InterceptorCache.
	 * 
	 * A InterceptorCache object is shared by service interceptor and reference interceptor, 
	 * which is used to store/exchange current transaction and its dependent transactions,i.e., root and parent transaction.
	 * 
	 * When a message is passing through the reference interceptor, it's supposed to modify the message's root and parent
	 * transaction id that is stored in InterceptorCache.
	 * 
	 * */
	public Message invoke(Message msg) {
		LOGGER.fine("operation =" + operation.toString());
		
//		if(isCallback(msg)){
//			LOGGER.fine(operation.toString() + " is a Callback operation when interceptor phase is " + phase);
//			return getNext().invoke(msg);
//		}
		if(phase.equals(Phase.SERVICE_POLICY) || phase.equals(Phase.REFERENCE_POLICY)){
			msg = trace(msg);
			msg = buffer(msg);
		}
		
		//test
//		String hostComponent = null;
//		if(getComponent() != null)
//			hostComponent = getComponent().getName();
		
		msg = getNext().invoke(msg);
		
		if(phase.equals(Phase.SERVICE_POLICY)){
			msg = attachEndedTxToResAtServicePolicy(msg);
		} else if(phase.equals(Phase.REFERENCE_POLICY)){
			msg = attachEndedTxToResAtRefernecePolicy(msg);
		}
		
		return msg;
	}
	
	/**
	 * This method is supposed to exchange root/parent transaction id via
	 * Message body.
	 */
	private Message trace(Message msg){
		//locate ROOT_PARENT_IDENTIFIER in message body
		List<Object> msgBodyOriginal;
		if(msg.getBody() == null){
			Object [] tmp = new Object[1];
			tmp[0] = (Object)"";
			msgBodyOriginal = Arrays.asList(tmp);
		}
		else
			msgBodyOriginal = Arrays.asList((Object [])msg.getBody());
		
		List<Object> msgBody = new ArrayList<Object>();
		msgBody.addAll(msgBodyOriginal);
		String transactionTag = null;
		for(Object object : msgBody){
			if(object.toString().contains(TracePolicyInterceptor.ROOT_PARENT_IDENTIFIER)){
				transactionTag = object.toString();
				msgBody.remove(object);
				break;
			}
		}
	
		if (phase.equals(Phase.SERVICE_POLICY)) {
			msg = traceServicePhase(msg, transactionTag, msgBody);
		} else if (phase.equals(Phase.REFERENCE_POLICY)) {
			msg = traceReferencePhase(msg, transactionTag, msgBody);
		}//else if(reference.policy)
		
//		if(phase.equals(Phase.REFERENCE_POLICY)
//				|| phase.equals(Phase.SERVICE_POLICY)){
//	
//			msgBodyOriginal = Arrays.asList((Object [])msg.getBody());
//			List<Object> copy = new ArrayList<Object>();
//			copy.addAll(msgBodyOriginal);
//			String msgBodyStr = new String();
//			msgBodyStr += "\t" + "Message body:";
//			for(Object object : copy){
//				String tmp = object.toString();
//				msgBodyStr += "\n\t\t" + tmp;
//			}
//			msgBodyStr += "\n";
//			LOGGER.fine(msgBodyStr);
//		}
		
		
		return msg;
	}

	private Message traceServicePhase(Message msg, String transactionTag, List<Object> msgBody) {
//			String currentTx = null;
			String hostComponent = null;
			String rootTx = null;
			String rootComponent = null;
			String parentTx = null;
			String parentComponent = null;
			String threadID = null;
			
			String subTx = null;
			String subComp = null;
			
			if(transactionTag==null || transactionTag.equals("")){
				//an exception is preferred
				LOGGER.warning("Error: message body cannot be null in a service body");
			}
			
			LOGGER.fine("trace SERVICE_POLICY : " + transactionTag);
			
			//get root, parent and current transaction id
			if(transactionTag != null){
				String target = getTargetString(transactionTag);
				String[] txInfos = target.split(","); 
				String rootInfo = txInfos[0];
				String parentInfo = txInfos[1];
				String subInfo = txInfos[2];
				
				String[] rootInfos = rootInfo.split(":");
				rootTx = rootInfos[0].equals("null") ? null : rootInfos[0];
				rootComponent = rootInfos[1].equals("null") ? null : rootInfos[1];
				
				String[] parentInfos = parentInfo.split(":");
				parentTx = parentInfos[0].equals("null") ? null : parentInfos[0];
				parentComponent = parentInfos[1].equals("null") ? null : parentInfos[1];
				
				String[] subInfos = subInfo.split(":");
				subTx = subInfos[0].equals("null") ? null : subInfos[0];
				subComp = subInfos[1].equals("null") ? null : subInfos[1];
				
//				String rootInfo = target.split(",")[0];
//				String parentInfo = target.split(",")[1];
//				String subInfo = target.split(",")[2];
//				rootTx = rootInfo.split(":")[0].equals("null") ? null
//						: rootInfo.split(":")[0];
//				rootComponent = rootInfo.split(":")[1].equals("null") ? null
//						: rootInfo.split(":")[1];
//				parentTx = parentInfo.split(":")[0].equals("null") ? null
//						: parentInfo.split(":")[0];
//				parentComponent = parentInfo.split(":")[1].equals("null") ? null
//						: parentInfo.split(":")[1];
//				subTx = subInfo.split(":")[0].equals("null") ? null
//						: subInfo.split(":")[0];
//				subComp = subInfo.split(":")[1].equals("null") ? null
//						: subInfo.split(":")[1];
			}
			
			Map<String, Object> headers = msg.getHeaders();
			if(rootTx!=null && rootComponent!=null){
				headers.put(ROOT_TX, rootTx);
				headers.put(ROOT_COMP, rootComponent);
				
				assert parentTx != null;
				assert parentComponent != null;
				assert subTx != null;
				assert subComp != null;
				
				headers.put(PARENT_TX, parentTx);
				headers.put(PARENT_COMP, parentComponent);
				
				headers.put(SUB_TX, subTx);
				headers.put(SUB_COMP, subComp);
			}
			
			//get host component name
			hostComponent = getComponent().getName();
	//		if(subComp != null)
	//			assert hostComponent.equals(subComp);
			
			// check interceptor cache
			InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
			threadID = getThreadID();
			TransactionContext txContext = cache.getTxCtx(threadID);
			if(txContext == null){
				// generate and init TransactionDependency
				txContext = new TransactionContext();
				txContext.setCurrentTx(null);
//				txContext.setHostComponent(hostComponent);
				txContext.setParentTx(parentTx);
				txContext.setParentComponent(parentComponent);
				txContext.setRootTx(rootTx);
				txContext.setRootComponent(rootComponent);
				//add to InterceptorCacheImpl
				cache.addTxCtx(threadID, txContext);
			} 
			txContext.setHostComponent(hostComponent);
//			else{
//				txContext.setHostComponent(hostComponent);
//			}
			
			String hostInfo = TracePolicyInterceptor.HOSTIDENTIFIER + "," + threadID + "," + hostComponent;
			msgBody.add(hostInfo);
			msg.setBody((Object [])msgBody.toArray());
			return msg;
		}

	private Message traceReferencePhase(Message msg, String transactionTag, List<Object> msgBody) {
		String currentTx = null;
		String hostComponent = null;
		String rootTx = null;
		String rootComponent = null;
		String parentTx = null;
		String parentComponent = null;
		String threadID = null;
		String subTx = null;
		String subComp = null;
		
		hostComponent = getComponent().getName();
		//get root and parent id from InterceptorCacheImpl
		InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
		threadID = getThreadID();
		TransactionContext txContext = cache.getTxCtx(threadID);
		if(txContext == null){	//the invoked transaction is a root transaction 
			currentTx = null;
			hostComponent = null;
			parentTx = null;
			parentComponent = null;
			rootTx = null;
			rootComponent = null;
			subTx = null;
			subComp = null;
		} else{
			rootTx = txContext.getRootTx();
			rootComponent = txContext.getRootComponent();
			currentTx = txContext.getCurrentTx();
			hostComponent = txContext.getHostComponent();
			parentTx = currentTx;
			parentComponent = hostComponent;
			
			TxDepMonitorImpl txDepMonitor = new TxDepMonitorImpl();
			subTx = new TxLifecycleManager().createFakeTxId();
			subComp = txDepMonitor.convertServiceToComponent(getTargetServiceName(), hostComponent);
			
			assert subComp != null;
			
			txDepMonitor.startRemoteSubTx(subComp, hostComponent, rootTx, parentTx, subTx);
		}//else(dependency != null)
		
		//generate transaction tag(identifier)
		String newRootParent;
		newRootParent = TracePolicyInterceptor.ROOT_PARENT_IDENTIFIER + 
				"[" + rootTx + ":" + rootComponent + 
				"," + parentTx + ":" + parentComponent + 
				"," + subTx + ":" + subComp +
				"]";
		StringBuffer buffer = new StringBuffer();
		buffer.append(newRootParent);
		msgBody.add(buffer.toString());
		msg.setBody((Object [])msgBody.toArray());
		
		LOGGER.fine("trace REFERENCE_POLICY : " + newRootParent);
		return msg;
	}

	private Message buffer(Message msg) {
		if (phase.equals(Phase.SERVICE_POLICY)) {
			String hostComp;
			hostComp = getComponent().getName();
			NodeManager nodeMgr = NodeManager.getInstance();
			DynamicDepManager depMgr = nodeMgr
					.getDynamicDepManager(hostComp);
			CompLifecycleManager clMgr;
			String threadID;
			InterceptorCache cache = InterceptorCache.getInstance(hostComp);
			threadID = getThreadID();
			TransactionContext txCtx = cache.getTxCtx(threadID);
			
			TxDepMonitor txDepMonitor = new TxDepMonitorImpl();
			Map<String, Object> msgHeaders = msg.getHeaders();

			// waiting during on-demand setup
			Object syncMonitor = depMgr.getOndemandSyncMonitor();
			Object subTx = msgHeaders.get(SUB_TX);
			synchronized (syncMonitor) {
				if (depMgr.isNormal()) {
					// the invoked transaction is not a root transaction
					if(txCtx.getRootTx() != null){
						assert txCtx.getParentTx() != null;
						assert txCtx.getParentComponent() != null;
						assert subTx != null;
						assert msgHeaders != null;
						assert txDepMonitor != null;
						
						txDepMonitor.initLocalSubTx(hostComp, subTx.toString(), 
								txCtx.getRootTx(), txCtx.getRootComponent(),
								txCtx.getParentTx(), txCtx.getParentComponent());
					}
					return msg;
				}
				
				try {
					if (depMgr.isOndemandSetting()) {
						LOGGER.fine("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier()+ "----------------ondemandSyncMonitor.wait()------------");
						syncMonitor.wait();
						LOGGER.fine("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier()+ "----------------finish ondemand------------");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			clMgr = CompLifecycleManager.getInstance(hostComp);
			String freenessConf = depMgr.getCompObject().getFreenessConf();
			FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
			assert freeness!= null;
			assert txCtx != null;
			
			// haven't received update request yet
			Object waitingRemoteCompUpdateDoneMonitor = depMgr.getWaitingRemoteCompUpdateDoneMonitor();
			synchronized (waitingRemoteCompUpdateDoneMonitor) {
				if (clMgr.getUpdateCtx() == null || clMgr.getUpdateCtx().isLoaded() == false) {
//					LOGGER.fine("ThreadID=" + getThreadID() + ", in buffer, haven't received update request yet");
					if( freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, false)){
						try {
							LOGGER.fine("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier() + " thread suspended to wait for remote update done--------------------------");
							waitingRemoteCompUpdateDoneMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// the invoked transaction is not a root transaction
					if(txCtx.getRootTx() != null){
						assert txCtx.getParentTx() != null;
						assert txCtx.getParentComponent() != null;
						assert subTx != null;
						txDepMonitor.initLocalSubTx(hostComp, subTx.toString(), 
								txCtx.getRootTx(), txCtx.getRootComponent(),
								txCtx.getParentTx(), txCtx.getParentComponent());
					}
					return msg;
				}
			}
			
			// try to be ready for update
			Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
			synchronized (validToFreeSyncMonitor) {
				if(depMgr.getCompStatus().equals(CompStatus.VALID)
					&& clMgr.getUpdateCtx() != null && clMgr.getUpdateCtx().isLoaded() ){
					// calculate old version root txs
					if (!clMgr.getUpdateCtx().isOldRootTxsInitiated()) {
						clMgr.initOldRootTxs();
//						Printer printer = new Printer();
//						printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
					}
					if (!freeness.isReadyForUpdate(hostComp)) {
//						LOGGER.fine("ThreadID=" + getThreadID()
//								+ "compStatus=" + depMgr.getCompStatus()
//								+ ", in buffer, try to be free via "
//								+ freeness.getFreenessType());
						Class<?> compClass = freeness.achieveFreeness(
								txCtx.getRootTx(), txCtx.getRootComponent(),
								txCtx.getParentComponent(),
								txCtx.getCurrentTx(), hostComp,
								UpdateFactory.createFreenessCallback(null));
						if (compClass != null) {
							addBufferMsgBody(msg, compClass);
						}
					}
					if (freeness.isReadyForUpdate(hostComp)) {
						depMgr.achievedFree();
					} else if (freeness.isInterceptRequiredForFree(
							txCtx.getRootTx(), hostComp, txCtx, true)) {
						LOGGER.fine("ThreadID=" + getThreadID()	+ "compStatus=" + depMgr.getCompStatus() + "----------------validToFreeSyncMonitor.wait();buffer------------root:" + txCtx.getRootTx() + ",parent:" + txCtx.getParentTx());
						try {
							validToFreeSyncMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
					}
				}
			}

			// if ready for update
			Object updatingSyncMonitor = depMgr.getUpdatingSyncMonitor();
			synchronized (updatingSyncMonitor) {
				if (depMgr.getCompStatus().equals(CompStatus.Free)) {
					LOGGER.fine("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + ", in buffer updatingSyncMonitor, is Free for update now, try to execute update...");
					clMgr.executeUpdate();
					clMgr.cleanupUpdate();
				}
				
			}
			
			// the invoked transaction is not a root transaction
			if(txCtx.getRootTx() != null){
				assert txCtx.getParentTx() != null;
				assert txCtx.getParentComponent() != null;
				assert subTx != null;
				txDepMonitor.initLocalSubTx(hostComp, subTx.toString(), 
						txCtx.getRootTx(), txCtx.getRootComponent(),
						txCtx.getParentTx(), txCtx.getParentComponent());
			}
		}// END IF(SERVICE_POLICY)
		return msg;
	}

	/**
	 * attach ended sub txs info to response message body at SERVICE_POLICY
	 * @param msg
	 * @return msg contains ended sub txs
	 */
	private Message attachEndedTxToResAtServicePolicy(Message msg) {
//			String currentTx = null;
			String hostComp = null;
			String rootTx = null;
			String rootComp = null;
			String parentTx = null;
			String parentComp = null;
//			String threadID = null;
			
			//locate ROOT_PARENT_IDENTIFIER in message body
			List<Object> msgBodyOriginal;
			// return value is void
			if (msg.getBody() == null) {
				msgBodyOriginal = new ArrayList<Object>();
			} else{
				msgBodyOriginal = Arrays.asList(msg.getBody());
			}
			
			List<Object> msgBody = new ArrayList<Object>();
			msgBody.addAll(msgBodyOriginal);
			
			if (phase.equals(Phase.SERVICE_POLICY)) {
				Map<String, Object> msgHeader = msg.getHeaders();
				String subTx = null;
				String subComp = null;
				
				//get host component name
				hostComp = getComponent().getName();
//				threadID = getThreadID();
				
				// current tx is a root tx, no need to attach any information to its response 
				if(msgHeader.get(SUB_TX) == null){
					return msg;
				}
				
				subTx = msgHeader.get(SUB_TX).toString();
				subComp = msgHeader.get(SUB_COMP).toString();
				rootTx = (String) msgHeader.get(ROOT_TX);
				rootComp = (String) msgHeader.get(ROOT_COMP);
				parentTx = (String) msgHeader.get(PARENT_TX);
				parentComp = (String) msgHeader.get(PARENT_COMP);
				
				assert subTx != null;
				if( !hostComp.equals(subComp) ){
					LOGGER.warning("hostComp: " + hostComp + " subComp: " + subComp);
					assert hostComp.equals(subComp);
				}
//				assert hostComp.equals(txCtx.getHostComponent());
				
				NodeManager nodeMgr = NodeManager.getInstance();
				DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
//				Printer printer = new Printer();
//				LOGGER.fine("TxS before removeFakeSubTx:");
//				printer.printTxs(LOGGER, depMgr.getTxs());
//				removeFakeSubTx(hostComp, subTx);
				depMgr.getTxDepMonitor().endLocalSubTx(hostComp, subTx);
//				LOGGER.fine("TxS after removeFakeSubTx:");
//				printer.printTxs(LOGGER, depMgr.getTxs());
				
				//generate info required to be attatched to the response msg body
				StringBuffer endedSubTxTag = new StringBuffer();
				endedSubTxTag.append(ENDED_SUB_TX_TAG);
				endedSubTxTag.append("[");
				endedSubTxTag.append(ROOT_TX + ":" + rootTx + ";");
				endedSubTxTag.append(ROOT_COMP + ":" + rootComp + ";");
				endedSubTxTag.append(PARENT_TX + ":" + parentTx + ";");
				endedSubTxTag.append(PARENT_COMP + ":" + parentComp + ";");
				endedSubTxTag.append(SUB_TX + ":" + subTx + ";");
				endedSubTxTag.append(SUB_COMP + ":" + subComp );
				endedSubTxTag.append("]");
				
				//reset the msg body
				msgBody.add(endedSubTxTag.toString());
				msg.setBody((Object [])msgBody.toArray());
				
				msgHeader.remove(ROOT_TX);
				msgHeader.remove(ROOT_COMP);
				msgHeader.remove(PARENT_TX);
				msgHeader.remove(PARENT_COMP);
				msgHeader.remove(SUB_TX);
				msgHeader.remove(SUB_COMP);
			} 
			return msg;
		}

	/**
	 * this method should be named as analysis the attached sub txs info from the returned message body
	 * @param msg
	 * @return msg
	 */
	private Message attachEndedTxToResAtRefernecePolicy(Message msg) {
		String currentTx = null;
		String hostComp = null;
		String rootTx = null;
		String subTx = null;
		String subComp = null;
		
		List<Object> msgBodyOriginal;
		if(msg.getBody() == null){
			Object [] tmp = new Object[1];
			tmp[0] = (Object)"";
			msgBodyOriginal = Arrays.asList(tmp);
		}
		else{
			if(msg.getBody().getClass().isArray())
				msgBodyOriginal = Arrays.asList((Object [])msg.getBody());
			else
				msgBodyOriginal = Arrays.asList(msg.getBody());
		}
		
		List<Object> msgBody = new ArrayList<Object>();
		msgBody.addAll(msgBodyOriginal);
		String subContextTag = null;
		for(Object object : msgBody){
			
			if(object instanceof String && object.toString().contains("ENDED_SUB_TX_TAG")){
				subContextTag = object.toString();
				msgBody.remove(object);
				break;
			}
		}
		if(subContextTag == null)
			return msg;
		
		LOGGER.fine("subContextTag:" + subContextTag + ", msgBody:" + msgBody);
		
		// Here we need to pay attention, the body in this return message should be only one object, not an array.
		// Because we have added ENDED_SUB_TX_TAG to the body in SERVICE.policy phase, and make the actual body become a list
		// If this service's return value is void, then msgBody.size can be 0. So we do not need to return anything
		if(msgBody.size() != 0)
			msg.setBody(msgBody.get(0));
		
		LOGGER.fine("attach REFERENCE_POLICY: " + subContextTag);
		
		Map<String, String> endedSubTxProperty = parseEndedSubTxTag(subContextTag);
		
		if(endedSubTxProperty.size() == 0){
			LOGGER.warning("invalid data in ENDED_SUB_TX_TAG");
		}
		
		rootTx = endedSubTxProperty.get(ROOT_TX);
		currentTx = endedSubTxProperty.get(PARENT_TX);
		hostComp = endedSubTxProperty.get(PARENT_COMP);
		subTx = endedSubTxProperty.get(SUB_TX);
		subComp = endedSubTxProperty.get(SUB_COMP);
		
		assert hostComp != null;
		assert hostComp.equals(getComponent().getName());
		
		if( !subComp.equals(hostComp)){
			
//			NodeManager nodeMgr = NodeManager.getInstance();
//			DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
//			Printer printer = new Printer();
			LOGGER.fine("TxS before endRemoteSubTx:");
//			printer.printTxs(LOGGER, depMgr.getTxs());
			
			TxDepMonitorImpl txDepMonitor = new TxDepMonitorImpl();
			txDepMonitor.endRemoteSubTx(subComp, hostComp, rootTx, currentTx, subTx);
			
			LOGGER.fine("TxS after endRemoteSubTx:");
//			printer.printTxs(LOGGER, depMgr.getTxs());
		}
		return msg;
	}

	private Map<String, String> parseEndedSubTxTag(String endedSubTxTag) {
		String subStrCtx = endedSubTxTag.substring(endedSubTxTag.indexOf("[")+1, endedSubTxTag.indexOf("]"));
		Map<String, String> result = new HashMap<String, String>();
		String [] splitted = subStrCtx.split(";");
		for(String str : splitted){
			String [] pair = str.split(":");
			result.put(pair[0], pair[1]);
		}
		return result;
	}

	private void addBufferMsgBody(Message msg, Class<?> compClass) {
		String className = compClass.getName();
		List<Object> originalMsgBody;
		List<Object> copyOfMsgBody = new ArrayList<Object>();
		originalMsgBody = Arrays.asList((Object [])msg.getBody());
		copyOfMsgBody.addAll(originalMsgBody);
		copyOfMsgBody.add(COMP_CLASS_OBJ_IDENTIFIER + ":" + className);
		copyOfMsgBody.add(compClass);
		msg.setBody((Object [])copyOfMsgBody.toArray());
	}


	
	private boolean isCallback(Message msg){
		boolean isCallback = false;
		Endpoint endpoint = msg.getTo();
//		LOGGER.fine(phase);
		if(endpoint instanceof RuntimeEndpointImpl
			&& phase.equals(Phase.SERVICE_POLICY)
			&& getComponent() != null
			&& getComponent().getReferences() != null){
			RuntimeEndpointImpl rtEp = ((RuntimeEndpointImpl) endpoint);
			String targetUri = rtEp.getDeployedURI();
			
//			if(targetUri.equals("http://114.212.81.182:12305/Car/SearchCallback")){
//				LOGGER.fine(phase + "  " + "http://114.212.81.182:12305/Car/SearchCallback");
//			}
			
			for (ComponentReference compRef : getComponent().getReferences()) {
				RuntimeComponentReferenceImpl rtCompRef = (RuntimeComponentReferenceImpl) compRef;
				if (rtCompRef.getCallback() == null
						|| rtCompRef.getCallback().getBindings() == null)
					continue;
				for (Binding binding : rtCompRef.getCallback().getBindings()) {
					if (targetUri.equals(binding.getURI())) {
						isCallback = true;
						break;
					}
				}
				if (isCallback) {
					break;
				}
			}
			
		} else if(phase.equals(Phase.REFERENCE_POLICY)
				&& getComponent() != null
				&& getComponent().getServices() != null){
			RuntimeEndpointImpl rtEp = ((RuntimeEndpointImpl) endpoint);
			String targetUri = rtEp.getDeployedURI();
			
//			if(targetUri.equals("http://114.212.81.182:12305/Car/SearchCallback")){
//				LOGGER.fine(phase + "  " + "http://114.212.81.182:12305/Car/SearchCallback");
//			}
			
			for (ComponentService compService : getComponent().getServices()) {
				RuntimeComponentServiceImpl rtCompService = (RuntimeComponentServiceImpl) compService;
				if (compService.getCallback() == null
						|| compService.getCallback().getBindings() == null) {
					continue;
				}
				for (Binding binding : compService.getCallback().getBindings()) {
					if (targetUri.equals(binding.getURI())) {
						isCallback = true;
						break;
					}
				}
				if (isCallback) {
					break;
				}
			}
			
		}
		return isCallback;
	}
	/**
	 * root and parent transaction id is stored in the format: VcTransactionRootAndParentIdentifier[ROOT_ID,PARENT_ID].
	 * 
	 * @return ROOT_ID,PARENT_ID
	 * 
	 * */
	private String getTargetString(String raw){
		if(raw == null){
			return null;
		}
		if(raw.startsWith("\"")){
			raw = raw.substring(1);
		}
		if(raw.endsWith("\"")){
			raw = raw.substring(0, raw.length()-1);
		}
		int index = raw.indexOf(TracePolicyInterceptor.ROOT_PARENT_IDENTIFIER);
		int head = raw.substring(index).indexOf("[")+1;
//		LOGGER.fine(raw.substring(0, head));
		int tail = raw.substring(index).indexOf("]");
//		LOGGER.fine(raw.substring(head, tail));
		return raw.substring(head, tail);
	}
	
	private String getTargetServiceName(){
		String serviceName = null;
		
		serviceName = operation.getInterface().toString();	
		return serviceName;
	}
	
	/** return current thread ID. */
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
	
	private Component getComponent(){
		if (subject instanceof Endpoint) {
			Endpoint endpoint = (Endpoint) subject;
			return endpoint.getComponent();
		} else if (subject instanceof EndpointReference) {
			EndpointReference endpointReference = (EndpointReference) subject;
			return endpointReference.getComponent();
		} else if (subject instanceof Component) {
			Component component = (Component) subject;
			return component;
		}
		return null;
		
	}

	private void init() {
	
	}

	private String getContext() {
		if (subject instanceof Endpoint) {
			Endpoint endpoint = (Endpoint) subject;
			return endpoint.getURI();
		} else if (subject instanceof EndpointReference) {
			EndpointReference endpointReference = (EndpointReference) subject;
			return endpointReference.getURI();
		} else if (subject instanceof Component) {
			Component component = (Component) subject;
			return component.getURI();
		} else if(subject instanceof Implementation){
			Implementation impl = (Implementation) subject;
			return impl.getURI();
		}
		return null;
	}

	public String getPhase() {
		return phase;
	}
	
	public Invoker getNext() {
		return next;
	}

	public void setNext(Invoker next) {
		this.next = next;
	}

	private class ResponseWrapper implements Serializable{
		private Object returnValue;
		private Object endedSubTxInfo;
		public ResponseWrapper(Object returnValue, Object endedSubTxInfo){
			this.returnValue = returnValue;
			this.endedSubTxInfo = endedSubTxInfo;
		}
		public Object getReturnValue() {
			return returnValue;
		}
		public void setReturnValue(Object returnValue) {
			this.returnValue = returnValue;
		}
		public Object getEndedSubTxInfo() {
			return endedSubTxInfo;
		}
		public void setEndedSubTxInfo(Object endedSubTxInfo) {
			this.endedSubTxInfo = endedSubTxInfo;
		}
		
	}
}