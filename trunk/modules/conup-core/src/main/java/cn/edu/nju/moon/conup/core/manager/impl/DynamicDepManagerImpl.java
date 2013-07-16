package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tuscany.sca.invocation.Message;

import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.BufferEventType;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;
import cn.edu.nju.moon.conup.spi.pubsub.Subject;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;
import cn.edu.nju.moon.conup.spi.utils.Printer;

/**
 * For managing/maintaining transactions and dependences
 * @author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class DynamicDepManagerImpl implements DynamicDepManager {
	private Logger LOGGER = Logger.getLogger(DynamicDepManagerImpl.class.getName());
	
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private BufferEventType bufferEventType = BufferEventType.NOTHING;
	private Algorithm algorithm = null;
	private ComponentObject compObj;
	private CompStatus compStatus = CompStatus.NORMAL;
	private Scope scope = null;
	/** dependences by other components */
	private DependenceRegistry inDepRegistry = new DependenceRegistry();
	/** dependences to other components */
	private DependenceRegistry outDepRegistry = new DependenceRegistry();
	/** transactions hosted by current component */
//	private TransactionRegistry txRegistry = new TransactionRegistry();
//	/** */
//	private TransactionRegistry fakeTxRegistry = new TransactionRegistry();
	/** used to identify whether received update request */
	private boolean isUpdateRequestReceived = false;
	
	private Object ondemandSyncMonitor = new Object();
	
	private Object validToFreeSyncMonitor = new Object();
	
	private Object updatingSyncMonitor = new Object();
	
	private Object freezeSyncMonitor = new Object();
	
	/**
	 * semaphore which is used to synchronize the re
	 */
	private Object waitingRemoteCompUpdateDoneMonitor = new Object();
	
//	private TxDepMonitor txDepMonitor = null;
	private TxLifecycleManager txLifecycleMgr = null;
	
	private TransactionRegistry txRegistry = null;

	public DynamicDepManagerImpl() {
	}

	/**
	 * maintain tx
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	@Override
	public boolean manageTx(TransactionContext txContext) {
		LOGGER.fine("DynamicDepManagerImpl.manageTx(...)");
		String currentTxID = txContext.getCurrentTx();
		if (!txRegistry.contains(currentTxID)) {
			txRegistry.addTransactionContext(currentTxID, txContext);
		} else {
			// if this tx id already in txRegistry, update it...
			txRegistry.updateTransactionContext(currentTxID, txContext);
		}
		
		return manageDependence(txContext);
	}

	/**
	 * maintain dependences, e.g., arcs
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	@Override
	public boolean manageDependence(TransactionContext txContext) {
		LOGGER.fine("DynamicDepManagerImpl.manageDependence(...)");
		
		algorithm.manageDependence(txContext);
		return true;
	}
	
	@Override
	public boolean isInterceptRequired() {
//		if(compStatus.equals(CompStatus.Free) || compStatus.equals(CompStatus.UPDATING)){
		if(compStatus.equals(CompStatus.UPDATING)){
			return true;
		}
		return false;
	}

	@Override
	public boolean manageDependence(String proctocol, String payload) {
		return algorithm.manageDependence(payload);
	}
	
	@Override
	public boolean isNormal(){
		return compStatus.equals(CompStatus.NORMAL);
	}

	@Override
	public boolean isValid() {
		return compStatus.equals(CompStatus.VALID);
	}

	@Override
	public boolean isReadyForUpdate() {
		boolean algReadyForUpdate = algorithm.isReadyForUpdate(compObj.getIdentifier());
		LOGGER.info("algReadyForUpdate:" + algReadyForUpdate + " compStatus.equals(CompStatus.VALID): " + compStatus.equals(CompStatus.VALID));
		return compStatus.equals(CompStatus.VALID) && algReadyForUpdate;
	}

	@Override
	public Scope getScope() {
		return scope;
	}

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}

	public DependenceRegistry getInDepRegistry() {
		return inDepRegistry;
	}

	public void setInDepRegistry(DependenceRegistry inDepRegistry) {
		this.inDepRegistry = inDepRegistry;
	}

	public DependenceRegistry getOutDepRegistry() {
		return outDepRegistry;
	}

	public void setOutDepRegistry(DependenceRegistry outDepRegistry) {
		this.outDepRegistry = outDepRegistry;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	@Override
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public CompStatus getCompStatus() {
		return compStatus;
	}

	public void ondemandSetting() {
		synchronized (ondemandSyncMonitor) {
			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.ONDEMAND))
				System.out.println(compObj.getIdentifier() + "--> compStatus:" + compStatus);
			assert compStatus.equals(CompStatus.NORMAL) || compStatus.equals(CompStatus.ONDEMAND);
			if(compStatus.equals(CompStatus.NORMAL)){
				this.compStatus = CompStatus.ONDEMAND;
				bufferEventType = BufferEventType.ONDEMAND;
				notifyObservers(bufferEventType);
			}
		}
	}

	@Override
	public boolean isOndemandSetting() {
		synchronized (ondemandSyncMonitor) {
			return compStatus.equals(CompStatus.ONDEMAND);
		}
	}

	@Override
	public Set<String> getAlgorithmOldVersionRootTxs() {
		return algorithm.getOldVersionRootTxs(inDepRegistry.getDependences());
	}
	
	@Override
	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs){
		return algorithm.convertToAlgorithmRootTxs(oldRootTxs);
	}
	
	@Override
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Set<String> getStaticDeps() {
		return compObj.getStaticDeps();
	}

	@Override
	public Set<Dependence> getRuntimeDeps() {
		return outDepRegistry.getDependences();
	}

	@Override
	public Set<Dependence> getRuntimeInDeps() {
		return inDepRegistry.getDependences();
	}

	@Override
	public Map<String, TransactionContext> getTxs() {
		return txRegistry.getTransactionContexts();
	}
	
	/**
	 * 
	 * @return transactions that are launched by another component, 
	 * and its's tx id is also generated by that component
	 */
//	@Override
//	public Map<String, TransactionContext> getFakeTxs(){
//		return fakeTxRegistry.getTransactionContexts();
//	}

	@Override
	public boolean isOndemandSetupRequired() {
		return compStatus.equals(CompStatus.NORMAL) 
				|| compStatus.equals(CompStatus.ONDEMAND);
	}

	@Override
	public void ondemandSetupIsDone() {
		
		//FOR TEST
		String inDepsStr = "";
		for (Dependence dep : inDepRegistry.getDependences()) {
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.info("ondemandSetupIsDone, inDepsStr:" + inDepsStr);
		
		String outDepsStr = "";
		for (Dependence dep : outDepRegistry.getDependences()) {
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.info("ondemandSetupIsDone, outDepsStr:" + outDepsStr);
//		
		Printer printer = new Printer();
		LOGGER.info("ondemandSetupIsDone, Txs:");
		printer.printTxs(LOGGER, getTxs());
		
		synchronized (ondemandSyncMonitor) {
			//FOR TEST
//			ExecutionRecorder exeRecorder;
//			exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
//			exeRecorder.ondemandIsDone();
//			
//			compStatus = CompStatus.VALID;
//			OndemandSetupHelper ondemandSetupHelper = NodeManager.getInstance().getOndemandSetupHelper(compObj.getIdentifier());
//			ondemandSetupHelper.resetIsOndemandRqstRcvd();
//			
//			LOGGER.info("--------------ondemand setup is done, now notify all...------\n\t compStatus:" + compStatus);
//			ondemandSetupHelper.onDemandIsDone();
//			ondemandSyncMonitor.notifyAll();
//			if(isUpdateRequestReceived){
//				algorithm.initiate(compObj.getIdentifier());
//			}
			
			assert compStatus.equals(CompStatus.ONDEMAND) || compStatus.equals(CompStatus.VALID);
			if(compStatus.equals(CompStatus.ONDEMAND)){
				//FOR TEST
				ExecutionRecorder exeRecorder;
				exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
				exeRecorder.ondemandIsDone();
				
				compStatus = CompStatus.VALID;
				if(isUpdateRequestReceived){
					bufferEventType = BufferEventType.VALIDTOFREE;
//					CompLifecycleManager clMgr = NodeManager.getInstance().getCompLifecycleManager(compObj.getIdentifier());
					UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(compObj.getIdentifier());
					if (!updateMgr.getUpdateCtx().isOldRootTxsInitiated()) {
						updateMgr.initOldRootTxs();
//						Printer printer = new Printer();
						printer.printTxs(LOGGER, getTxs());
						
					}
					
				} else {
					bufferEventType = BufferEventType.WAITFORREMOTEUPDATE;
				}
				notifyObservers(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				OndemandSetupHelper ondemandSetupHelper = NodeManager.getInstance().getOndemandSetupHelper(compObj.getIdentifier());
				ondemandSetupHelper.resetIsOndemandRqstRcvd();
				
				LOGGER.info("-------------- " + compObj.getIdentifier() + "ondemand setup is done, now notify all...------\n\n");
				ondemandSetupHelper.onDemandIsDone();
				ondemandSyncMonitor.notifyAll();
				
				if(isUpdateRequestReceived)
					algorithm.initiate(compObj.getIdentifier());
			}
		}
	}

	@Override
	public void dynamicUpdateIsDone() {
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.updateIsDone();
		
//		String inDepsStr = "";
//		for (Dependence dep : inDepRegistry.getDependences()) {
//			inDepsStr += "\n" + dep.toString();
//		}
//		LOGGER.fine("achievedFree, inDepsStr:" + inDepsStr);
//		
//		String outDepsStr = "";
//		for (Dependence dep : outDepRegistry.getDependences()) {
//			outDepsStr += "\n" + dep.toString();
//		}
//		LOGGER.fine("achievedFree, outDepsStr:" + outDepsStr);
//		
//		Printer printer = new Printer();
//		LOGGER.fine("achievedFree, Txs:");
//		printer.printTxs(LOGGER, getTxs());
		
		synchronized (updatingSyncMonitor) {
			//before changing to NORMAL, compStatus is supposed to be UPDATING.
			LOGGER.info("-----------" + "CompStatus: " + compStatus + " -> NORMAL" + ", dynamic update is done, now notify all...\n\n");
			isUpdateRequestReceived = false;
			compStatus = CompStatus.NORMAL;
			bufferEventType = BufferEventType.NOTHING;
			notifyObservers(bufferEventType);
//			System.out.println("in ddm:" + bufferEventType);
			
			updatingSyncMonitor.notifyAll();
			
			algorithm.updateIsDone(compObj.getIdentifier());
		}
		
	}
	
	@Override
	public void updating(){
		LOGGER.fine("Executing dynamic update for component '" + compObj.getIdentifier() + "'");
		compStatus = CompStatus.UPDATING;
	}
	
	@Override
	public void achievedFree(){
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.achievedFree();
		
//		String inDepsStr = "";
//		for (Dependence dep : inDepRegistry.getDependences()) {
//			inDepsStr += "\n" + dep.toString();
//		}
//		LOGGER.fine("achievedFree, inDepsStr:" + inDepsStr);
//		
//		String outDepsStr = "";
//		for (Dependence dep : outDepRegistry.getDependences()) {
//			outDepsStr += "\n" + dep.toString();
//		}
//		LOGGER.fine("achievedFree, outDepsStr:" + outDepsStr);
//		
//		Printer printer = new Printer();
//		LOGGER.fine("achievedFree, Txs:");
//		printer.printTxs(LOGGER, getTxs());
		
		synchronized (validToFreeSyncMonitor) {
			LOGGER.fine("compStatus: " + compStatus);
			assert compStatus.equals(CompStatus.VALID) || compStatus.equals(CompStatus.Free);
			if(compStatus.equals(CompStatus.VALID)){
				compStatus = CompStatus.Free;
				bufferEventType = BufferEventType.EXEUPDATE;
				notifyObservers(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				LOGGER.info("-----------component has achieved free,now nitify all...\n\n");
				validToFreeSyncMonitor.notifyAll();
			}
		}
	}

	@Override
	public Set<String> getStaticInDeps() {
		return compObj.getStaticInDeps();
	}

	@Override
	public Object getFreezeSyncMonitor() {
		return freezeSyncMonitor;
	}

	@Override
	public Object getOndemandSyncMonitor() {
		return ondemandSyncMonitor;
	}
	
	public Object getValidToFreeSyncMonitor() {
		return validToFreeSyncMonitor;
	}

	public Object getUpdatingSyncMonitor() {
		return updatingSyncMonitor;
	}

	@Override
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD) {
		return algorithm.isBlockRequiredForFree(algorithmOldVersionRootTxs, txContext, isUpdateReqRCVD);
	}

	@Override
	public Object getWaitingRemoteCompUpdateDoneMonitor() {
		return waitingRemoteCompUpdateDoneMonitor;
	}

	@Override
	public void remoteDynamicUpdateIsDone() {
		synchronized (waitingRemoteCompUpdateDoneMonitor) {
//			compStatus = CompStatus.VALID;
			assert compStatus.equals(CompStatus.NORMAL) || compStatus.equals(CompStatus.VALID);
//			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.VALID)){
//				LOGGER.warning("CompStatus is supposed to be NORMAL or VALID, but it's " + compStatus);
//			}
//			LOGGER.info(compObj.getIdentifier() + " receive remote_update_is_done, CompStatus: " + compStatus + ", now notify all");
			if(compStatus.equals(CompStatus.VALID)){
				compStatus = CompStatus.NORMAL;
				bufferEventType = BufferEventType.NOTHING;
				notifyObservers(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				String compIdentifier = compObj.getIdentifier();
				LOGGER.info(compIdentifier + " remote update is done, CompStatus: " + compStatus + ", now notify all");
				waitingRemoteCompUpdateDoneMonitor.notifyAll();
				
				// add for experiments
				// record update cost time
				if(compIdentifier.equals("Coordination"))
					DisruptionExp.getInstance().setUpdateEndTime(System.nanoTime());
			}
		}
	}

	@Override
	public boolean updateIsReceived() {
		LOGGER.info("Received dynamic update request.");
		isUpdateRequestReceived = true;
//		algorithm.initiate(compObj.getIdentifier());
		return true;
	}

	@Override
	public boolean isUpdateRequiredComp() {
		return isUpdateRequestReceived;
	}

	@Override
	public String getAlgorithmRoot(String parentTx, String rootTx) {
		return algorithm.getAlgorithmRoot(parentTx, rootTx);
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx,
			String parentTx, String subTx) {
		return algorithm.notifySubTxStatus(subTxStatus, subComp, curComp, rootTx, parentTx, subTx);
	}
	
//	public TxDepMonitor getTxDepMonitor() {
////		return txDepMonitor;
//		if(txDepMonitor == null)
//			return null;
////		return txDepMonitor.newInstance();
//		return NodeManager.getInstance().getTxDepMonitor(compObj.getIdentifier());
//	}
//
//	public void setTxDepMonitor(TxDepMonitor txDepMonitor) {
//		this.txDepMonitor = txDepMonitor;
//	}

//	@Override
//	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
//		return algorithm.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
//	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext) {
		return algorithm.initLocalSubTx(txContext);
	}

	@Override
	public void dependenceChanged(String hostComp) {
		if(isUpdateRequestReceived){
//			txDepMonitor.checkFreeness(hostComp);
//			NodeManager.getInstance().getCompLifecycleManager(hostComp).checkFreeness(hostComp);
			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(compObj.getIdentifier());
			updateMgr.checkFreeness(hostComp);
		}
	}
	@Override
	public void setTxLifecycleMgr(TxLifecycleManager txLifecycleMgr) {
		this.txLifecycleMgr = txLifecycleMgr;
		this.txRegistry = txLifecycleMgr.getTxRegistry();
	}

	public TxLifecycleManager getTxLifecycleMgr() {
		if(txLifecycleMgr == null){
			txLifecycleMgr = NodeManager.getInstance().getTxLifecycleManager(compObj.getIdentifier());
		}
		assert txLifecycleMgr != null;
		return txLifecycleMgr;
	}

	@Override
	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	@Override
	public void registerObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers(Object arg) {
		for(int i = 0; i < observers.size(); i++ ){
			Observer observer = observers.get(i);
			//System.out.println("in ddm:" + (BufferEventType)arg);
			observer.update(this, arg);
		}
		System.out.println("observers.size():" + observers.size());
	}

	@Override
	public void update(Subject subject, Object arg) {
		RequestObject reqObj = (RequestObject) arg;
		if(reqObj.getMsgType().equals(MsgType.DEPENDENCE_MSG)){
			boolean result = manageDependence(reqObj.getProtocol(), reqObj.getPayload());
			subject.setResult("manageDepResult:" + result);
		}
	}

	@Override
	public void setResult(String result) {
		
	}

	@Override
	public Message checkOndemand(TransactionContext txCtx, Object subTx,
			Interceptor interceptor, Message msg) {
		// waiting during on-demand setup
		synchronized (ondemandSyncMonitor) {
			if (isNormal()) {
				// the invoked transaction is not a root transaction
				if (txCtx.getRootTx() != null) {
					assert txCtx.getParentTx() != null;
					assert txCtx.getParentComponent() != null;
					assert subTx != null;

					txLifecycleMgr.initLocalSubTx(compObj.getIdentifier(),
							subTx.toString(), txCtx.getRootTx(),
							txCtx.getRootComponent(), txCtx.getParentTx(),
							txCtx.getParentComponent());
				}
				return msg;
			}
			
			if(isOndemandSetting()){
				interceptor.freeze(ondemandSyncMonitor);
			}

//			try {
//				if (isOndemandSetting()) {
//					ondemandSyncMonitor.wait();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		return null;
		
	}

	@Override
	public Message checkValidToFree(TransactionContext txCtx, Object subTx,
			Interceptor interceptor, Message msg, UpdateManager updateMgr) {
		String hostComp = compObj.getIdentifier();
		String freenessConf = getCompObject().getFreenessConf();
		FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
		synchronized (validToFreeSyncMonitor) {
			if(getCompStatus().equals(CompStatus.VALID)
				&& updateMgr.getUpdateCtx() != null && updateMgr.getUpdateCtx().isLoaded() ){
				// calculate old version root txs
				if (!updateMgr.getUpdateCtx().isOldRootTxsInitiated()) {
					updateMgr.initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
				}
				if (!freeness.isReadyForUpdate(hostComp)) {
					Class<?> compClass = freeness.achieveFreeness(
							txCtx.getRootTx(), txCtx.getRootComponent(),
							txCtx.getParentComponent(),
							txCtx.getCurrentTx(), hostComp);
					if (compClass != null) {
						addBufferMsgBody(msg, compClass);
					}
				}
				if (freeness.isReadyForUpdate(hostComp)) {
					achievedFree();
				} else if (freeness.isInterceptRequiredForFree(
						txCtx.getRootTx(), hostComp, txCtx, true)) {
					interceptor.freeze(validToFreeSyncMonitor);
//						validToFreeSyncMonitor.wait();
				} else {
				}
			}
		}
		
		return null;
	}
	
	private void addBufferMsgBody(Message msg, Class<?> compClass) {
		String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";
		String className = compClass.getName();
		List<Object> originalMsgBody;
		List<Object> copyOfMsgBody = new ArrayList<Object>();
		originalMsgBody = Arrays.asList((Object [])msg.getBody());
		copyOfMsgBody.addAll(originalMsgBody);
		copyOfMsgBody.add(COMP_CLASS_OBJ_IDENTIFIER + ":" + className);
		copyOfMsgBody.add(compClass);
		msg.setBody((Object [])copyOfMsgBody.toArray());
	}

}
