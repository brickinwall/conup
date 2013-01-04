package cn.edu.nju.moon.conup.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.communication.services.ArcService;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.Scope;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;
import cn.edu.nju.moon.conup.printer.container.ContainerPrinter;

public class VcAlgorithmImpl implements VcAlgorithm {
	
	private final static Logger LOGGER = Logger.getLogger(VcAlgorithmImpl.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	private VcContainer vcContainer;
	private ArcRegistry inArcRegistry;
	private ArcRegistry outArcRegistry;
	private TransactionRegistry transactionRegistry;

	public VcAlgorithmImpl(VcContainer vcContainer) {
		this.vcContainer = vcContainer;
		inArcRegistry = vcContainer.getInArcRegistry();
		outArcRegistry = vcContainer.getOutArcRegistry();
		transactionRegistry = vcContainer.getTransactionRegistry();
	}

	public String getServiceString(String componentName) {
		return componentName + "Comm#service-binding(ArcService/ArcService)";
	}

	 @Override
	public void analyze(String transactionStatus, String threadID,
			Set<String> futureC, Set<String> pastC) {
//		System.out.println("transaction.status: " + transactionStatus);
		Node communicationNode = vcContainer.getCommunicationNode();
		String currentTransaction = null;
		String parentTransaction = null;
		String rootTransaction = null;
		String hostComponent = null;
		InterceptorCache cache = vcContainer.getInterceptorCache();
		ComponentStatus componentStatus = vcContainer.getComponentStatus();
		Scope scope = componentStatus.getScope();
		
		// get root, parent and current transaction id from InterceptorCache
		TransactionDependency dependency = cache.getDependency(threadID);
		currentTransaction = dependency.getCurrentTx();
		parentTransaction = dependency.getParentTx();
		rootTransaction = dependency.getRootTx();
		hostComponent = dependency.getHostComponent();
		boolean isRoot = false;
		
		//if current(host) component is not in scope, 
		//no arcs need to be created for it. 
		if(scope != null && !scope.contains(hostComponent)){
			return;
		}
		
		/**
		 * analyze transactionStatus, according to the status(start,running,
		 * end), then change the Data Module(InArcsRegistry, OutArcsRegistry,
		 */
		if (transactionStatus.equals("start")) {
			LOGGER.info("*** A new transaction start.....");
			if(rootTransaction.equals(currentTransaction)){
				//current transaction is root
				isRoot = true;
			}
			
			Arc lfe = new Arc();
			lfe.setType("future");
			lfe.setRootTransaction(rootTransaction);
			lfe.setSourceComponent(hostComponent);
			lfe.setTargetComponent(hostComponent);
			if(!inArcRegistry.contain(lfe)){
				inArcRegistry.addArc(lfe);
			}
			if(!outArcRegistry.contain(lfe)){
				outArcRegistry.addArc(lfe);
			}

			Arc lpe = new Arc();
			lpe.setType("past");
			lpe.setRootTransaction(rootTransaction);
			lpe.setSourceComponent(hostComponent);
			lpe.setTargetComponent(hostComponent);
			if(!inArcRegistry.contain(lpe)){
				inArcRegistry.addArc(lpe);
			}
			if(!outArcRegistry.contain(lpe)){
				outArcRegistry.addArc(lpe);
			}

		} else if (transactionStatus.equals("running")) {
			// check outArcRegistry whether it has future arcs not in futureC
			Set<Arc> futureArcsInOutArcRegistry = outArcRegistry.getArcsViaType("future");
			currentTransaction = cache.getDependency(threadID).getCurrentTx();
			rootTransaction = transactionRegistry.getDependency(currentTransaction).getRootTx();
			
			//setup is done berfore root's first sub-tx
			VcContainer container;
			ComponentListenerImpl listener;
			Map<String, Boolean> isSetupDone;
			container = VcContainerImpl.getInstance();
			isSetupDone = ComponentListenerImpl.isSetupDone;
			if(rootTransaction.equals(currentTransaction)){
				//current transaction is root
				isRoot = true;
			}
			LOGGER.info("*** transactionStatus:running, Current tx is root = " + isRoot);
			if(isRoot && !isSetupDone.get(rootTransaction)){
				//create a new arc
				Arc arc = new Arc();
				arc.setType("future");
				arc.setRootTransaction(rootTransaction);
				arc.setSourceComponent(hostComponent);
				arc.setTargetComponent(hostComponent);
				
				String endpoint = getServiceString(hostComponent);
				ArcService arcService;
				try {
					arcService = communicationNode.getService(ArcService.class,
							endpoint);
					LOGGER.info("*** Current transaction is root, and setup is not done, start setup.");
					arcService.setUp(arc, scope);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				}
				isSetupDone.put(rootTransaction, true);
				LOGGER.info("Transaction status: " + transactionStatus + ", isSetupDone: ");
				printIsSetupDone(isSetupDone);
			}//END IF(isRoot)
			
			
			Set<Arc> futureArcsBelongToRootInOutArcRegistry = new HashSet<Arc>();
			for (Arc arc : futureArcsInOutArcRegistry) {
				if (arc.getRootTransaction().equals(rootTransaction))
					futureArcsBelongToRootInOutArcRegistry.add(arc);
			}

			Set<String> futureComponentNames = new HashSet<String>();
			futureComponentNames.addAll(futureC);
			// find some component will not be used in the future
			// change the arc from future to past
			for (Arc arc : futureArcsBelongToRootInOutArcRegistry) {
				if (!futureComponentNames.contains(arc.getTargetComponent())
						&& !arc.getTargetComponent().equals(hostComponent)) {
					LOGGER.info("*** " + arc.toString()	+ "will not use this component anymore, add Past arc in OutArcRegistry.");
					
					outArcRegistry.update(arc);

					// notify sub-components to change the arcs to them
					String serviceString = getServiceString(arc
							.getTargetComponent());
					LOGGER.info("*** notify sub-components to add past arc in their InArcRegistry.");
					
					ArcService arcService;
					try {
						arcService = communicationNode.getService(
								ArcService.class, serviceString);
						arcService.update(arc, "sub");
					} catch (NoSuchServiceException e) {
						e.printStackTrace();
					}

				}
			}// for
			
			// because of the local dependences information not fully describe actual invocation
			// when a component will call another component twice, we can not get this message from futureC
			// so we will make some wrong deletion, the below code is used to fix this problem
			
			// because every tx ends, it will add host component to pastC
			// if this host component will host other tx in the futureC, it will not be deleted in the futureC
			// so we will compare futureC with pastC, and find which component will run twice in the future.
			
			for(String past : pastC){
				if(futureC.contains(past)){
					Arc arc = new Arc();
					arc.setType("future");
					arc.setRootTransaction(rootTransaction);
					arc.setSourceComponent(hostComponent);
					arc.setTargetComponent(past);
					outArcRegistry.addArc(arc);
					
					String serviceString = getServiceString(past);
					ArcService arcService;
					try {
						arcService = communicationNode.getService(ArcService.class, serviceString);
						arcService.createArc(arc);
					} catch (NoSuchServiceException e) {
						e.printStackTrace();
					}
				}
			}
			
			
			
			

		} else { // transactionStatus.equals("end")
			//remove this->this in outArcRegistry and inArcRegistry
			Arc lfe = new Arc();
			lfe.setType("future");
			lfe.setRootTransaction(rootTransaction);
			lfe.setSourceComponent(hostComponent);
			lfe.setTargetComponent(hostComponent);
			inArcRegistry.removeArc(lfe);
			outArcRegistry.removeArc(lfe);
			
			Arc lpe = new Arc();
			lpe.setType("past");
			lpe.setRootTransaction(rootTransaction);
			lpe.setSourceComponent(hostComponent);
			lpe.setTargetComponent(hostComponent);
			inArcRegistry.removeArc(lpe);
			outArcRegistry.removeArc(lpe);
			
			//remove(destroy) current transaction id
//			removeTransactionID(currentTransaction);
			
			//if root transaction ends
			if(rootTransaction.equals(currentTransaction)){
				String endpoint = getServiceString(hostComponent);
				ArcService arcService;
				try {
					arcService = communicationNode.getService(ArcService.class,
							endpoint);
					LOGGER.info("*** root transaction ends, start clean up.");
					arcService.cleanUp(rootTransaction, scope);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				} 
			}// END IF
			
		}//else
		
		
		LOGGER.info("*** >>in VcAlgorithm.analyze(...) print informations start:");
		ContainerPrinter containerPrinter = new ContainerPrinter();
		containerPrinter.printInArcRegistry(inArcRegistry);
		containerPrinter.printOutArcRegistry(outArcRegistry);
		containerPrinter.printTransactionRegistry(transactionRegistry);

	}// analyze
	
	private void printIsSetupDone(Map<String, Boolean> isSetupDone){
		Iterator<Entry<String, Boolean>> iterator;
		iterator = isSetupDone.entrySet().iterator();
		String tmp = "";
		while(iterator.hasNext()){
			tmp += "\n\t" + iterator.next().toString();
		}
		LOGGER.info(tmp);
	}

}