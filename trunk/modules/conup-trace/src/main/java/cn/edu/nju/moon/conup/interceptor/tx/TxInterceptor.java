package cn.edu.nju.moon.conup.interceptor.tx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
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
	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
	/**
	 * it's used to identify a ended sub tx id in the response message
	 */
//	private static String HOSTIDENTIFIER = "HostIdentifier";
//	private static final String ROOT_TX = "ROOT_TX";
//	private static final String ROOT_COMP = "ROOT_COMP";
//	private static final String PARENT_TX = "PARENT_TX";
//	private static final String PARENT_COMP = "PARENT_COMP";
//	private static final String SUB_TX = "SUB_TX";
//	private static final String SUB_COMP = "SUB_COMP";
	
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
		// locate ROOT_PARENT_IDENTIFIER in message body
		List<Object> msgBodyOriginal;
		if (msg.getBody() == null) {
			Object[] tmp = new Object[1];
			tmp[0] = (Object) "";
			msgBodyOriginal = Arrays.asList(tmp);
		} else
			msgBodyOriginal = Arrays.asList((Object[]) msg.getBody());

		List<Object> msgBody = new ArrayList<Object>();
		msgBody.addAll(msgBodyOriginal);
		String transactionTag = null;
		for (Object object : msgBody) {
			if (object.toString().contains(TxInterceptor.ROOT_PARENT_IDENTIFIER)) {
				transactionTag = object.toString();
				msgBody.remove(object);
				break;
			}
		}
		
		String hostComponent = getComponent().getName();
		
		if (phase.equals(Phase.SERVICE_POLICY)) {
			msg = txLifecycleMgr.traceServicePhase(msg, transactionTag, msgBody, hostComponent);
		} else if (phase.equals(Phase.REFERENCE_POLICY)) {
			msg = txLifecycleMgr.traceReferencePhase(msg, transactionTag, msgBody, hostComponent, getTargetServiceName(), txDepMonitor);
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


	@Override
	public void freeze(Object obj) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void defreeze(Object obj) {
		// TODO Auto-generated method stub
		
	}

}
