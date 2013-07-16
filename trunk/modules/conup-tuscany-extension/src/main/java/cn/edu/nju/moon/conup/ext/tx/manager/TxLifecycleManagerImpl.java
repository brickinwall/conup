package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.tuscany.sca.invocation.Message;

import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxDep;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public class TxLifecycleManagerImpl implements TxLifecycleManager {
	
	private static final Logger LOGGER = Logger.getLogger(TxLifecycleManagerImpl.class.getName());
	private static String ROOT_PARENT_IDENTIFIER = "VcTransactionRootAndParentIdentifier";
	private static String HOSTIDENTIFIER = "HostIdentifier";
	private static final String ROOT_TX = "ROOT_TX";
	private static final String ROOT_COMP = "ROOT_COMP";
	private static final String PARENT_TX = "PARENT_TX";
	private static final String PARENT_COMP = "PARENT_COMP";
	private static final String SUB_TX = "SUB_TX";
	private static final String SUB_COMP = "SUB_COMP";
	
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
//		CompLifecycleManager compLcMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		UpdateManager updateMgr = nodeManager.getUpdateManageer(hostComp);
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
//		compLcMgr = CompLifecycleManagerImpl.getInstance(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		LOGGER.fine("txID:" + rootTxId + " hostComp:" + hostComp + " compStatus:" + dynamicDepMgr.getCompStatus());
		synchronized (validToFreeSyncMonitor) {
//			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
//				&& compLcMgr.isDynamicUpdateRqstRCVD()
//				&& compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
			if(updateMgr.isDynamicUpdateRqstRCVD() && updateMgr.getUpdateCtx().isOldRootTxsInitiated()){
				updateMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);

				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)) {
					updateMgr.attemptToUpdate();
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
		TxDepMonitor txDepMonitor = nodeManager.getTxDepMonitor(hostComp);
		TxDepRegistry txDepRegistry = txDepMonitor.getTxDepRegistry();
		TransactionContext subTxCtx;
		
		subTxCtx = new TransactionContext();
		subTxCtx.setFakeTx(true);
		subTxCtx.setCurrentTx(fakeSubTx);
		subTxCtx.setHostComponent(hostComp);
		subTxCtx.setEventType(TxEventType.TransactionStart);
//		subTxCtx.setFutureComponents(new HashSet<String>());
//		subTxCtx.setPastComponents(new HashSet<String>());
		TxDep txDep = new TxDep(new HashSet<String>(), new HashSet<String>());
		txDepRegistry.addLocalDep(fakeSubTx, txDep);
		subTxCtx.setParentComponent(parentComp);
		subTxCtx.setParentTx(parentTx);
		subTxCtx.setRootTx(rootTx);
		subTxCtx.setRootComponent(rootComp);
		
		depMgr.getTxs().put(fakeSubTx, subTxCtx);
		
		return depMgr.initLocalSubTx(subTxCtx);
//		return depMgr.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
	}

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

	@Override
	public TransactionRegistry getTxRegistry() {
		return txRegistry;
	}

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	@Override
	public Message traceServicePhase(Message msg, String transactionTag,
			List<Object> msgBody, String hostComponent) {
		String rootTx = null;
		String rootComponent = null;
		String parentTx = null;
		String parentComponent = null;
		String threadID = null;
		
		String subTx = null;
		String subComp = null;
		
		if(transactionTag==null || transactionTag.equals("")){
			//an exception is preferred
			LOGGER.warning("Error: message body cannot be null in a service body");
		}
		
		LOGGER.fine("trace SERVICE_POLICY : " + transactionTag);
		
		//get root, parent and current transaction id
		if(transactionTag != null){
			String target = getTargetString(transactionTag);
			String[] txInfos = target.split(","); 
			String rootInfo = txInfos[0];
			String parentInfo = txInfos[1];
			String subInfo = txInfos[2];
			
			String[] rootInfos = rootInfo.split(":");
			rootTx = rootInfos[0].equals("null") ? null : rootInfos[0];
			rootComponent = rootInfos[1].equals("null") ? null : rootInfos[1];
			
			String[] parentInfos = parentInfo.split(":");
			parentTx = parentInfos[0].equals("null") ? null : parentInfos[0];
			parentComponent = parentInfos[1].equals("null") ? null : parentInfos[1];
			
			String[] subInfos = subInfo.split(":");
			subTx = subInfos[0].equals("null") ? null : subInfos[0];
			subComp = subInfos[1].equals("null") ? null : subInfos[1];
			
		}
		
		Map<String, Object> headers = msg.getHeaders();
		if(rootTx!=null && rootComponent!=null){
			headers.put(ROOT_TX, rootTx);
			headers.put(ROOT_COMP, rootComponent);
			
			assert parentTx != null;
			assert parentComponent != null;
			assert subTx != null;
			assert subComp != null;
			
			headers.put(PARENT_TX, parentTx);
			headers.put(PARENT_COMP, parentComponent);
			
			headers.put(SUB_TX, subTx);
			headers.put(SUB_COMP, subComp);
		}
		
		// check interceptor cache
		InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
		threadID = getThreadID();
		TransactionContext txContext = cache.getTxCtx(threadID);
		if(txContext == null){
			// generate and init TransactionDependency
			txContext = new TransactionContext();
			txContext.setCurrentTx(null);
//			txContext.setHostComponent(hostComponent);
			txContext.setParentTx(parentTx);
			txContext.setParentComponent(parentComponent);
			txContext.setRootTx(rootTx);
			txContext.setRootComponent(rootComponent);
			//add to InterceptorCacheImpl
			cache.addTxCtx(threadID, txContext);
		} 
		txContext.setHostComponent(hostComponent);
//		else{
//			txContext.setHostComponent(hostComponent);
//		}
		
		String hostInfo = HOSTIDENTIFIER + "," + hostComponent;
		msgBody.add(hostInfo);
		msg.setBody((Object [])msgBody.toArray());
		return msg;
	}

	@Override
	public Message traceReferencePhase(Message msg, String transactionTag,
			List<Object> msgBody, String hostComponent, String serviceName, TxDepMonitor txDepMonitor) {
		String currentTx = null;
//		String hostComponent = null;
		String rootTx = null;
		String rootComponent = null;
		String parentTx = null;
		String parentComponent = null;
		String threadID = null;
		String subTx = null;
		String subComp = null;
		
//		hostComponent = getComponent().getName();
		//get root and parent id from InterceptorCacheImpl
		InterceptorCache cache = InterceptorCache.getInstance(hostComponent);
		threadID = getThreadID();
		TransactionContext txContext = cache.getTxCtx(threadID);
		if(txContext == null){	//the invoked transaction is a root transaction 
			currentTx = null;
			hostComponent = null;
			parentTx = null;
			parentComponent = null;
			rootTx = null;
			rootComponent = null;
			subTx = null;
			subComp = null;
		} else{
			rootTx = txContext.getRootTx();
			rootComponent = txContext.getRootComponent();
			currentTx = txContext.getCurrentTx();
			hostComponent = txContext.getHostComponent();
			parentTx = currentTx;
			parentComponent = hostComponent;
			
			subTx = createFakeTxId();
			subComp = txDepMonitor.convertServiceToComponent(serviceName, hostComponent);
//			subComp = txDepMonitor.convertServiceToComponent(getTargetServiceName(), hostComponent);
			
			assert subComp != null;
			
			startRemoteSubTx(subComp, hostComponent, rootTx, parentTx, subTx);
		}//else(dependency != null)
		
		//generate transaction tag(identifier)
		String newRootParent;
		newRootParent = ROOT_PARENT_IDENTIFIER + 
				"[" + rootTx + ":" + rootComponent + 
				"," + parentTx + ":" + parentComponent + 
				"," + subTx + ":" + subComp +
				"]";
		StringBuffer buffer = new StringBuffer();
		buffer.append(newRootParent);
		msgBody.add(buffer.toString());
		msg.setBody((Object [])msgBody.toArray());
		
		LOGGER.fine("trace REFERENCE_POLICY : " + newRootParent);
		return msg;
	}
	
	/**
	 * root and parent transaction id is stored in the format: VcTransactionRootAndParentIdentifier[ROOT_ID,PARENT_ID].
	 * 
	 * @return ROOT_ID,PARENT_ID
	 * 
	 * */
	private String getTargetString(String raw){
		if(raw == null){
			return null;
		}
		if(raw.startsWith("\"")){
			raw = raw.substring(1);
		}
		if(raw.endsWith("\"")){
			raw = raw.substring(0, raw.length()-1);
		}
		int index = raw.indexOf(ROOT_PARENT_IDENTIFIER);
		int head = raw.substring(index).indexOf("[")+1;
//		LOGGER.fine(raw.substring(0, head));
		int tail = raw.substring(index).indexOf("]");
//		LOGGER.fine(raw.substring(head, tail));
		return raw.substring(head, tail);
	}
	
}
