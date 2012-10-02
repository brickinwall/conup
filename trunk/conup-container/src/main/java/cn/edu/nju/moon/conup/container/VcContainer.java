package cn.edu.nju.moon.conup.container;

import org.apache.tuscany.sca.Node;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithm;
import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.communication.generator.CompositeAnalyzer;
import cn.edu.nju.moon.conup.communication.generator.CompositeAnalyzerImpl;
import cn.edu.nju.moon.conup.communication.launcher.LaunchCommunication;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.data.MessageQueue;
import cn.edu.nju.moon.conup.data.OutArcRegistryImpl;
import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistryImpl;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

public abstract class VcContainer {
	Node businessNode;
	Node communicationNode;
	ArcRegistry inArcRegistry;
	ArcRegistry outArcRegistry;
	TransactionRegistry transactionRegistry;
	MessageQueue messageQueue;

	// need to add interceptorCache? to discuss
	InterceptorCache interceptorCache;
	ComponentStatus componentStatus;
	
	ComponentListener listener;
	VcAlgorithm vcAlgorithm;
	
	CompositeAnalyzer compositeAnalyser;
	
	String businessComponentName = null;
	
	private Class oldClass;
	private Class newClass;

	

	public VcContainer() {
		init();
	}

	public VcContainer(Node businessNode) {
		init();
		this.businessNode = businessNode;
	}

	private void init() {
		inArcRegistry = InArcRegistryImpl.getInstance();
		outArcRegistry = OutArcRegistryImpl.getInstance();
		transactionRegistry = TransactionRegistryImpl.getInstance();
		interceptorCache = InterceptorCacheImpl.getInstance();
		messageQueue = MessageQueue.getInstance();
		componentStatus = ComponentStatus.getInstance();
		componentStatus.setDefaultStatus(ComponentStatus.NORMAL);
		componentStatus.setFreenessSetup(ComponentStatus.CONCURRENT);
//		componentStatus.setFreenessSetup(ComponentStatus.WAITING);
		
		listener = ComponentListenerImpl.getInstance();
		vcAlgorithm = new VcAlgorithmImpl(this);
		compositeAnalyser = CompositeAnalyzerImpl.getInstance();
	}
	
	public Class getOldClass() {
		return oldClass;
	}

	public void setOldClass(Class oldClass) {
		this.oldClass = oldClass;
	}

	public Class getNewClass() {
		return newClass;
	}

	public void setNewClass(Class newClass) {
		this.newClass = newClass;
	}
	
	public MessageQueue getMessageQueue() {
		return messageQueue;
	}
	
	public void setMessageQueue(MessageQueue messageQueue) {
		this.messageQueue = messageQueue;
	}

	public Node getBusinessNode() {
		return businessNode;
	}

	public Node getCommunicationNode() {
		return communicationNode;
	}

	public ArcRegistry getInArcRegistry() {
		return inArcRegistry;
	}

	public ArcRegistry getOutArcRegistry() {
		return outArcRegistry;
	}

	public TransactionRegistry getTransactionRegistry() {
		return transactionRegistry;
	}

	public void setBusinessNode(Node businessNode, String componentName) {
		this.businessNode = businessNode;
		// start communication component
		// try {
		// // String componentName =
		// businessNode.getDomainComposite().getComponents().get(0).getName();
		// communicationNode = LaunchCommunication.launch(componentName);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
	

	public void analyseNodeComposite(String filePath){
		compositeAnalyser.analyze(filePath);
	}
	public void setBusinessComponentName(String componentName, String compositeLocation) {
		this.businessComponentName = componentName;
		componentStatus.setComponentName(businessComponentName);
		// start communication component
		try {
			communicationNode = new LaunchCommunication().launch(componentName, compositeLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setCommunicationNode(Node communicationNode) {
		this.communicationNode = communicationNode;
	}

	public void setInArcRegistry(ArcRegistry inArcRegistry) {
		this.inArcRegistry = inArcRegistry;
	}

	public void setOutArcRegistry(ArcRegistry outArcRegistry) {
		this.outArcRegistry = outArcRegistry;
	}

	public void setTransactionRegistry(TransactionRegistry transactionRegistry) {
		this.transactionRegistry = transactionRegistry;
	}

	public InterceptorCache getInterceptorCache() {
		return interceptorCache;
	}

	public void setInterceptorCache(InterceptorCache interceptorCache) {
		this.interceptorCache = interceptorCache;
	}

	public ComponentStatus getComponentStatus() {
		return componentStatus;
	}

	public void setComponentStatus(ComponentStatus componentStatus) {
		this.componentStatus = componentStatus;
	}

	public String getBusinessComponentName() {
		return businessComponentName;
	}
	
	
	
	

}
