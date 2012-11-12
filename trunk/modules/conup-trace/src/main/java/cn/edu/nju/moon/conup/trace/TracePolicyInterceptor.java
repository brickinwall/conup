package cn.edu.nju.moon.conup.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeEndpointImpl;
import org.apache.tuscany.sca.core.assembly.impl.RuntimeEndpointReferenceImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

import cn.edu.nju.moon.conup.ext.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


public class TracePolicyInterceptor implements PhasedInterceptor {

	public static final String tracePolicy = "TracePolicy";
	public static final QName policySetQName = new QName(TracePolicy.SCA11_NS, tracePolicy);
	private final static Logger LOGGER = Logger.getLogger(TracePolicyInterceptor.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	} 
	
	/** The identifier of root transaction in Message header. */
	private static String rootIdentifier = "RootVcTransaction";
	/** The identifier of parent transaction in Message header. */
	private static String parentIdentifier = "ParentVcTransaction";
	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
	private static String HOSTIDENTIFIER = "HostIdentifier";

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
//		System.out.println("TracePolicyInterceptor.setNext()=" + next);
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
		//ignore messages that is passing through communication component
		if(operation.toString().contains("cn.edu.nju.moon.conup.communication.services")
			|| operation.toString().contains("NotifyService")
			|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.VcService")
			|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.ArcService")
			|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.FreenessService")
			|| operation.toString().contains("cn.edu.nju.moon.conup.domain.services")){
				return getNext().invoke(msg);
		} else{
			LOGGER.info("operation =" + operation.toString());
			msg = exchangeViaMsgBody(msg);
			return getNext().invoke(msg);
		}//else
		
	}
	
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
	  *  This method is supposed to exchange root/parent transaction id via Message body.
	  */
	private Message exchangeViaMsgBody(Message msg){
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
			CompositeContext compositeContext = null;
			
			if(transactionTag==null || transactionTag.equals("")){
				//an exception is preferred
				LOGGER.warning("Error: message body cannot be null in a service body");
			}
			
			//get root, parent and current transaction id
			String target = getTargetString(transactionTag);
			String rootInfo = target.split(",")[0];
			String parentInfo = target.split(",")[1];
			rootTx = rootInfo.split(":")[0].equals("null") ? null : rootInfo.split(":")[0];
			rootComponent = rootInfo.split(":")[1].equals("null") ? null : rootInfo.split(":")[1];
			parentTx = parentInfo.split(":")[0].equals("null") ? null : parentInfo.split(":")[0];
			parentComponent = parentInfo.split(":")[1].equals("null") ? null : parentInfo.split(":")[1];
			
			//get host component name
			hostComponent = getComponent().getName();
			
			// check interceptor cache
			InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
			threadID = getThreadID();
			TransactionContext txContext = cache.getTxContext(hostComponent);
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
				cache.setCache(threadID, txContext);
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
			TransactionContext txContext = cache.getTxContext(threadID);
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
			LOGGER.info(phase + " TraceInterceptor" +
					"\n\t" + "messageID:" + msg.getMessageID() +
					"\n\t" + "msgFrom:" + msg.getFrom() +
					"\n\t" + "msgTo:" + msg.getTo() +
					"\n\t" + "msgOperation:" + msg.getOperation() +
					"\n\t" + "threadID:" + threadID +
					"\n\t" + "rootTx:" + rootTx +
					"\n\t" + "rootComponent:" + rootComponent +
					"\n\t" + "parentTx:" + parentTx +
					"\n\t" + "parentComponent:" + parentComponent + 
					"\n\t" + "currentTx:" + currentTx + 
					"\n\t" + "hostComponent:" + hostComponent);

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
			LOGGER.info(msgBodyStr);
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
		if(raw.startsWith("\"")){
			raw = raw.substring(1);
		}
		if(raw.endsWith("\"")){
			raw = raw.substring(0, raw.length()-1);
		}
		int index = raw.indexOf(TracePolicyInterceptor.ROOT_PARENT_IDENTIFIER);
		int head = raw.substring(index).indexOf("[")+1;
		System.out.println(raw.substring(0, head));
		int tail = raw.substring(index).indexOf("]");
		System.out.println(raw.substring(head, tail));
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
