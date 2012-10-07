package cn.edu.nju.moon.conup.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.mortbay.jetty.Main;

import cn.edu.nju.moon.conup.buffer.update.Dispatcher;
import cn.edu.nju.moon.conup.buffer.update.JavaDispatcherImpl;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.data.MessageQueue;
import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.OldVersionRootTransation;
import cn.edu.nju.moon.conup.def.ReconfigurationVersion;

public class BufferPolicyInterceptor implements PhasedInterceptor {

	public static final String bufferPolicy = "BufferPolicy";
	public static final QName policySetQName = new QName(BufferPolicy.SCA11_NS,	bufferPolicy);
	
	private final static Logger LOGGER = Logger.getLogger(BufferPolicyInterceptor.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	/** The identifier of root transaction in Message header. */
	// private static String rootIdentifier = "RootVcTransaction";
	/** The identifier of parent transaction in Message header. */
	// private static String parentIdentifier = "ParentVcTransaction";
	// private static String ROOT_PARENT_IDENTIFIER =
	// "VcTransactionRootAndParentIdentifier";

	private Invoker next;
	private Operation operation;
	private List<BufferPolicy> policies;
	private PolicySubject subject;
	private String context;
	private String phase;

	public BufferPolicyInterceptor(PolicySubject subject, String context,
			Operation operation, List<BufferPolicy> policies, String phase) {
		super();
		this.operation = operation;
		this.policies = policies;
		this.subject = subject;
		this.phase = phase;
		this.context = getContext();
		init();
		// System.out.println("TracePolicyInterceptor...");
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
		} else if (subject instanceof Implementation) {
			Implementation impl = (Implementation) subject;
			return impl.getURI();
		}
		return null;
	}

	public void setNext(Invoker next) {
		// System.out.println("TracePolicyInterceptor.setNext()=" + next);
		this.next = next;
	}

	public Invoker getNext() {
		return next;
	}

	/**
	 * This method will be called by both service interceptor and reference
	 * interceptor. When the component is ready to update, this method will cache all 
	 * later coming msgs, and after update, restore these msgs
	 * */
	public Message invoke(Message msg) {
		// ignore messages that is passing through communication component
		if (operation.toString().contains("cn.edu.nju.moon.conup.communication.services")
				|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.VcService")
//				|| operation.toString().contains("NotifyService")
				|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.ArcService")
				|| operation.toString().contains("cn.edu.nju.moon.conup.communication.services.FreenessService")
				|| operation.toString().contains("cn.edu.nju.moon.conup.domain.services")) {
		// just pass the msg to another interceptor
//			LOGGER.info("In BufferPolicyInterceptor print thread id " + getThreadID() +
//					"\n\tWe current in: " + phase +
//					"\n\tpass msg down...");
//			System.out.println("IF: we current in " + phase);
//			System.out.println("In BufferPolicyInterceptor print thread id " + getThreadID());
//			System.out.println("BufferPolicyInterceptor: " + this);
			
			return getNext().invoke(msg);
		}else {
			Message returnMsg = null;
//			System.out.println("in Buffer interceptor operation =" + operation.toString());
			ComponentStatus componentStatus = ComponentStatus.getInstance();
			if (phase.equals(Phase.SERVICE_POLICY)) {
				LOGGER.info("in Buffer interceptor operation =" + operation.toString() +
						"\n\twe currnet in " + phase);
//				System.out.println("ELSE: we current in " + phase);
				MessageQueue msgQueue = MessageQueue.getInstance();
//				System.out.println(msgQueue);
				String currentStatus = componentStatus.getCurrentStatus();
				String freenessSetup = componentStatus.getFreenessSetup();
				System.out.println("componentStatus.getFreenessSetup(): " + componentStatus.getFreenessSetup());
				//TODO  have a bug!!!! fix it
//				if(currentStatus.equals(ComponentStatus.CONCURRENT) || currentStatus.equals(ComponentStatus.FREE) || currentStatus.equals(ComponentStatus.UPDATING))
				if(freenessSetup.equals(ComponentStatus.CONCURRENT) && 
					(currentStatus.equals(ComponentStatus.CONCURRENT) || currentStatus.equals(ComponentStatus.FREE) || currentStatus.equals(ComponentStatus.UPDATING))){
//					ComponentStatus.getInstance().getNext();				//Concurrent--------------> free
//					ComponentStatus.getInstance().getNext();				//free--------------------> updating
					
					String currentRootTxID = InterceptorCacheImpl.getInstance().getDependency(getThreadID()).getRootTx();
					// get old version root tx id
					// check current root tx id in old ?
					// dispatch	
					OldVersionRootTransation oldVersionRootTx = OldVersionRootTransation.getInstance();
					Set<String> oldVersionRootTxIds = oldVersionRootTx.getOldRootTxIds();
					analyseRootTx(oldVersionRootTxIds);
					Dispatcher dispatcher = new JavaDispatcherImpl();
					ReconfigurationVersion rcfgVersion = ReconfigurationVersion.getInstance();
					ReflectiveInstanceFactory instanceFactory = rcfgVersion.getInstanceFactory();
					synchronized(instanceFactory){
						if((oldVersionRootTxIds!=null) && oldVersionRootTxIds.contains(currentRootTxID)){
							LOGGER.info("dispatcher.dispatchToOldVersion()");
//							System.out.println("dispatcher.dispatchToOldVersion()");
							dispatcher.dispatchToOldVersion();
						}else{
							LOGGER.info("dispatcher.dispatchToNewVersion()");
//							System.out.println("dispatcher.dispatchToNewVersion()");
							dispatcher.dispatchToNewVersion();
						}
					}
					while(!componentStatus.getCurrentStatus().equals(ComponentStatus.UPDATING)){				//Concurrent/Free--------------> updating
						componentStatus.getNext();
					}
					
					if(OldVersionRootTransation.getInstance().getOldRootTxIds().isEmpty() 
						&& componentStatus.getCurrentStatus().equals(ComponentStatus.UPDATING)){
						componentStatus.getNext();							// updating----------> updated
						LOGGER.info("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + ", which is supposed to be UPDATED.");
						rcfgVersion.setOldVersion(rcfgVersion.getNewVersion());
						try {
							instanceFactory.setCtr(rcfgVersion.getNewVersion().getConstructor());
							rcfgVersion.reset();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
						componentStatus.getNext();							// updated--------> activated
						LOGGER.info("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + ", which is supposed to be ACTIVATED.");
//						System.out.println("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + ", which is supposed to be ACTIVATED.");
					}
					
					
				}else if(freenessSetup.equals(ComponentStatus.WAITING) && 
						(currentStatus.equals(ComponentStatus.WAITING) || currentStatus.equals(ComponentStatus.FREE) || currentStatus.equals(ComponentStatus.UPDATING))){
					//check freeness
					//update implementation && cache messages
					//resume all cached messages
					if(currentStatus.equals(ComponentStatus.WAITING)){	//achieve freeness!!!
						boolean freenessFlag = queryFreeness();
						if(freenessFlag){
							if(currentStatus.equals(ComponentStatus.WAITING))
								componentStatus.getNext();					// WAITING------------------->FREE
							LOGGER.info("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + "which is supposed to be FREENESS.");
//							System.out.println("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + "which is supposed to be FREENESS.");
							// go to update
							if(currentStatus.equals(ComponentStatus.FREE))
								componentStatus.getNext();					// FREE------------------>UPDATING
							ReconfigurationVersion rcfgVersion = ReconfigurationVersion.getInstance();
							ReflectiveInstanceFactory instanceFactory = rcfgVersion.getInstanceFactory();
							Class newVersion = rcfgVersion.getNewVersion();
							try {
								instanceFactory.setCtr(newVersion.getConstructor());
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
							
							//finish update
							if(currentStatus.equals(ComponentStatus.UPDATING))
								componentStatus.getNext();					// UPDATING------------------->UPDATED
							
							Map<PhasedInterceptor, Queue<Message>> msgMap = msgQueue.getMsgMap();
							Queue<Message> queue = null;
							if(queue == null){
								queue = msgMap.get(this);
							}
							while(!queue.isEmpty()){
								returnMsg = getNext().invoke(queue.poll());
							}
							if(currentStatus.equals(ComponentStatus.UPDATED))
								componentStatus.getNext();					// updated--------> activated
							LOGGER.info("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + "which is supposed to be ACTIVATED.");
//							System.out.println("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + "which is supposed to be ACTIVATED.");
							return returnMsg;
							
						}else{
							//wait for freeness
						}
						
					}else if(currentStatus.equals(ComponentStatus.UPDATING)){
						//cache messages during updating
						Map<PhasedInterceptor, Queue<Message>> msgMap = msgQueue.getMsgMap();
						Queue<Message> queue = null;
						if (msgMap.containsKey(this)) {
							msgMap.get(this).add(msg);
						}else {
							queue = new ConcurrentLinkedQueue<Message>();
							queue.add(msg);
							msgMap.put(this, queue);
						}
						
						synchronized (this) {
							try {
								wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
//						return getNext().invoke(msg);
					}
//					else if(currentStatus.equals(ComponentStatus.UPDATED)){
//						Map<PhasedInterceptor, Queue<Message>> msgMap = msgQueue.getMsgMap();
//						Queue<Message> queue = null;
//						if(queue == null){
//							queue = msgMap.get(this);
//						}
//						while(!queue.isEmpty()){
//							returnMsg = getNext().invoke(queue.poll());
//						}
//						componentStatus.getNext();					// updated--------> activated
//						System.out.println("CurrentComponentStatus: " + componentStatus.getCurrentStatus() + "which is supposed to be ACTIVATED.");
//						return returnMsg;
//					}
					
					
					//TODO ACTIVATED------------------------------>NORMAL/VALID
					
					
					
				}else if(freenessSetup.equals(ComponentStatus.BLOCKING) && (currentStatus.equals(ComponentStatus.BLOCKING))){
					
				}else{
					
				}
				
				
//				
//				if (queryComponentStatus()) {
//					// cache all the incoming msg to msgQueue
//					Map<PhasedInterceptor, Queue<Message>> msgMap = msgQueue.getMsgMap();
//					 Queue<Message> queue = null;
//					 if (msgMap.containsKey(this)) {
//						 msgMap.get(this).add(msg);
//					 }else {
//						 queue = new ConcurrentLinkedQueue<Message>();
//						 queue.add(msg);
//						 msgMap.put(this, queue);
//					 }
//					 
//					System.out.println("In BufferPolicyInterceptor print thread id " + getThreadID());
//					System.out.println("In BufferPolicyInterceptor, add Msg to queue...");
//					System.out.println("BufferPolicyInterceptor: " + this);
//					
//					System.out.println("before call the wait method of this thread......");
//					if(queue == null)
//						queue = msgMap.get(this);
//					System.out.println("queue.size():" + queue.size() + "\n");
//					synchronized (this) {
//						try {
//							wait();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					if(queue == null){
//						queue = msgMap.get(this);
//					}
//					System.out.println("before restore all cached msgs......");
//					System.out.println("queue.size():" + queue.size());
//					while(!queue.isEmpty()){
//						returnMsg = getNext().invoke(queue.poll());
//					}
//					
//					System.out.println("In BufferPolicyInterceptor print thread id "+ getThreadID());
//					System.out.println("In BufferPolicyInterceptor, just print BufferPolicyInterceptor...");
//					System.out.println("BufferPolicyInterceptor: " + this + "\n");
//					
//					return returnMsg;
//				} else {
//					System.out.println("In BufferPolicyInterceptor print thread id "+ getThreadID());
//					System.out.println("In BufferPolicyInterceptor, just print BufferPolicyInterceptor...");
//					System.out.println("BufferPolicyInterceptor: " + this + "\n");
//				}
			}
			
			return getNext().invoke(msg);
		}// else

	}

	/**
	 * query the component status if the component status is xxx, we cache all
	 * the after coming msg to the messageQueue
	 */
	public boolean queryComponentStatus() {
		boolean cacheFlag = false;
		ComponentStatus componentStatus = ComponentStatus.getInstance();
		System.out.println("current status is: " + componentStatus.getCurrentStatus());
		if (componentStatus.getCurrentStatus().equals(ComponentStatus.FREE)
				|| componentStatus.getCurrentStatus().equals(ComponentStatus.UPDATING)
				|| componentStatus.getCurrentStatus().equals(ComponentStatus.BLOCKING)) {
			cacheFlag = true;
		} else {
			cacheFlag = false;
		}
		return cacheFlag;
	}

	private boolean queryFreeness(){
		boolean freeFlag = false;
		Set<Arc> inArcs = InArcRegistryImpl.getInstance().getArcs();
		Set<String> rootTransactionIds = new HashSet<String>();
		Iterator iterator = inArcs.iterator();
		while(iterator.hasNext()){
			Arc arc = (Arc) iterator.next();
			if(!rootTransactionIds.contains(arc.getRootTransaction()))
				rootTransactionIds.add(arc.getRootTransaction());
		}
		iterator = rootTransactionIds.iterator();
		while(iterator.hasNext()){
			Set<Arc> belongToSameRootID = InArcRegistryImpl.getInstance().getArcsViaRootTransaction((String)iterator.next());
			boolean hasFuture = false;
			boolean hasPast = false;
			for(Arc arc : belongToSameRootID){
				if(arc.getType().equals("future"))
					hasFuture = true;
				if(arc.getType().equals("past"))
					hasPast = true;
			}
			if(hasFuture && hasPast){
				return false;
			}
		}
		return true;
	}
	
	/* return current thread ID. */
	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	private Component getComponent() {
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
	
	private boolean analyseRootTx(Set<String> oldRootTx){
		ArcRegistry inArcRegistry = InArcRegistryImpl.getInstance();
		Set<Arc> inArcs = inArcRegistry.getArcs();
		for(Arc arc : inArcs){
			if(arc.getType().equals(Arc.PAST)){
				oldRootTx.add(arc.getRootTransaction());
			}
		}
		return true;
	}

	public String getPhase() {
		return phase;
	}
	
}
