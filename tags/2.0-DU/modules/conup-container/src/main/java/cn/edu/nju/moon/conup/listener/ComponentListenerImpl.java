/**
 * 
 */
package cn.edu.nju.moon.conup.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;
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
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.Scope;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.def.TransactionSnapshot;
import cn.edu.nju.moon.conup.printer.container.ContainerPrinter;


/**
 * ComponentListenerImpl class only has one instance, and provides a global point of access to it.
 * 
 * @author nju
 *
 */
public class ComponentListenerImpl implements ComponentListener{
	private static ComponentListenerImpl instance = new ComponentListenerImpl();
	private final static Logger LOGGER = Logger.getLogger(ComponentListenerImpl.class.getName());
	
	/** 
	 * 	Purpose: 
	 * 		It's used to mark whether a setup is done for a root tx.
	 * 	<KEY, VALUE>:
	 * 		key: root tx ID 
	 * 		value: defalut is false, and true means setup is done.
	 * 	Description:
	 * 		0)	When ComponentListenerImpl realizes a root tx starts, it adds <rootID, false>
	 * 			to the isSetupDone, and when a root tx ends, ComponentListenerImpl removes it;
	 * 		1)	When VcAlgorithmImpl realizes a root's status is "running" for the first time,
	 * 			it will try to set up recursively, and set <rootID, false> to <rootID, true>;
	 * 		2)	Taking on-demand setup into consideration, even current component status is 
	 * 			NORMAL, ComponentListenerImpl needs to set <rootID, false> to <rootID, true>
	 * 			as long as root tx status is "running".
	 * 	Disadvantages:
	 * 		0)	it depends on PingSu's notification strategy, we need an extra notification
	 * 			from PingSu exactly before root tx starts its first sub-tx.
	 * 	
	 *  */
	public static Map<String, Boolean> isSetupDone = new ConcurrentHashMap<String, Boolean>();
	
	public static Map<String,String> ThreadIDs = new ConcurrentHashMap<String, String>();
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	private VcContainer vcContainer;
	private ArcRegistry inArcRegistry;
	private ArcRegistry outArcRegistry;
	private TransactionRegistry transactionRegistry;
	private Node commNode;

	public ComponentListenerImpl(){
	}
	
	
	/** when an event occurs, this method will be called. */
	public boolean notify(String transactionStatus, String threadID, Set<String> futureC, Set<String> pastC){
//		LOGGER.info("\n\ntransaction.status: " + transactionStatus);
//TODO just for suping's test, delete in the future!!!
//		Set<String> futureTempSet = new ConcurrentSkipListSet<String>();
//		Iterator iterator = futureC.iterator();
//		while(iterator.hasNext()){
//			String temp = (String) iterator.next();
//			String[] strs = temp.split("/");
//			futureTempSet.add(strs[0]);
//		}
//		futureC.removeAll(futureC);
//		futureC.addAll(futureTempSet);
//		
//		Set<String> pastTempSet = new ConcurrentSkipListSet<String>();
//		iterator = pastC.iterator();
//		while(iterator.hasNext()){
//			String temp = (String) iterator.next();
//			String[] strs = temp.split("/");
//			pastTempSet.add(strs[0]);
//		}
//		pastC.removeAll(pastC);
//		pastC.addAll(pastTempSet);
		
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
//		for(Entry<String, TransactionDependency> c : cache.getDependencies()){
////			if(c.getKey().equals(threadID)){
//				System.out.println(c.getKey() + "|||| true " + c.getValue());
////				if(c.getValue() == null)
////					System.out.println("c.getValue == null");
////			}
//		}
//		System.out.println("\n\n threadID " + threadID);
		TransactionDependency dependency = cache.getDependency(threadID);
		currentTransaction = dependency.getCurrentTx();
		parentTransaction = dependency.getParentTx();
		rootTransaction = dependency.getRootTx();
		hostComponent = dependency.getHostComponent();
		rootComponent = dependency.getRootComponent();
		parentComponent = dependency.getParentComponent();
		
		boolean isRoot = false;
		
		if (transactionStatus.equals("start")) {
//			assert(!ComponentListenerImpl.threadID.contains(threadID));
			if(rootTransaction==null && parentTransaction==null 
					&& currentTransaction==null && hostComponent!=null){
				//current transaction is root
				isRoot = true;
				currentTransaction = createTransactionID(hostComponent);
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
				currentTransaction = createTransactionID(hostComponent);
				//update interceptor cache dependency
				dependency.setCurrentTx(currentTransaction);
			} else{
				LOGGER.warning("Error: dirty data in InterceptroCache.");
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
			
			//for debug
			String value = "";
			value = ThreadIDs.get(currentTransaction);
			value += threadID + " |||| "+ Thread.currentThread() + "tx start, add to txRegistry, " + "<" + currentTransaction + ", " + rootTransaction + ", " + parentTransaction + ">\n";
			ThreadIDs.put(currentTransaction, value);
			
			ContainerPrinter containerPrinter = new ContainerPrinter();
			
			//setup is not needed immediately when a root transaction starts
			if(isRoot){
				isSetupDone.put(rootTransaction, false);
				LOGGER.info("Transaction status: " + transactionStatus + ", isSetupDone: ");
				printIsSetupDone(isSetupDone);
			}
			
			//notify current transaction's parent that a new sub-tx starts
			if(!isRoot){
				String targetEndpoint = parentComponent +
						"Comm#service-binding(ArcService/ArcService)";
				LOGGER.info("Try to notify a new sub-tx starts:" + 
						"\n\t" + "targetEndpoint: " + targetEndpoint +
						"\n\t" + "parentTransaction: " + parentTransaction +
						"\n\t" + "currentTransaction: " + currentTransaction +
						"\n\t" + "hostComponent: " + hostComponent +
						"\n\t" + "txStatus: " + TransactionSnapshot.START);
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
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
//						Thread.currentThread().wait();
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
			}
			if(!componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
				&& !componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				VcAlgorithmImpl vcAlgorithm = 
						new VcAlgorithmImpl(VcContainerImpl.getInstance());
				vcAlgorithm.analyze(transactionStatus, threadID, futureC, pastC);
			}else{
				LOGGER.info(">>>>In ComponentListenerImpl.notify(start, ,...)");
				containerPrinter.printInArcRegistry(inArcRegistry);
				containerPrinter.printOutArcRegistry(outArcRegistry);
				containerPrinter.printTransactionRegistry(transactionRegistry);
			}
			
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
			
			//for debug
			String value = ThreadIDs.get(currentTransaction);
			value += threadID + " |||| " + Thread.currentThread() +" tx runnng, update txRegistry, " + "<" + currentTransaction + ", " + rootTransaction + ", " + parentTransaction + ">\n";
			ThreadIDs.put(currentTransaction, value);
			
			//If ComponentStatus isn't NORMAL/ON_DEMAND, arcs need to be maintained.
			String currentStatus = componentStatus.getCurrentStatus();
			LOGGER.info(componentStatus.getComponentName() + "'s status: " + currentStatus);
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
			}
			if(!componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
				&& !componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				VcAlgorithmImpl vcAlgorithm = 
						new VcAlgorithmImpl(VcContainerImpl.getInstance());
				vcAlgorithm.analyze(transactionStatus, threadID, futureC, pastC);
			}else{
				LOGGER.info(">>>>In ComponentListenerImpl.notify(running , ,...)");
				ContainerPrinter containerPrinter = new ContainerPrinter();
				containerPrinter.printInArcRegistry(inArcRegistry);
				containerPrinter.printOutArcRegistry(outArcRegistry);
				containerPrinter.printTransactionRegistry(transactionRegistry);
//				LOGGER.info("<<<<In ComponentListenerImpl.notify(running , ,...)");
			}
			
			
			if(componentStatus.getCurrentStatus().equals(ComponentStatus.NORMAL)
					|| componentStatus.getCurrentStatus().equals(ComponentStatus.ON_DEMAND)){
				//maintain isSetupDone
				isSetupDone.put(rootTransaction, true);
				LOGGER.info("Transaction status: " + transactionStatus + ", isSetupDone: ");
				printIsSetupDone(isSetupDone);
			}
			
		} else { // transactionStatus.equals("end")
			
//			if(ComponentListenerImpl.threadID.contains(threadID)){
//				System.err.println("ComponentListenerImpl.threadID.contains(threadID)" + threadID);
//			}else{
//				ComponentListenerImpl.threadID.add(threadID, );
//			}
			
			currentTransaction = cache.getDependency(threadID).getCurrentTx();
			try{
				rootTransaction = transactionRegistry.
						getDependency(currentTransaction).getRootTx();
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
//			rootTransaction = cache.getDependency(threadID).getRootTx();
			
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
				LOGGER.info("Try to notify a sub-tx ends:" +
						"\n\t" + "targetEndpoint: " + targetEndpoint +
						"\n\t" + "parentTransaction: " + parentTransaction +
						"\n\t" + "currentTransaction: " + currentTransaction +
						"\n\t" + "hostComponent: " + hostComponent + 
						"\n\t" + "txStatus: " + TransactionSnapshot.END);
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
			//suspend current thread
			if( currentStatus.equals(ComponentStatus.ON_DEMAND)){
				OndemandThreadBuffer threadBuffer;
				Set<Runnable> threads;
				threadBuffer = OndemandThreadBuffer.getInstance();
				threads = threadBuffer.getThreads();
				synchronized (threadBuffer) {
					try {
						LOGGER.info("ComponentStatus is ON_DEMAND, now wait()...");
						threads.add(Thread.currentThread());
//						Thread.currentThread().wait();
						threadBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LOGGER.info("wait() is done, and ComponentStatus is " + 
						componentStatus.getCurrentStatus());
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
					LOGGER.info("root tx ends, clean up in ComponentListenerImpl:" + arcService);
					arcService.cleanUp(rootTransaction, scope);
				} catch (NoSuchServiceException e) {
					e.printStackTrace();
				} 
				
				//remove root from isSetupDone
				if( !isSetupDone.containsKey(rootTransaction) 
					|| !isSetupDone.get(rootTransaction))
					LOGGER.warning("dirty data found in isSetupDone when cleanup() for root " + rootTransaction);
				isSetupDone.remove(rootTransaction);
				LOGGER.info("Transaction status: " + transactionStatus + ", isSetupDone: ");
				printIsSetupDone(isSetupDone);
			}
			
			//clean interceptor cache
			InterceptorCache clearCache = InterceptorCacheImpl.getInstance();
			clearCache.removeDependecy(threadID);
			LOGGER.info(">>>>In ComponentListenerImpl.notify(end, ,...)");
			ContainerPrinter containerPrinter = new ContainerPrinter();
			containerPrinter.printInArcRegistry(inArcRegistry);
			containerPrinter.printOutArcRegistry(outArcRegistry);
			containerPrinter.printTransactionRegistry(transactionRegistry);
//			LOGGER.info("<<<<In ComponentListenerImpl.notify(end, ,...)");
			
		}//else

		return true;
	}
	
	private synchronized String createTransactionID(String hostComponent){
		
		String result = null;
		
		Random random = new Random(System.currentTimeMillis());
		result = hostComponent + Long.toString(System.currentTimeMillis()) + "." +
				random.nextInt() + Thread.currentThread().hashCode();
		return result;
	}

	public Map<String, Boolean> getIsSetupDone() {
		return isSetupDone;
	}
	
	private void printIsSetupDone(Map<String, Boolean> isSetupDone){
		Iterator<Entry<String, Boolean>> iterator;
		iterator = isSetupDone.entrySet().iterator();
		String tmp = "";
		while(iterator.hasNext()){
			tmp += "\n\t" + iterator.next().toString();
		}
		LOGGER.info(tmp);
	}
	
}//END CLASS

