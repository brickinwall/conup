package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.complifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;

/**
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public class TxLifecycleManagerImpl implements TxLifecycleManager {
	
	private static final Logger LOGGER = Logger.getLogger(TxLifecycleManagerImpl.class.getName());
	/**
	 * TX_IDS takes transactionID and TransactionContext as key and value respectively.
	 */
	public static Map<String, TransactionContext> TX_IDS = new ConcurrentHashMap<String, TransactionContext>();
	
	/**
	 * maintained by trace interceptor and TxLifecycleManager
	 * key : threadID
	 * value is component identifier
	 * it is just used to as a temporary to hold host component name
	 */
//	private Map<String, String> associateTx = new ConcurrentHashMap<String, String>();
	
//	private String compIdentifier = null;
	private ComponentObject compObject = null;
	
	public TxLifecycleManagerImpl(ComponentObject compObject){
		this.compObject = compObject;
	}
	
//	public TxLifecycleManagerImpl(String compIdentifier){
//		this.compIdentifier = compIdentifier;
//	}
	
	@Override
	public String createID(){
		String txID = null;
		
		// use UUID to generate txID
		UUID uuid = UUID.randomUUID();
		txID = uuid.toString();
		
		String threadID = getThreadID();
//		String componentIdentifier = associateTx.get(threadID);
//		assert(componentIdentifier != null);
//		associateTx.remove(threadID);
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
		TX_IDS.put(txID, txContext);
		return txID;
	}
	
	@Override
	public String createFakeTxId(){
		UUID uuid = UUID.randomUUID();
		String txID = uuid.toString();
		return "FAKE_TX_ID" + txID;
	}
	
	@Override
	public void destroyID(String id){
		TX_IDS.remove(id);
	}
	
	@Override
	public int getTxs(){
		return TX_IDS.size();
	}

	/**
	 * invoked by JavaImplementationInvoker
	 * add <threadID, componentIdentifier> To AssociateTx
	 * @param threadID
	 * @param txContext
	 */
//	public void addToAssociateTx(String threadID, String identifier){
//		associateTx.put(threadID, identifier);
//	}

	@Override
	public void rootTxEnd(String hostComp, String rootTxId) {
		CompLifecycleManager compLcMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		compLcMgr = CompLifecycleManagerImpl.getInstance(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		LOGGER.fine("txID:" + rootTxId + " hostComp:" + hostComp + " compStatus:" + dynamicDepMgr.getCompStatus());
		synchronized (validToFreeSyncMonitor) {
//			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
//				&& compLcMgr.isDynamicUpdateRqstRCVD()
//				&& compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
			if(compLcMgr.isDynamicUpdateRqstRCVD() && compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
				compLcMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);

				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)) {
					compLcMgr.attemptToUpdate();
				}
			}
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
	
	@Override
	public boolean startRemoteSubTx(String subComp, String curComp,
			String rootTx, String parentTx, String subTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionStart, 
				subComp, curComp, rootTx, parentTx, subTx);
	}

	@Override
	public boolean endRemoteSubTx(String subComp, String curComp,
			String rootTx, String parentTx, String subTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionEnd, 
				subComp, curComp, rootTx, parentTx, subTx);
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		TransactionContext txCtx;
		
		txCtx = new TransactionContext();
		txCtx.setFakeTx(true);
		txCtx.setCurrentTx(fakeSubTx);
		txCtx.setHostComponent(hostComp);
		txCtx.setEventType(TxEventType.TransactionStart);
		txCtx.setFutureComponents(new HashSet<String>());
		txCtx.setPastComponents(new HashSet<String>());
		txCtx.setParentComponent(parentComp);
		txCtx.setParentTx(parentTx);
		txCtx.setRootTx(rootTx);
		txCtx.setRootComponent(rootComp);
		
		depMgr.getTxs().put(fakeSubTx, txCtx);
		
		return depMgr.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
	}

	@Override
	public boolean endLocalSubTx(String hostComp, String fakeSubTx) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
		
		Object ondemandMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (ondemandMonitor) {
			depMgr.getTxs().remove(fakeSubTx);
		}
		return true;
	}
	
	@Override
	public String getCompIdentifier() {
		return compObject.getIdentifier();
	}

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
}
