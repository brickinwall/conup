package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.omg.CORBA.Principal;

import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.core.TransactionRegistry;
import cn.edu.nju.moon.conup.core.ondemand.OndemandSetupHelperImpl;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.DisruptionExp;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;
import cn.edu.nju.moon.conup.spi.utils.Printer;

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
	private TransactionRegistry txRegistry = new TransactionRegistry();
	/** */
	private TransactionRegistry fakeTxRegistry = new TransactionRegistry();
	/** used to identify whether received update request */
	private boolean isUpdateRequestReceived = false;
	
	private Object ondemandSyncMonitor = new Object();
	
	private Object validToFreeSyncMonitor = new Object();
	
	private Object updatingSyncMonitor = new Object();
	
	/**
	 * 
	 */
	private Object waitingRemoteCompUpdateDoneMonitor = new Object();
	
	private TxDepMonitor txDepMonitor = null;

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
		long enterTime = System.nanoTime();
		
		algorithm.manageDependence(txContext);
		
		long leaveTime = System.nanoTime();
		LOGGER.fine(txContext.getHostComponent() + " algorithm.doNormal() cost time:" + (leaveTime - enterTime) / 1000000.0);
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
		LOGGER.fine("algReadyForUpdate:" + algReadyForUpdate + " compStatus.equals(CompStatus.VALID): " + compStatus.equals(CompStatus.VALID));
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
	
	/**
	 * 
	 * @return transactions that are launched by another component, 
	 * and its's tx id is also generated by that component
	 */
	public Map<String, TransactionContext> getFakeTxs(){
		return fakeTxRegistry.getTransactionContexts();
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
		
		//FOR TEST
		String inDepsStr = "";
		for (Dependence dep : inDepRegistry.getDependences()) {
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("ondemandSetupIsDone, inDepsStr:" + inDepsStr);
		
		String outDepsStr = "";
		for (Dependence dep : outDepRegistry.getDependences()) {
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("ondemandSetupIsDone, outDepsStr:" + outDepsStr);
		
		Printer printer = new Printer();
		LOGGER.fine("ondemandSetupIsDone, Txs:");
		printer.printTxs(LOGGER, getTxs());
		
		compStatus = CompStatus.VALID;
		OndemandSetupHelper ondemandSetupHelper = NodeManager.getInstance().getOndemandSetupHelper(compObj.getIdentifier());
		ondemandSetupHelper.resetIsOndemandRqstRcvd();
		synchronized (ondemandSyncMonitor) {
			LOGGER.fine("--------------ondemand setup is done, now notify all...------\n\n");
			ondemandSetupHelper.onDemandIsDone();
			ondemandSyncMonitor.notifyAll();
		}
	}

	@Override
	public void dynamicUpdateIsDone() {
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.updateIsDone();
		
		String inDepsStr = "";
		for (Dependence dep : inDepRegistry.getDependences()) {
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("achievedFree, inDepsStr:" + inDepsStr);
		
		String outDepsStr = "";
		for (Dependence dep : outDepRegistry.getDependences()) {
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("achievedFree, outDepsStr:" + outDepsStr);
		
		Printer printer = new Printer();
		LOGGER.fine("achievedFree, Txs:");
		printer.printTxs(LOGGER, getTxs());
		
		synchronized (updatingSyncMonitor) {
			algorithm.updateIsDone(compObj.getIdentifier());
			//before changing to NORMAL, compStatus is supposed to be UPDATING.
			LOGGER.info("-----------" + "CompStatus: " + compStatus + " -> NORMAL" + ", dynamic update is done, now notify all...\n\n");
			isUpdateRequestReceived = false;
			compStatus = CompStatus.NORMAL;
			updatingSyncMonitor.notifyAll();
			
//			LOGGER.info("-----------" + "CompStatus: " + compStatus + " -> NORMAL" + ", dynamic update is done, now notify all...\n\n");
//			isUpdateRequestReceived = false;
//			compStatus = CompStatus.VALID;
//			updatingSyncMonitor.notifyAll();
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
		
		String inDepsStr = "";
		for (Dependence dep : inDepRegistry.getDependences()) {
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("achievedFree, inDepsStr:" + inDepsStr);
		
		String outDepsStr = "";
		for (Dependence dep : outDepRegistry.getDependences()) {
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("achievedFree, outDepsStr:" + outDepsStr);
		
		Printer printer = new Printer();
		LOGGER.fine("achievedFree, Txs:");
		printer.printTxs(LOGGER, getTxs());
		
		synchronized (validToFreeSyncMonitor) {
			if(compStatus.equals(CompStatus.VALID)){
				compStatus = CompStatus.Free;
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
		algorithm.initiate(compObj.getIdentifier());
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
	
	public TxDepMonitor getTxDepMonitor() {
//		return txDepMonitor;
		if(txDepMonitor == null)
			return null;
		return txDepMonitor.newInstance();
	}

	public void setTxDepMonitor(TxDepMonitor txDepMonitor) {
		this.txDepMonitor = txDepMonitor;
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
		return algorithm.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
	}
	
}
