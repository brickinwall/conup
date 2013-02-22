package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.core.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

/**
 * For managing/maintaining transactions and dependences
 * @author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class DynamicDepManagerImpl implements DynamicDepManager {
	private Logger LOGGER = Logger.getLogger(DynamicDepManagerImpl.class.getName());
	private Algorithm algorithm = null;
	private ComponentObject compObj;
	private CompStatus compStatus = CompStatus.NORMAL;
	private Scope scope = null;
	/** dependences by other components */
	private DependenceRegistry inDepRegistry = new DependenceRegistry();
	/** dependences to other components */
	private DependenceRegistry outDepRegistry = new DependenceRegistry();
	/** transactions hosted by current component */
	private TransactionRegistry txRegistry = TransactionRegistry.getInstance();
	/** used to identify whether received update request */
	private boolean isUpdateRequestReceived = false;
	
	private Object ondemandSyncMonitor = new Object();
	
	private Object validToFreeSyncMonitor = new Object();
	
	private Object updatingSyncMonitor = new Object();
	
	/**
	 * 
	 */
	private Object waitingRemoteCompUpdateDoneMonitor = new Object();

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
		return compStatus.equals(CompStatus.VALID) && algorithm.isReadyForUpdate(compObj.getIdentifier());
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

	public void setCompStatus(CompStatus compStatus) {
		synchronized (this) {
			this.compStatus = compStatus;
		}
	}

	@Override
	public boolean isOndemandSetting() {
		return compStatus.equals(CompStatus.ONDEMAND);
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

	@Override
	public boolean isOndemandSetupRequired() {
		return compStatus.equals(CompStatus.NORMAL) 
				|| compStatus.equals(CompStatus.ONDEMAND);
	}

	@Override
	public void ondemandSetupIsDone() {
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.ondemandIsDone();
		
		compStatus = CompStatus.VALID;
		synchronized (ondemandSyncMonitor) {
			LOGGER.fine("--------------ondemand setup is done, now notify all...------\n\n");
			ondemandSyncMonitor.notifyAll();
			
			String inDepsStr = "In Dynamic dep manager, ondemandSetupIsDone(), print Dep infos:\n";
			for (Dependence dep : inDepRegistry.getDependences()) {
				inDepsStr += "\n" + dep.toString();
			}
			LOGGER.fine("inDepsStr:" + inDepsStr);
			String outDepsStr = "";

			for (Dependence dep : outDepRegistry.getDependences()) {
				outDepsStr += "\n" + dep.toString();
			}
			LOGGER.fine("outDepsStr:" + outDepsStr);
			
		}
	}

	@Override
	public void dynamicUpdateIsDone() {
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.updateIsDone();
//		synchronized (updatingSyncMonitor) {
//			compStatus = CompStatus.VALID;
//			System.out.println("-----------dynamic update is done, now notify all...\n\n");
//			updatingSyncMonitor.notifyAll();
//		}
//		algorithm.updateIsDone(compObj.getIdentifier());
//		isUpdateRequestReceived = false;
		
		synchronized (updatingSyncMonitor) {
			synchronized (ondemandSyncMonitor) {
				isUpdateRequestReceived = false;
			}
			algorithm.updateIsDone(compObj.getIdentifier());
			
			//before changing to NORMAL, compStatus is supposed to be UPDATING.
			LOGGER.info("-----------" + "CompStatus: " + compStatus + " -> NORMAL" + ", dynamic update is done, now notify all...\n\n");
			compStatus = CompStatus.NORMAL;
			updatingSyncMonitor.notifyAll();
		}
		
	}
	
	@Override
	public void updating(){
		compStatus = CompStatus.UPDATING;
	}
	
	@Override
	public void achievedFree(){
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.achievedFree();
		
		synchronized (validToFreeSyncMonitor) {
			compStatus = CompStatus.Free;
			LOGGER.fine("-----------component has achieved free,now nitify all...\n\n");
			validToFreeSyncMonitor.notifyAll();
		}
	}

	@Override
	public Set<String> getStaticInDeps() {
		return compObj.getStaticInDeps();
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
			Set<String> bufferOldVersionRootTxs, TransactionContext txContext,
			boolean isUpdateReqRCVD) {
		return algorithm.isBlockRequiredForFree(algorithmOldVersionRootTxs, bufferOldVersionRootTxs, txContext, isUpdateReqRCVD);
	}

	@Override
	public Object getWaitingRemoteCompUpdateDoneMonitor() {
		return waitingRemoteCompUpdateDoneMonitor;
	}

	@Override
	public void remoteDynamicUpdateIsDone() {
		synchronized (waitingRemoteCompUpdateDoneMonitor) {
//			compStatus = CompStatus.VALID;
			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.VALID)){
				LOGGER.warning("CompStatus is supposed to be NORMAL or VALID, but it's " + compStatus);
			}
			if(compStatus.equals(CompStatus.VALID)){
				compStatus = CompStatus.NORMAL;
				System.out.println("remote update is done, CompStatus: " + compStatus + ", now notify all");
				waitingRemoteCompUpdateDoneMonitor.notifyAll();
			}
		}
	}

	@Override
	public boolean updateIsReceived() {
		isUpdateRequestReceived = true;
		algorithm.start(compObj.getIdentifier());
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
	
}
