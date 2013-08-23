package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxDep;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public class TxLifecycleManagerImpl implements TxLifecycleManager {
	
	private static final Logger LOGGER = Logger.getLogger(TxLifecycleManagerImpl.class.getName());
	
	/** transactions hosted by current component */
	private TransactionRegistry txRegistry = new TransactionRegistry();
	
	private ComponentObject compObject = null;
	
	public TxLifecycleManagerImpl(ComponentObject compObject){
		this.compObject = compObject;
	}
	
	@Override
	public String createID(){
		String txID = null;
		
		// use UUID to generate txID
		UUID uuid = UUID.randomUUID();
		txID = uuid.toString();
		
		String threadID = getThreadID();
		assert(compObject.getIdentifier() != null);
		
		
		// get info from interceptor cache
		// according threadID 
		InterceptorCache interceptorCache = InterceptorCache.getInstance(compObject.getIdentifier());
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		String parentTx = txContextInCache.getParentTx();
		String currentTx = txContextInCache.getCurrentTx();
		String hostComponent = txContextInCache.getHostComponent();
		String rootComponent = txContextInCache.getRootComponent();
		String parentComponent = txContextInCache.getParentComponent();
		
		// associate currentTxId with parent/root
		if(rootTx==null && parentTx==null 
				&& currentTx==null && hostComponent!=null){
			//current transaction is root
			assert(rootTx==null && parentTx==null && currentTx==null && hostComponent!=null);
			currentTx = txID;
			rootTx = currentTx;
			parentTx = currentTx;
			//update interceptor cache transactionContext
			txContextInCache.setCurrentTx(currentTx);
			txContextInCache.setParentTx(parentTx);
			txContextInCache.setRootTx(rootTx);
			rootComponent = hostComponent;
			parentComponent = hostComponent;
			txContextInCache.setHostComponent(hostComponent);
			txContextInCache.setRootComponent(rootComponent);
			txContextInCache.setParentComponent(parentComponent);
			LOGGER.fine("created root tx id:" + rootTx);
//		} else if(rootTx!=null && parentTx!=null 
//				&& currentTx==null && hostComponent!=null){
		} else if(rootTx!=null && parentTx!=null 
				&& hostComponent!=null){
			assert(rootTx!=null && parentTx!=null && hostComponent!=null);
			//current transaction is a sub-transaction
			//update interceptor cache dependency
			currentTx = txID;
			txContextInCache.setCurrentTx(currentTx);
		} else{
			LOGGER.warning("Error: dirty data in InterceptroCache, " +
					"because [rootTx, parentTx, currentTx, hostComp] = " + rootTx + ", " +
					parentTx + ", " + currentTx + ", " + hostComponent);
		}
		
		/**
		 * add new txContext to TX_IDS
		 */
		TransactionContext txContext = new TransactionContext();
		txContext.setCurrentTx(currentTx);
		txContext.setHostComponent(hostComponent);
		txContext.setParentComponent(parentComponent);
		txContext.setRootComponent(rootComponent);
		txContext.setParentTx(parentTx);
		txContext.setRootTx(rootTx);
//		TX_IDS.put(txID, txContext);
		txRegistry.addTransactionContext(txID, txContext);
		return txID;
	}
	
	@Override
	public String createFakeTxId(){
		UUID uuid = UUID.randomUUID();
		String txID = uuid.toString();
		return "FAKE_TX_ID" + txID;
	}
	
	@Override
	public void destroyID(String txId){
//		TX_IDS.remove(id);
		txRegistry.removeTransactionContext(txId);
	}
	
	@Override
	public int getTxs(){
//		return TX_IDS.size();
		return txRegistry.getTransactionContexts().size();
	}

	@Override
	public void rootTxEnd(String hostComp, String rootTxId) {
		CompLifeCycleManager compLifeCycleMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		UpdateManager updateMgr = nodeManager.getUpdateManageer(hostComp);
//		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		compLifeCycleMgr = nodeManager.getCompLifecycleManager(hostComp);
		
		Object validToFreeSyncMonitor = compLifeCycleMgr.getCompObject().getValidToFreeSyncMonitor();
		LOGGER.fine("txID:" + rootTxId + " hostComp:" + hostComp + " compStatus:" + compLifeCycleMgr.getCompStatus());
		synchronized (validToFreeSyncMonitor) {
			updateMgr.removeAlgorithmOldRootTx(rootTxId);
//			if(updateMgr.isDynamicUpdateRqstRCVD() && updateMgr.getUpdateCtx().isOldRootTxsInitiated()){
//				updateMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);
//
//				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);
//
//				if (compLcMgr.isValid()) {
//					updateMgr.attemptToUpdate();
//				}
//			}
		}
		
//		ExecutionRecorder exeRecorder;
//		exeRecorder = ExecutionRecorder.getInstance(hostComp);
//		String completeAction = exeRecorder.getCompleteAction(rootTxId);
		
//		if(completeAction == null || completeAction.equals("null")){
//			if(TxLifecycleManager.getRootTx(hostComp, rootTxId)  != null)
//				completeAction = exeRecorder.getCompleteAction(TxLifecycleManager.getRootTx(hostComp, rootTxId));
//		}
//		if(completeAction != null){
//			LOGGER.info(completeAction);
//		}
//		
//		//when a root tx ends, remove it from TxLifecycleManager
//		if(TxLifecycleManager.getRootTx(hostComp, rootTxId) != null){
//			rootTxId = TxLifecycleManager.getRootTx(hostComp, rootTxId);
//		}
//		TxLifecycleManager.removeRootTx(hostComp, rootTxId);
		LOGGER.fine("In TxDepMonitorImpl, removed rootTxId " + rootTxId);
	}
	
	

//	@Override
//	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
//		NodeManager nodeManager = NodeManager.getInstance();
//		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
//		TxDepMonitor txDepMonitor = nodeManager.getTxDepMonitor(hostComp);
//		TxDepRegistry txDepRegistry = txDepMonitor.getTxDepRegistry();
//		TransactionContext subTxCtx;
//		
//		subTxCtx = new TransactionContext();
//		subTxCtx.setFakeTx(true);
//		subTxCtx.setCurrentTx(fakeSubTx);
//		subTxCtx.setHostComponent(hostComp);
//		subTxCtx.setEventType(TxEventType.TransactionStart);
////		subTxCtx.setFutureComponents(new HashSet<String>());
////		subTxCtx.setPastComponents(new HashSet<String>());
//		TxDep txDep = new TxDep(new HashSet<String>(), new HashSet<String>());
//		txDepRegistry.addLocalDep(fakeSubTx, txDep);
//		subTxCtx.setParentComponent(parentComp);
//		subTxCtx.setParentTx(parentTx);
//		subTxCtx.setRootTx(rootTx);
//		subTxCtx.setRootComponent(rootComp);
//		
//		depMgr.getTxs().put(fakeSubTx, subTxCtx);
//		
//		return depMgr.initLocalSubTx(subTxCtx);
////		return depMgr.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
//	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, TransactionContext txCtxInCache) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		TxDepMonitor txDepMonitor = nodeManager.getTxDepMonitor(hostComp);
		TxDepRegistry txDepRegistry = txDepMonitor.getTxDepRegistry();
		
		String parentComp = txCtxInCache.getParentComponent();
		String parentTx = txCtxInCache.getParentTx();
		String rootTx = txCtxInCache.getRootTx();
		String rootComp = txCtxInCache.getRootComponent();
		
		TransactionContext txCtx;
		txCtx = new TransactionContext();
		txCtx.setFakeTx(true);
		txCtx.setCurrentTx(fakeSubTx);
		txCtx.setHostComponent(hostComp);
		txCtx.setEventType(TxEventType.TransactionStart);
//		txCtx.setFutureComponents(new HashSet<String>());
//		txCtx.setPastComponents(new HashSet<String>());
		TxDep txDep = new TxDep(new HashSet<String>(), new HashSet<String>());
		txDepRegistry.addLocalDep(fakeSubTx, txDep);
		
		txCtx.setParentComponent(parentComp);
		txCtx.setParentTx(parentTx);
		txCtx.setRootTx(rootTx);
		txCtx.setRootComponent(rootComp);
		
		depMgr.getTxs().put(fakeSubTx, txCtx);
		
		return depMgr.initLocalSubTx(txCtx);
	}

	@Override
	public boolean endLocalSubTx(String hostComp, String fakeSubTx) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
		CompLifeCycleManager compLifeCycleMgr = nodeMgr.getCompLifecycleManager(hostComp);
		
		Object ondemandMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandMonitor) {
			depMgr.getTxs().remove(fakeSubTx);
		}
		return true;
	}
	
	@Override
	public String getCompIdentifier() {
		return compObject.getIdentifier();
	}

	@Override
	public TransactionRegistry getTxRegistry() {
		return txRegistry;
	}
	
	@Override
	public void resolveInvocationContext(InvocationContext invocationContext, String hostComponent) {
		InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
		String threadID = getThreadID();
		TransactionContext txContext = cache.getTxCtx(threadID);
		if(txContext == null){
			// generate and init TransactionDependency
			txContext = new TransactionContext();
			txContext.setCurrentTx(null);
			txContext.setParentTx(invocationContext.getParentTx());
			txContext.setParentComponent(invocationContext.getParentComp());
			txContext.setRootTx(invocationContext.getRootTx());
			txContext.setRootComponent(invocationContext.getRootComp());
			//add to InterceptorCacheImpl
			cache.addTxCtx(threadID, txContext);
		} 
		txContext.setHostComponent(hostComponent);
//		else{
//			txContext.setHostComponent(hostComponent);
//		}
	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	@Override
	public InvocationContext createInvocationCtx(String hostComponent, String serviceName,
			TxDepMonitor txDepMonitor) {
		
		InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
		String threadID = getThreadID();
		TransactionContext txContext = cache.getTxCtx(threadID);
		InvocationContext invocationCtx = null;
		
		if(txContext == null){	//the invoked transaction is a root transaction 
			invocationCtx = new InvocationContext(null, null, null, null, null, null);
		} else{
			String rootTx = txContext.getRootTx();
			String rootComp = txContext.getRootComponent();
			String currentTx = txContext.getCurrentTx();
			String parentTx = currentTx;
			String parentComponent = hostComponent;
			
			String subTx = createFakeTxId();
			String subComp = txDepMonitor.convertServiceToComponent(serviceName, hostComponent);
			assert subComp != null;
			invocationCtx = new InvocationContext(rootTx, rootComp, parentTx, parentComponent, subTx, subComp);
			
//			startRemoteSubTx(subComp, hostComponent, rootTx, parentTx, subTx);
			startRemoteSubTx(invocationCtx);
		}//else(dependency != null)
		return invocationCtx;
		
	}

	private boolean startRemoteSubTx(InvocationContext invocationCtx) {
		// invocationCtx is going to send to sub component 
		// so here parent component is host component
		String hostComp = invocationCtx.getParentComp();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		CompLifeCycleManager compLifeCycleMgr = nodeManager.getCompLifecycleManager(hostComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionStart, invocationCtx, compLifeCycleMgr);
	}

//	@Override
//	public boolean endRemoteSubTx(String subComp, String curComp,
//			String rootTx, String parentTx, String subTx) {
//		NodeManager nodeManager = NodeManager.getInstance();
//		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
//		
//		return depMgr.notifySubTxStatus(TxEventType.TransactionEnd, 
//				subComp, curComp, rootTx, parentTx, subTx);
//	}

	@Override
	public boolean endRemoteSubTx(InvocationContext invocationCtx) {
		String hostComp = invocationCtx.getParentComp();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		CompLifeCycleManager compLifeCycleMgr = nodeManager.getCompLifecycleManager(hostComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionEnd, invocationCtx, compLifeCycleMgr);
	}

//	public boolean startRemoteSubTx(String subComp, String curComp,
//			String rootTx, String parentTx, String subTx) {
//		NodeManager nodeManager = NodeManager.getInstance();
//		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
//		
//		return depMgr.notifySubTxStatus(TxEventType.TransactionStart, 
//				subComp, curComp, rootTx, parentTx, subTx);
//	}
}
