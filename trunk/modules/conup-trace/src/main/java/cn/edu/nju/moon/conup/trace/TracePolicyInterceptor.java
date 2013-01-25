package cn.edu.nju.moon.conup.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
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

	public void setNext(Invoker next) {
		this.next = next;
	}

	public Invoker getNext() {
		return next;
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
		
		if(isCallback(msg)){
			LOGGER.warning(operation.toString() + " is a Callback operation when interceptor phase is " + phase);
//			if(operation.toString().contains("searchResultsResponse")){
//				System.out.println();
//			}
			return getNext().invoke(msg);
		}
		
		if(phase.equals(Phase.SERVICE_POLICY) || phase.equals(Phase.REFERENCE_POLICY)){
			msg = exchangeViaMsgBody(msg);
			msg = buffer(msg);
		}
		
		return getNext().invoke(msg);
	}
	
	private boolean isCallback(Message msg){
		boolean isCallback = false;
		Endpoint endpoint = msg.getTo();
//		System.out.println(phase);
		if(endpoint instanceof RuntimeEndpointImpl
			&& phase.equals(Phase.SERVICE_POLICY)
			&& getComponent() != null
			&& getComponent().getReferences() != null){
			RuntimeEndpointImpl rtEp = ((RuntimeEndpointImpl) endpoint);
			String targetUri = rtEp.getDeployedURI();
			
//			if(targetUri.equals("http://114.212.81.182:12305/Car/SearchCallback")){
//				System.out.println(phase + "  " + "http://114.212.81.182:12305/Car/SearchCallback");
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
//				System.out.println(phase + "  " + "http://114.212.81.182:12305/Car/SearchCallback");
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

	private Message buffer(Message msg) {
		if (phase.equals(Phase.SERVICE_POLICY)) {
			String compIdentifier = getComponent().getName();
			NodeManager nodeMgr = NodeManager.getInstance();
			DynamicDepManager depMgr = nodeMgr
					.getDynamicDepManager(compIdentifier);
			CompLifecycleManager clMgr;
			clMgr = CompLifecycleManager.getInstance(compIdentifier);
			String hostComp;
			String threadID;
			hostComp = getComponent().getName();
			InterceptorCache cache = InterceptorCache.getInstance(hostComp);
			threadID = getThreadID();
			TransactionContext txCtx = cache.getTxCtx(threadID);
			
//			System.out.println("\n\n\n ThreadID=" + getThreadID() + ", in buffer, compStatus=" + depMgr.getCompStatus() + " \n\n\n");

			if (depMgr.isNormal()) {
				TxLifecycleManager.addRootTx(txCtx.getParentTx(), txCtx.getRootTx());
				return msg;
			}

			// waiting during on-demand setup
			Object syncMonitor = depMgr.getOndemandSyncMonitor();
			synchronized (syncMonitor) {
				try {
					if (depMgr.isOndemandSetting()) {
						LOGGER.warning("ThreadID=" + getThreadID() + "----------------syncMonitor.wait();buffer------------");
						syncMonitor.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			String freenessConf = depMgr.getCompObject().getFreenessConf();
			FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
			if(freeness == null){
				throw new NullPointerException("NullPointerException because freeness == null");
			}
			if(txCtx == null){
//				getNext().invoke(msg);
				throw new NullPointerException("NullPointerException because txCtx == null");
			}

			// haven't received update request yet
			Object waitingRemoteCompUpdateDoneMonitor = depMgr.getWaitingRemoteCompUpdateDoneMonitor();
			synchronized (waitingRemoteCompUpdateDoneMonitor) {
				if (clMgr.getUpdateCtx() == null || clMgr.getUpdateCtx().isLoaded() == false) {
//					System.out.println("ThreadID=" + getThreadID() + ", in buffer, haven't received update request yet");
					if( freeness.isInterceptRequiredForFree(txCtx.getRootTx(), compIdentifier, txCtx, false)){
						try {
							waitingRemoteCompUpdateDoneMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					TxLifecycleManager.addRootTx(txCtx.getParentTx(), txCtx.getRootTx());
					return msg;
				}
			}
			
			// try to be ready for update
			Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
			synchronized (validToFreeSyncMonitor) {
				if(depMgr.getCompStatus().equals(CompStatus.VALID)
					&& clMgr.getUpdateCtx() != null && clMgr.getUpdateCtx().isLoaded() ){
					// add root tx to
					TxLifecycleManager.addRootTx(txCtx.getParentTx(), txCtx.getRootTx());
					// calculate old version root txs
					if (!clMgr.getUpdateCtx().isOldRootTxsInitiated()) {
						clMgr.initOldRootTxs();
//						Printer printer = new Printer();
//						printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
					}
					if (!freeness.isReadyForUpdate(hostComp)) {
//						System.out.println("ThreadID=" + getThreadID()
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
							TxLifecycleManager.removeRootTx(txCtx.getParentTx(), txCtx.getRootTx());
							clMgr.removeBufferoldRootTxs(txCtx.getParentTx(), txCtx.getRootTx());
//							clMgr.getUpdateCtx().removeBufferOldRootTx(txCtx.getParentTx(), txCtx.getRootTx());
							validToFreeSyncMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						// clMgr.getUpdateCtx().getBufferOldRootTxs().add(txCtx.getRootTx());
					}
				}
			}

//			// if ready for update
			Object updatingSyncMonitor = depMgr.getUpdatingSyncMonitor();
			synchronized (updatingSyncMonitor) {
				if (depMgr.getCompStatus().equals(CompStatus.Free)) {
					LOGGER.warning("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + ", in buffer updatingSyncMonitor, is Free for update now, try to execute update...");
					clMgr.executeUpdate();
					clMgr.cleanupUpdate();
				}
				
				if (depMgr.isInterceptRequired()) {
					LOGGER.warning("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + "----------------updatingSyncMonitor.wait();buffer------------");
					try {
						updatingSyncMonitor.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			TxLifecycleManager.addRootTx(txCtx.getParentTx(), txCtx.getRootTx());
		}// END IF(SERVICE_POLICY)
		return msg;
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

	@Deprecated
	private Message exchangeViaMsgHeader(Message msg){
		if (phase.equals(Phase.SERVICE_POLICY)) {
			System.out.println("\n\n\nservice interceptor: ");
			System.out.println(msg.getHeaders().get("conup"));
		} else if(phase.equals(Phase.REFERENCE_POLICY)){
			System.out.println("\n\n\nreference interceptor: ");
			System.out.println(msg.getHeaders());
			msg.getHeaders().put("conup", "testing");
		} else if(phase.equals(Phase.SERVICE_INTERFACE)){
			System.out.println("\n\n\n " + Phase.SERVICE_INTERFACE);
			System.out.println(msg.getHeaders());
		} else if(phase.equals(Phase.IMPLEMENTATION_POLICY)){
			System.out.println("\n\n\n " + Phase.IMPLEMENTATION_POLICY);
			System.out.println(msg.getHeaders());
		} 
		
		return msg;
	}
	
	/**
	 * This method is supposed to exchange root/parent transaction id via
	 * Message body.
	 */
	public Message exchangeViaMsgBody(Message msg){
		String currentTx = null;
		String hostComponent = null;
		String rootTx = null;
		String rootComponent = null;
		String parentTx = null;
		String parentComponent = null;
		String threadID = null;
		
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
			
			if(transactionTag==null || transactionTag.equals("")){
				//an exception is preferred
				LOGGER.fine("Error: message body cannot be null in a service body");
			}
			
			//get root, parent and current transaction id
			if(transactionTag != null){
				String target = getTargetString(transactionTag);
				String rootInfo = target.split(",")[0];
				String parentInfo = target.split(",")[1];
				rootTx = rootInfo.split(":")[0].equals("null") ? null
						: rootInfo.split(":")[0];
				rootComponent = rootInfo.split(":")[1].equals("null") ? null
						: rootInfo.split(":")[1];
				parentTx = parentInfo.split(":")[0].equals("null") ? null
						: parentInfo.split(":")[0];
				parentComponent = parentInfo.split(":")[1].equals("null") ? null : parentInfo.split(":")[1];
			}
			
			//get host component name
			hostComponent = getComponent().getName();
			
			// check interceptor cache
			InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
			threadID = getThreadID();
			TransactionContext txContext = cache.getTxCtx(hostComponent);
			if(txContext == null){
				// generate and init TransactionDependency
				txContext = new TransactionContext();
				txContext.setCurrentTx(null);
				txContext.setHostComponent(hostComponent);
				txContext.setParentTx(parentTx);
				txContext.setParentComponent(parentComponent);
				txContext.setRootTx(rootTx);
				txContext.setRootComponent(rootComponent);
				//add to InterceptorCacheImpl
				cache.addTxCtx(threadID, txContext);
			} else{
				txContext.setHostComponent(hostComponent);
			}
			
			String hostInfo = TracePolicyInterceptor.HOSTIDENTIFIER + "," + threadID + "," + hostComponent;
			msgBody.add(hostInfo);
			msg.setBody((Object [])msgBody.toArray());
			
		} else if (phase.equals(Phase.REFERENCE_POLICY)) {
			
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
			} else{
				rootTx = txContext.getRootTx();
				rootComponent = txContext.getRootComponent();
				currentTx = txContext.getCurrentTx();
				hostComponent = txContext.getHostComponent();
				parentTx = currentTx;
				parentComponent = hostComponent;
			}//else(dependency != null)
			
			
			//generate transaction tag(identifier)
			String newRootParent;
			newRootParent = TracePolicyInterceptor.ROOT_PARENT_IDENTIFIER + 
					"[" + rootTx + ":" + rootComponent + 
					"," + parentTx + ":" + parentComponent + "]";
			StringBuffer buffer = new StringBuffer();
			buffer.append(newRootParent);
			msgBody.add(buffer.toString());
			msg.setBody((Object [])msgBody.toArray());
		}//else if(reference.policy)
		
		if(phase.equals(Phase.REFERENCE_POLICY)
				|| phase.equals(Phase.SERVICE_POLICY)){

			msgBodyOriginal = Arrays.asList((Object [])msg.getBody());
			List<Object> copy = new ArrayList<Object>();
			copy.addAll(msgBodyOriginal);
			String msgBodyStr = new String();
			msgBodyStr += "\t" + "Message body:";
			for(Object object : copy){
				String tmp = object.toString();
				msgBodyStr += "\n\t\t" + tmp;
			}
			msgBodyStr += "\n";
			LOGGER.fine(msgBodyStr);
		}
		
		
		return msg;
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
//		System.out.println(raw.substring(0, head));
		int tail = raw.substring(index).indexOf("]");
//		System.out.println(raw.substring(head, tail));
		return raw.substring(head, tail);
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

	public String getPhase() {
		return phase;
	}

}