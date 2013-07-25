package cn.edu.nju.moon.conup.interceptor.tx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.binding.jsonrpc.protocol.JsonRpc10Request;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
/**
 * TxInterceptor is used to transfer TxContext(root tx, parent tx, sub tx)
 * at refernece phase add TxContext to message body
 * at service phase retrieve TxContext from message body
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 *
 */
public class TxInterceptor implements Interceptor {

	private final static Logger LOGGER = Logger.getLogger(TxInterceptor.class.getName());
	/**
	 * it's used to identify a ended sub tx id in the response message
	 */
//	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
	private static String ROOT_PARENT_IDENTIFIER = "InvocationContext";

	private static String HOSTIDENTIFIER = "HostIdentifier";
//	private static final String ROOT_TX = "ROOT_TX";
//	private static final String ROOT_COMP = "ROOT_COMP";
//	private static final String PARENT_TX = "PARENT_TX";
//	private static final String PARENT_COMP = "PARENT_COMP";
//	private static final String SUB_TX = "SUB_TX";
//	private static final String SUB_COMP = "SUB_COMP";
	private static final String INVOCATION_CONTEXT = "INVOCATION_CONTEXT";
	
	private PolicySubject subject;
	private Operation operation;
	private String phase;
	private TxDepMonitor txDepMonitor;
	private TxLifecycleManager txLifecycleMgr;
	
	public TxInterceptor(PolicySubject subject, Operation operation, String phase, TxDepMonitor txDepMonitor, TxLifecycleManager txLifecycleMgr){
		this.subject = subject;
		this.operation = operation;
		this.phase = phase;
		this.txDepMonitor = txDepMonitor;
		this.txLifecycleMgr = txLifecycleMgr;
	}


	@Override
	public Message invoke(Message msg) {
		InvocationContext invocationContext = null;
		
		invocationContext = getInvocationCtxFromMsgHeader(msg.getHeaders());
		String hostComponent = getComponent().getName();
		
		if (phase.equals(Phase.SERVICE_POLICY)) {
			msg = traceServicePhase(msg, invocationContext, hostComponent);
		} else if (phase.equals(Phase.REFERENCE_POLICY)) {
			msg = traceReferencePhase(msg, hostComponent, getTargetServiceName(), txDepMonitor);
		} // else if(reference.policy)

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
	
	private InvocationContext getInvocationCtxFromMsgHeader(Map<String, Object> headers) {
		InvocationContext invocationCtx = null;
		Object object = headers.get("RequestMessage");
		if(object instanceof JsonRpc10Request){
			JsonRpc10Request jsonRpc10Request = (JsonRpc10Request)object;
			String invocationCtxStr = jsonRpc10Request.getInvocationCtx();
			invocationCtx = InvocationContext.getInvocationCtx(invocationCtxStr);
		}
		return invocationCtx;
	}


	private String getTargetServiceName(){
		String serviceName = null;
		serviceName = operation.getInterface().toString();	
		return serviceName;
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
	
	public Message traceServicePhase(Message msg, InvocationContext invocationContext,
			String hostComponent) {
		
		if(invocationContext ==null || invocationContext.toString().equals("")){
			//an exception is preferred
			LOGGER.warning("Error: message body cannot be null in a service body");
		}
		
		LOGGER.fine("trace SERVICE_POLICY : " + invocationContext);
		List<Object> originalMsgBody = Arrays.asList((Object [])msg.getBody());
		List<Object> msgBody = new ArrayList<Object>();
		msgBody.addAll(originalMsgBody);
		Map<String, Object> headers = msg.getHeaders();
		if(invocationContext != null)
			headers.put(INVOCATION_CONTEXT, invocationContext);
		
		txLifecycleMgr.resolveInvocationContext(invocationContext, hostComponent);
		
		String hostInfo = HOSTIDENTIFIER + "," + hostComponent;
		msgBody.add(hostInfo);
		msg.setBody((Object [])msgBody.toArray());
		return msg;
	}

	public Message traceReferencePhase(Message msg, String hostComponent, String serviceName, TxDepMonitor txDepMonitor) {
		InvocationContext invocationCtx = txLifecycleMgr.createInvocationCtx(hostComponent, serviceName ,txDepMonitor);
		msg.getHeaders().put("InvocationContext", invocationCtx.toString());
		LOGGER.fine("trace REFERENCE_POLICY : " + invocationCtx);
		return msg;
	}

//	/**
//	 * root and parent transaction id is stored in the format: VcTransactionRootAndParentIdentifier[ROOT_ID,PARENT_ID].
//	 * 
//	 * @return ROOT_ID,PARENT_ID
//	 * 
//	 * */
//	private String getTargetString(String raw){
//		if(raw == null){
//			return null;
//		}
//		if(raw.startsWith("\"")){
//			raw = raw.substring(1);
//		}
//		if(raw.endsWith("\"")){
//			raw = raw.substring(0, raw.length()-1);
//		}
//		int index = raw.indexOf(ROOT_PARENT_IDENTIFIER);
//		int head = raw.substring(index).indexOf("[")+1;
////		LOGGER.fine(raw.substring(0, head));
//		int tail = raw.substring(index).indexOf("]");
////		LOGGER.fine(raw.substring(head, tail));
//		return raw.substring(head, tail);
//	}
//	
//	private String getThreadID() {
//		return new Integer(Thread.currentThread().hashCode()).toString();
//	}


	@Override
	public void update(Object arg) {
		
	}

}