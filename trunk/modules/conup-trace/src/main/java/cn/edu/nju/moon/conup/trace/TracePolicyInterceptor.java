package cn.edu.nju.moon.conup.trace;

import java.util.ArrayList;
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
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 *
 */
public class TracePolicyInterceptor implements PhasedInterceptor {

	public static final String tracePolicy = "TracePolicy";
	public static final QName policySetQName = new QName(TracePolicy.SCA11_NS, tracePolicy);
	private final static Logger LOGGER = Logger.getLogger(TracePolicyInterceptor.class.getName());
	
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
	private UpdateManager updateMgr;

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
		LOGGER.fine("phase:" + phase + " operation:" + operation.toString());
		
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
			
			if (phase.equals(Phase.SERVICE_POLICY)) {
				Map<String, Object> msgHeaders = msg.getHeaders();
				String subTx = null;
				String subComp = null;
				String rootTx = null;
				
				//get host component name
				hostComp = getComponent().getName();
				
				// current tx is a root tx, no need to attach any information to its response 
				InvocationContext invocationCtx = (InvocationContext) msgHeaders.get(TxInterceptor.INVOCATION_CONTEXT);
				rootTx = invocationCtx.getRootTx();
				
				Map<String, ArrayList<String>> compsVisitLogs = updateMgr.getCompsVisitLogs();
				if(rootTx != null){
					// current tx is a sub tx
					
					StringBuffer currentCompVisitLog = new StringBuffer(rootTx);
					currentCompVisitLog.append("#").append(hostComp);
					currentCompVisitLog.append(":").append((String)msgHeaders.get("COMP_VERSION"));
					if(compsVisitLogs.get(rootTx) != null){
						for(String log : compsVisitLogs.get(rootTx)){
							currentCompVisitLog.append(",").append(log);
						}
					}
					msgHeaders.put("COMP_VERSION", currentCompVisitLog.toString());
					LOGGER.info("currentCompVisitLog:" + currentCompVisitLog);
				} else{
					// when rootTx is null, it means that current tx is root tx
					// then we do not need to put the component visit log to the header
					// because we do not need to send these informations to somebody
				}
				
				
				if(invocationCtx.getSubTx() == null){
					msgHeaders.remove(TxInterceptor.INVOCATION_CONTEXT);
					return msg;
				}
				subTx = invocationCtx.getSubTx();
				subComp = invocationCtx.getSubComp();
				
				assert subTx != null;
				assert hostComp.equals(subComp);
				
				txLifecycleMgr.endLocalSubTx(hostComp, subTx);
				
				msgHeaders.remove(TxInterceptor.INVOCATION_CONTEXT);
				
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
		InvocationContext invocationCtx = InvocationContext.getInvocationCtx((String)msgHeaders.get(TxInterceptor.INVOCATION_CONTEXT));

		if(invocationCtx == null || invocationCtx.getSubTx() == null){
			return msg;
		}

		hostComp = getComponent().getName();
		currentTx = invocationCtx.getParentTx();
		rootTx = invocationCtx.getRootTx();
		subTx = invocationCtx.getSubTx();
		subComp = invocationCtx.getSubComp();
		assert hostComp != null;
		assert hostComp.equals(invocationCtx.getParentComp());

		String compVersion = (String)msgHeaders.get("COMP_VERSION");
		LOGGER.info("COMP_VERSION:" + compVersion + "@attachEndedTxToResAtRefernecePolicy");
		
		// A -> B -> C
		// here we 
		Map<String, ArrayList<String>> compsVisitLogs = updateMgr.getCompsVisitLogs();
		if(compsVisitLogs.get(rootTx) != null){
			// TODO
			checkConsistency(compsVisitLogs, compVersion.split("#")[0], compVersion.split("#")[1]);
			compsVisitLogs.get(rootTx).add(compVersion.split("#")[1]);
			
		} else{
			ArrayList<String> logs = new ArrayList<String>();
			logs.add(compVersion.split("#")[1]);
			compsVisitLogs.put(rootTx, logs);
		}
		LOGGER.info("currentCompVisitLog:" + compsVisitLogs.get(rootTx));
		if( !subComp.equals(hostComp)){
			
//			NodeManager nodeMgr = NodeManager.getInstance();
//			DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
//			Printer printer = new Printer();
			LOGGER.fine("TxS before endRemoteSubTx:");
//			printer.printTxs(LOGGER, depMgr.getTxs());
			
//			nodeMgr.getTxLifecycleManager(hostComp).endRemoteSubTx(subComp, hostComp, rootTx, currentTx, subTx);
//			txLifecycleMgr.endRemoteSubTx(subComp, hostComp, rootTx, currentTx, subTx);
			txLifecycleMgr.endRemoteSubTx(invocationCtx);
			
			LOGGER.fine("TxS after endRemoteSubTx:");
//			printer.printTxs(LOGGER, depMgr.getTxs());
		}
		return msg;
	}

	private void checkConsistency(Map<String, ArrayList<String>> compsVisitLogs, String rootTxId, String subCompVisitLog) {
		ArrayList<String> previousSubCompVisitLogs = compsVisitLogs.get(rootTxId);
		String subCompName = subCompVisitLog.split(":")[0];
		String subCompVersion = subCompVisitLog.split(":")[1];
		for(String s : previousSubCompVisitLogs){
			if(s.contains(subCompName)){
				if(!s.split(":")[1].equals(subCompVersion)){
					// Found inconsistency!
				} else {
					continue;
				}
			}
		}
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
				CompLifeCycleManager compLifeCycleMgr = nodeMgr.getCompLifecycleManager(hostComp);
				
				String freenessConf = depMgr.getCompObject().getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf, compLifeCycleMgr);

				txInterceptor = new TxInterceptor(subject, operation, phase, txDepMonitor, txLifecycleMgr);
				this.updateMgr = nodeMgr.getUpdateManageer(hostComp);
				bufferInterceptor = new BufferInterceptor(subject, phase, txLifecycleMgr, freeness, compLifeCycleMgr, updateMgr);
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

}