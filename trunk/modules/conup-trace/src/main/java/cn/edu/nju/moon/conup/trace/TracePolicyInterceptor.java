package cn.edu.nju.moon.conup.trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.interceptor.buffer.BufferInterceptor;
import cn.edu.nju.moon.conup.interceptor.tx.TxInterceptor;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorStub;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 *
 */
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
//	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
//	private static String HOSTIDENTIFIER = "HostIdentifier";
//	private static String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";
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
	private TxInterceptor txInterceptor;
	private BufferInterceptor bufferInterceptor;
	
	private NodeManager nodeMgr;
	private DynamicDepManager depMgr;
	private TxDepMonitor txDepMonitor;
	private TxLifecycleManager txLifecycleMgr;

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
			msg = txInterceptor.invoke(msg);
			msg = bufferInterceptor.invoke(msg);
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
	 * attach ended sub txs info to response message body at SERVICE_POLICY
	 * @param msg
	 * @return msg contains ended sub txs
	 */
	private Message attachEndedTxToResAtServicePolicy(Message msg) {
			String hostComp = null;
//			String rootTx = null;
//			String rootComp = null;
//			String parentTx = null;
//			String parentComp = null;
			
//			//locate ROOT_PARENT_IDENTIFIER in message body
//			List<Object> msgBodyOriginal;
//			// return value is void
//			if (msg.getBody() == null) {
//				msgBodyOriginal = new ArrayList<Object>();
//			} else{
//				msgBodyOriginal = Arrays.asList(msg.getBody());
//			}
//			
//			List<Object> msgBody = new ArrayList<Object>();
//			msgBody.addAll(msgBodyOriginal);
			
			if (phase.equals(Phase.SERVICE_POLICY)) {
				Map<String, Object> msgHeaders = msg.getHeaders();
				String subTx = null;
				String subComp = null;
				
				//get host component name
				hostComp = getComponent().getName();
				
				// current tx is a root tx, no need to attach any information to its response 
				InvocationContext invocationCtx = (InvocationContext) msgHeaders.get(TxInterceptor.INVOCATION_CONTEXT);
				if(invocationCtx.getSubTx() == null){
					msgHeaders.remove(TxInterceptor.INVOCATION_CONTEXT);
					return msg;
				}
				subTx = invocationCtx.getSubTx();
				subComp = invocationCtx.getSubComp();
//				rootTx = invocationCtx.getRootTx();
//				rootComp = invocationCtx.getRootComp();
//				parentTx = invocationCtx.getParentTx();
//				parentComp = invocationCtx.getParentComp();;
				
//				if(msgHeader.get(SUB_TX) == null){
//					return msg;
//				}
//				
//				subTx = msgHeader.get(SUB_TX).toString();
//				subComp = msgHeader.get(SUB_COMP).toString();
//				rootTx = msgHeader.get(ROOT_TX).toString();
//				rootComp = msgHeader.get(ROOT_COMP).toString();
//				parentTx = msgHeader.get(PARENT_TX).toString();
//				parentComp = msgHeader.get(PARENT_COMP).toString();
				
				assert subTx != null;
				assert hostComp.equals(subComp);
//				if( !hostComp.equals(subComp) ){
//					LOGGER.warning("hostComp: " + hostComp + " subComp: " + subComp);
//					assert hostComp.equals(subComp);
//				}
//				assert hostComp.equals(txCtx.getHostComponent());
				
				txLifecycleMgr.endLocalSubTx(hostComp, subTx);
				
				//generate info required to be attatched to the response msg body
//				StringBuffer endedSubTxTag = new StringBuffer();
//				endedSubTxTag.append(ENDED_SUB_TX_TAG);
//				endedSubTxTag.append("[");
//				endedSubTxTag.append(ROOT_TX + ":" + rootTx + ";");
//				endedSubTxTag.append(ROOT_COMP + ":" + rootComp + ";");
//				endedSubTxTag.append(PARENT_TX + ":" + parentTx + ";");
//				endedSubTxTag.append(PARENT_COMP + ":" + parentComp + ";");
//				endedSubTxTag.append(SUB_TX + ":" + subTx + ";");
//				endedSubTxTag.append(SUB_COMP + ":" + subComp );
//				endedSubTxTag.append("]");
				
				//reset the msg body
//				msgBody.add(endedSubTxTag.toString());
//				msg.setBody((Object [])msgBody.toArray());
				
				msgHeaders.remove(TxInterceptor.INVOCATION_CONTEXT);
				
//				msgHeader.remove(ROOT_TX);
//				msgHeader.remove(ROOT_COMP);
//				msgHeader.remove(PARENT_TX);
//				msgHeader.remove(PARENT_COMP);
//				msgHeader.remove(SUB_TX);
//				msgHeader.remove(SUB_COMP);
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
		
		Map<String, Object> msgHeaders = msg.getHeaders();
		InvocationContext invocationCtx = (InvocationContext) msgHeaders.get(TxInterceptor.INVOCATION_CONTEXT);
		if(invocationCtx == null || invocationCtx.getSubTx() == null){
			return msg;
		}
		currentTx = invocationCtx.getParentTx();
		hostComp = getComponent().getName();
		rootTx = invocationCtx.getRootTx();
		subTx = invocationCtx.getSubTx();
		subComp = invocationCtx.getSubComp();
		
//		List<Object> msgBodyOriginal;
//		if(msg.getBody() == null){
//			Object [] tmp = new Object[1];
//			tmp[0] = (Object)"";
//			msgBodyOriginal = Arrays.asList(tmp);
//		}
//		else{
//			if(msg.getBody().getClass().isArray())
//				msgBodyOriginal = Arrays.asList((Object [])msg.getBody());
//			else
//				msgBodyOriginal = Arrays.asList(msg.getBody());
//		}
//		
//		List<Object> msgBody = new ArrayList<Object>();
//		msgBody.addAll(msgBodyOriginal);
//		String subContextTag = null;
//		for(Object object : msgBody){
//			
//			if(object instanceof String && object.toString().contains("ENDED_SUB_TX_TAG")){
//				subContextTag = object.toString();
//				msgBody.remove(object);
//				break;
//			}
//		}
//		if(subContextTag == null)
//			return msg;
//		
//		LOGGER.fine("subContextTag:" + subContextTag + ", msgBody:" + msgBody);
		
		// Here we need to pay attention, the body in this return message should be only one object, not an array.
		// Because we have added ENDED_SUB_TX_TAG to the body in SERVICE.policy phase, and make the actual body become a list
		// If this service's return value is void, then msgBody.size can be 0. So we do not need to return anything
//		if(msgBody.size() != 0)
//			msg.setBody(msgBody.get(0));
		
//		LOGGER.fine("attach REFERENCE_POLICY: " + subContextTag);
		
//		Map<String, String> endedSubTxProperty = parseEndedSubTxTag(subContextTag);
		
//		if(endedSubTxProperty.size() == 0){
//			LOGGER.warning("invalid data in ENDED_SUB_TX_TAG");
//		}
//		
//		rootTx = endedSubTxProperty.get(ROOT_TX);
//		currentTx = endedSubTxProperty.get(PARENT_TX);
//		hostComp = endedSubTxProperty.get(PARENT_COMP);
//		subTx = endedSubTxProperty.get(SUB_TX);
//		subComp = endedSubTxProperty.get(SUB_COMP);
		
		assert hostComp != null;
//		assert hostComp.equals(getComponent().getName());
		
		if( !subComp.equals(hostComp)){
			
//			NodeManager nodeMgr = NodeManager.getInstance();
//			DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
//			Printer printer = new Printer();
			LOGGER.fine("TxS before endRemoteSubTx:");
//			printer.printTxs(LOGGER, depMgr.getTxs());
			
//			nodeMgr.getTxLifecycleManager(hostComp).endRemoteSubTx(subComp, hostComp, rootTx, currentTx, subTx);
			txLifecycleMgr.endRemoteSubTx(subComp, hostComp, rootTx, currentTx, subTx);
			
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
		if(phase.equals(Phase.SERVICE_POLICY) || phase.equals(Phase.REFERENCE_POLICY)){
			if (getComponent() != null) {
				String hostComp = getComponent().getName();
				this.nodeMgr = NodeManager.getInstance();
				this.depMgr = nodeMgr.getDynamicDepManager(hostComp);
				this.txDepMonitor = nodeMgr.getTxDepMonitor(hostComp);
				this.txLifecycleMgr = nodeMgr.getTxLifecycleManager(hostComp);
				
				String freenessConf = depMgr.getCompObject().getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);

				txInterceptor = new TxInterceptor(subject, operation, phase, txDepMonitor, txLifecycleMgr);
				bufferInterceptor = new BufferInterceptor(subject, phase, depMgr, txLifecycleMgr, freeness);
				InterceptorStub interceptorStub = NodeManager.getInstance().getInterceptorStub(hostComp);
				interceptorStub.addInterceptor(bufferInterceptor);
//				depMgr.registerObserver(bufferInterceptor);
			} else {
//				txInterceptor = new TxInterceptor(subject, operation, phase);
//				bufferInterceptor = new BufferInterceptor(subject, operation, phase);
			}
		}
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