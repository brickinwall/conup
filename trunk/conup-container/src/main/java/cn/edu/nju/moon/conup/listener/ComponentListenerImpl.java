/**
 * 
 */
package cn.edu.nju.moon.conup.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithm;
import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.communication.services.ArcService;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.InArcRegistryImpl;
import cn.edu.nju.moon.conup.data.OndemandThreadBuffer;
import cn.edu.nju.moon.conup.data.OutArcRegistryImpl;
import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistryImpl;
import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.Scope;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.def.TransactionSnapshot;
import cn.edu.nju.moon.conup.domain.services.TransactionIDService;
import cn.edu.nju.moon.conup.printer.container.ContainerPrinter;


/**
 * ComponentListenerImpl class only has one instance, and provides a global point of access to it.
 * 
 * @author nju
 *
 */
public class ComponentListenerImpl implements ComponentListener{
	private static ComponentListenerImpl instance = new ComponentListenerImpl();
	private final static Logger LOGGER = Logger.getLogger(VcAlgorithmImpl.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	private VcContainer vcContainer;
	private ArcRegistry inArcRegistry;
	private ArcRegistry outArcRegistry;
	private TransactionRegistry transactionRegistry;
	private Node commNode;

	private ComponentListenerImpl(){
	}
	
	/** Get the ComponentListenerImpl instance.*/
	public static ComponentListenerImpl getInstance(){
		return instance;
	}
	
	/** when an event occurs, this method will be called. */
	public boolean notify(String transactionStatus, String threadID, Set<String> futureC, Set<String> pastC){
//		System.out.println("\n\ntransaction.status: " + transactionStatus);
//TODO just for suping's test, delete in the future!!!
		Set<String> futureTempSet = new ConcurrentSkipListSet<String>();
		Iterator iterator = futureC.iterator();
		while(iterator.hasNext()){
			String temp = (String) iterator.next();
			String[] strs = temp.split("/");
			futureTempSet.add(strs[0]);
		}
		futureC.removeAll(futureC);
		futureC.addAll(futureTempSet);
		
		Set<String> pastTempSet = new ConcurrentSkipListSet<String>();
		iterator = pastC.iterator();
		while(iterator.hasNext()){
			String temp = (String) iterator.next();
			String[] strs = temp.split("/");
			pastTempSet.add(strs[0]);
		}
		pastC.removeAll(pastC);
		pastC.addAll(pastTempSet);
		
//		Set<String> pastTempSet = new HashSet<String>();
//		for(String component : pastC){
//			pastC.remove(component);
//			String[] strs = component.split("/");
//			pastTempSet.add(strs[0]);
//		}
//		pastC.addAll(pastTempSet);
		
		vcContainer = VcContainerImpl.getInstance();
		inArcRegistry = InArcRegistryImpl.getInstance();
		outArcRegistry = OutArcRegistryImpl.getInstance();
		transactionRegistry = TransactionRegistryImpl.getInstance();
		commNode = VcContainerImpl.getInstance().getCommunicationNode();
		
		Node communicationNode = vcContainer.getCommunicationNode();
		String currentTransaction = null;
		String parentTransaction = null;
		String rootTransaction = null;
		String hostComponent = null;
		String rootComponent = null;
		String parentComponent = null;
		InterceptorCache cache = vcContainer.getInterceptorCache();
		ComponentStatus componentStatus = vcContainer.getComponentStatus();
		Scope scope = componentStatus.getScope();
		
		//If scope is not null, it means dynamic update is limited in the affected components
		//so we need to remove components from futureC and pastC which are not in scope
		if(scope != null){
			for(String component : futureC){
				if(!scope.contains(component))
					futureC.remove(component); 
			}
			for(String component : pastC){
				if(!scope.contains(component))
					pastC.remove(component);
			}
		}// END IF

		// get root, parent and current transaction id from InterceptorCache
		TransactionDependency dependency = cache.getDependency(threadID);
		currentTransaction = dependency.getCurrentTx();
		parentTransaction = dependency.getParentTx();
		rootTransaction = dependency.getRootTx();
		hostComponent = dependency.getHostComponent();
		rootComponent = dependency.getRootComponent();
		parentComponent = dependency.getParentComponent();
		
		boolean isRoot = false;
		
		if (transactionStatus.equals("start")) {
			if(rootTransaction==null && parentTransaction==null 
					&& currentTransaction==null && hostComponent!=null){
				//current transaction is root
				isRoot = true;
				currentTransaction = createTransactionID();
				rootTransaction = currentTransaction;
				parentTransaction = currentTransaction;
				//update interceptor cache dependency
				dependency.setCurrentTx(currentTransaction);
				dependency.setParentTx(parentTransaction);
				dependency.setRootTx(rootTransaction);
				rootComponent = hostComponent;
				parentComponent = hostComponent;
				dependency.setHostComponent(hostComponent);
				dependency.setRootComponent(rootComponent);
				dependency.setParentComponent(parentComponent);
			} else if(rootTransaction!=null && parentTransaction!=null 
					&& currentTransaction==null && hostComponent!=null){
				//current transaction is a sub-transaction
				currentTransaction = createTransactionID();
				//update interceptor cache dependency
				dependency.setCurrentTx(currentTransaction);
			} else{
				LOGGER.warning("Error: dirty data in InterceptroCache.");
//				System.out.println("Error: dirty data in InterceptroCache.");
			}
			
			// add the current transaction and its dependency to TransactionRegistry
			TransactionDependency currentTransactionDependency = new TransactionDependency();
			currentTransactionDependency.setCurrentTx(currentTransaction);
			currentTransactionDependency.setParentTx(parentTransaction);
			currentTransactionDependency.setRootTx(rootTransaction);
			currentTransactionDependency.setHostComponent(hostComponent);
			currentTransactionDependency.setRootComponent(rootComponent);
			currentTransactionDependency.setParentComponent(parentComponent);
			currentTransactionDependency.setStatus(TransactionSnapshot.START);
			currentTransactionDependency.setFutureComponents(futureC);
			currentTransactionDependency.setPastComponents(pastC);
			transactionRegistry.addDependency(currentTransaction,
					currentTransactionDependency);
			
			LOGGER.info(">>>>In ComponentListenerImpl.notify(before start, ,...)");
//			System.out.println(">>>>In ComponentListenerImpl.notify(before start, ,...)");
			ContainerPrinter containerPrinter = new ContainerPrinter();
			containerPrinter.printInArcRegistry(inArcRegistry);
			containerPrinter.printOutArcRegistry(outArcRegistry);
			containerPrinter.printTransactionRegistry(transactionRegistry);
			LOGGER.info("<<<<In ComponentListenerImpl.notify(before start, ,...)");
//			System.out.println("<<<<In ComponentListenerImpl.notify(before start, ,...)");
			
			//notify current transaction's parent that a new sub-tx starts
			if(!isRoot){
				String targetEndpoint = parentComponent +
						"Comm#service-binding(ArcService/ArcService)";
				LOGGER.fine("Try to notify a new sub-tx starts:" + 
						"\n\t" + "targetEndpoint: " + targetEndpoint +
						"\n\t" + "parentTransaction: " + parentTransaction +
						"\n\t" + "currentTransaction: " + currentTransaction +
						"\n\t" + "hostComponent: " + hostComponent +
						"\n\t" + "txStatus: " + TransactionSnapshot.START);
//				System.out.println("Try to notify a new sub-tx starts:");
//				System.out.println("\t" + "targetEndpoint: " + targetEndpoint);
//				System.out.println("\t" + "parentTransaction: " + parentTransaction);
//				System.out.println("\t" + "currentTransaction: " + currentTransaction);
//				System.out.println("\t" + "hostComponent: " + hostComponent);
//				System.out.println("\t" + "txStatus: " + TransactionSnapshot.START);
				ArcService arcService;
				try {
					arcService = commNode.getService(
							ArcService.class, targetEndpoint);
					arcService.notifySubTxStatus(parentTransaction, 
							currentTransaction, hostComponent, TransactionSnapshot.START);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				}
			}//END IF
			
			//If ComponentStatus isn't NORMAL/ON_DEMAND, arcs need to be maintained.
			String currentStatus = componentStatus.getCurrentStatus();
			
			LOGGER.info(componentStatus.getComponentName() + "'s status: " + currentStatus);
//			System.out.println(componentStatus.getComponentName() + "'s status: " + currentStatus);
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
//						System.out.println("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
//						Thread.currentThread().wait();
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
//				System.out.println("wait() is done, and ComponentStatus is " + 
//						componentStatus.getCurrentStatus());
			}
			if(!componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
				&& !componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				VcAlgorithmImpl vcAlgorithm = 
						new VcAlgorithmImpl(VcContainerImpl.getInstance());
				vcAlgorithm.analyze(transactionStatus, threadID, futureC, pastC);
			}
			
			LOGGER.info(">>>>In ComponentListenerImpl.notify(start, ,...)");
//			System.out.println(">>>>In ComponentListenerImpl.notify(start, ,...)");
//			ContainerPrinter containerPrinter = new ContainerPrinter();
			containerPrinter.printInArcRegistry(inArcRegistry);
			containerPrinter.printOutArcRegistry(outArcRegistry);
			containerPrinter.printTransactionRegistry(transactionRegistry);
			LOGGER.info("<<<<In ComponentListenerImpl.notify(start, ,...)");
//			System.out.println("<<<<In ComponentListenerImpl.notify(start, ,...)");
			
		} else if (transactionStatus.equals("running")) {
			currentTransaction = cache.getDependency(threadID).getCurrentTx();
			rootTransaction = transactionRegistry.getDependency(currentTransaction).getRootTx();
			
			//update TransactionRegistry
			TransactionRegistry txRegistry;
			TransactionDependency tmpDependency;
			txRegistry = TransactionRegistryImpl.getInstance();
			tmpDependency = txRegistry.getDependency(currentTransaction);
			tmpDependency.setStatus(TransactionSnapshot.RUNNING);
			tmpDependency.setFutureComponents(futureC);
			tmpDependency.setPastComponents(pastC);
			
			//If ComponentStatus isn't NORMAL/ON_DEMAND, arcs need to be maintained.
			String currentStatus = componentStatus.getCurrentStatus();
			LOGGER.info(componentStatus.getComponentName() + "'s status: " + currentStatus);
//			System.out.println(componentStatus.getComponentName() + "'s status: " + currentStatus);
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
//						System.out.println("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
//						Thread.currentThread().wait();
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
//				System.out.println("wait() is done, and ComponentStatus is " + 
//						componentStatus.getCurrentStatus());
			}
			if(!componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
				&& !componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				VcAlgorithmImpl vcAlgorithm = 
						new VcAlgorithmImpl(VcContainerImpl.getInstance());
				vcAlgorithm.analyze(transactionStatus, threadID, futureC, pastC);
			}
			LOGGER.info(">>>>In ComponentListenerImpl.notify(running , ,...)");
//			System.out.println(">>>>In ComponentListenerImpl.notify(running , ,...)");
			ContainerPrinter containerPrinter = new ContainerPrinter();
			containerPrinter.printInArcRegistry(inArcRegistry);
			containerPrinter.printOutArcRegistry(outArcRegistry);
			containerPrinter.printTransactionRegistry(transactionRegistry);
			LOGGER.info("<<<<In ComponentListenerImpl.notify(running , ,...)");
//			System.out.println("<<<<In ComponentListenerImpl.notify(running , ,...)");
			
		} else { // transactionStatus.equals("end")
			currentTransaction = cache.getDependency(threadID).getCurrentTx();
			rootTransaction = transactionRegistry.
					getDependency(currentTransaction).getRootTx();
			
			//update TransactionRegistry
			TransactionRegistry txRegistry;
			TransactionDependency tmpDependency;
			txRegistry = TransactionRegistryImpl.getInstance();
			tmpDependency = txRegistry.getDependency(currentTransaction);
			tmpDependency.setStatus(TransactionSnapshot.END);
			tmpDependency.setFutureComponents(futureC);
			tmpDependency.setPastComponents(pastC);
			
			//notify current transaction's parent that a new sub-tx ends
			if(!rootTransaction.equals(currentTransaction)){
				String targetEndpoint = parentComponent +
						"Comm#service-binding(ArcService/ArcService)";
				LOGGER.info("Try to notify a new sub-tx ends:" +
						"\n\t" + "targetEndpoint: " + targetEndpoint +
						"\n\t" + "parentTransaction: " + parentTransaction +
						"\n\t" + "currentTransaction: " + currentTransaction +
						"\n\t" + "hostComponent: " + hostComponent + 
						"\n\t" + "txStatus: " + TransactionSnapshot.END);
//				System.out.println("Try to notify a new sub-tx ends:");
//				System.out.println("\t" + "targetEndpoint: " + targetEndpoint);
//				System.out.println("\t" + "parentTransaction: " + parentTransaction);
//				System.out.println("\t" + "currentTransaction: " + currentTransaction);
//				System.out.println("\t" + "hostComponent: " + hostComponent);
//				System.out.println("\t" + "txStatus: " + TransactionSnapshot.END);
				ArcService arcService;
				try {
					arcService = commNode.getService(
							ArcService.class, targetEndpoint);
					arcService.notifySubTxStatus(parentTransaction, 
							currentTransaction, hostComponent, TransactionSnapshot.END);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				}
			}//END IF
			
			//If ComponentStatus isn't NORMAL/ON_DEMAND, arcs need to be maintained.
			String currentStatus = componentStatus.getCurrentStatus();
			LOGGER.info(componentStatus.getComponentName() + "'s status: " + currentStatus);
//			System.out.println(componentStatus.getComponentName() + "'s status: " + currentStatus);
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
//						System.out.println("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
//						Thread.currentThread().wait();
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
//				System.out.println("wait() is done, and ComponentStatus is " + 
//						componentStatus.getCurrentStatus());
			}
			if(!componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
				&& !componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				VcAlgorithmImpl vcAlgorithm = 
						new VcAlgorithmImpl(VcContainerImpl.getInstance());
				vcAlgorithm.analyze(transactionStatus, threadID, futureC, pastC);
			} else if(rootTransaction.equals(currentTransaction)){
				//if root transaction ends
				String endpoint;
				endpoint = hostComponent + "Comm#service-binding(ArcService/ArcService)";
				ArcService arcService;
				try {
					arcService = communicationNode.getService(ArcService.class,
								endpoint);
//					System.out.println("clean up in ComponentListenerImpl:" + arcService);
					LOGGER.info("root tx ends, clean up in ComponentListenerImpl:" + arcService);
					arcService.cleanUp(rootTransaction, scope);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				} 
			}
			
			//clean interceptor cache
			InterceptorCache clearCache = InterceptorCacheImpl.getInstance();
			clearCache.removeDependecy(threadID);
			LOGGER.info(">>>>In ComponentListenerImpl.notify(end, ,...)");
//			System.out.println(">>>>In ComponentListenerImpl.notify(end, ,...)");
			ContainerPrinter containerPrinter = new ContainerPrinter();
			containerPrinter.printInArcRegistry(inArcRegistry);
			containerPrinter.printOutArcRegistry(outArcRegistry);
			containerPrinter.printTransactionRegistry(transactionRegistry);
			LOGGER.info("<<<<In ComponentListenerImpl.notify(end, ,...)");
//			System.out.println("<<<<In ComponentListenerImpl.notify(end, ,...)");
			
		}//else

		return true;
	}
	
	private String createTransactionID(){
		String result = null;
		String targetEndpoint = 
				"DomainManagerComponent#service-binding(TransactionIDService/TransactionIDService)";
		TransactionIDService transactionIDService;
		try {
			transactionIDService = vcContainer.getCommunicationNode().getService(
					TransactionIDService.class, targetEndpoint);
			result = transactionIDService.createID();
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
}//END CLASS

