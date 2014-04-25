package cn.edu.nju.moon.conup.ext.comp.manager;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
import cn.edu.nju.moon.conup.spi.datamodel.BufferEventType;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorStub;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;
import cn.edu.nju.moon.conup.spi.utils.Printer;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayload;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadResolver;

/**
 * UpdateManager is used to execute the real update operation which is delegated from CompLifeCycleManager
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * Jul 26, 2013 10:07:04 PM
 */
public class UpdateManagerImpl implements UpdateManager {
	private static final Logger LOGGER = Logger.getLogger(UpdateManagerImpl.class.getName());
	
	private CompLifeCycleManager compLifeCycleMgr = null;

	/** ComponentObject that represents current CompLifecycleManager*/
	private ComponentObject compObj;
	private ComponentUpdator compUpdator = null;
	private DynamicDepManager depMgr = null;
	
	/** is updated? */
	private boolean isUpdated = false;
	private OndemandSetupHelper ondemandSetupHelper = null;
	
	/** DynamicUpdateContext */
	private DynamicUpdateContext updateCtx = null;
	
	private BufferEventType bufferEventType = BufferEventType.NORMAL;
	
	/** key is rootTxId, value is the sub component visit log(including component name and version)**/
	private Map<String, ArrayList<String>> compsVisitLogs = new ConcurrentHashMap<String, ArrayList<String>>();
	
	@Override
	public void initUpdateMgr(ComponentObject compObj) {
		setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
		this.compObj = compObj;
	}

	@Override
	public void attemptToUpdate() {
		Object validToFreeSyncMonitor = compObj.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)
				&& updateCtx != null && updateCtx.isLoaded()){
				//calculate old version root txs
				if(!updateCtx.isOldRootTxsInitiated()){
					initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(dynamicDepMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = compObj.getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf, compLifeCycleMgr);
				if(freeness.isReadyForUpdate(compObj.getIdentifier())){
//					compLifeCycleMgr.achieveFree();
					achieveFree();
				}
			}
		}
		
		Object updatingSyncMonitor = compObj.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.FREE)){
				executeUpdate();
				cleanupUpdate();
			}
		}
		
	}

	@Override
	public void achieveFree() {
		// FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.achievedFree();

		Object validToFreeSyncMonitor = compObj.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			CompStatus compStatus = compLifeCycleMgr.getCompStatus();
			LOGGER.fine("compStatus: " + compStatus);
			assert compStatus.equals(CompStatus.VALID) || compStatus.equals(CompStatus.FREE);
			if (compStatus.equals(CompStatus.VALID)) {
//				compStatus = CompStatus.Free;
				compLifeCycleMgr.transitToFree();
				
				// TODO
				bufferEventType = BufferEventType.EXEUPDATE;
				notifyInterceptors(bufferEventType);

				LOGGER.info("**** Component has achieved free, now notify all ****\n\n");
				validToFreeSyncMonitor.notifyAll();
			}
		}		
	}

	@Override
	public void checkFreeness(String hostComp) {
		Object validToFreeSyncMonitor = compObj.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(isDynamicUpdateRqstRCVD() && getUpdateCtx().isOldRootTxsInitiated()){

				if (compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)) {
					attemptToUpdate();
				}
			}
		}
	}

	@Override
	public void cleanupUpdate() {
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
		// if dynamic update is done, cleanup is needed
		LOGGER.info("**** BeforeSetToNewVersion");
		compUpdator.finalizeOld(compIdentifier, updateCtx.getOldVerClass(),
				updateCtx.getNewVerClass(), UpdateFactory.createTransformer());
		compUpdator.initNewVersion(compIdentifier, updateCtx.getNewVerClass());
		compUpdator.cleanUpdate(compIdentifier);
		compUpdator = UpdateFactory.createCompUpdator(compObj.getImplType());
		
		LOGGER.info("**** HaveSetToNewVersion");
		LOGGER.info("finish update and clean up is done!");
		
		//FOR TEST
//		Set<Dependence> allInDeps;
//		allInDeps = depMgr.getRuntimeInDeps();
//		String inDepsStr = "";
//		for (Dependence dep : allInDeps) {
//			inDepsStr += "\n" + dep.toString();
//		}
//		LOGGER.fine("in CompLcMgr, when cleanupUpdate is done:" + inDepsStr);
		
//		}
		Object updatingSyncMonitor = compObj.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			compLifeCycleMgr.transitToNormal();
			bufferEventType = BufferEventType.NORMAL;
			notifyInterceptors(bufferEventType);
			updatingSyncMonitor.notifyAll();
		}
		
		depMgr.dynamicUpdateIsDone();
		PerformanceRecorder.getInstance(compIdentifier).updateIsDone(System.nanoTime());
		
	}

	@Override
	public void executeUpdate() {
		String compIdentifier;
		//duplicated update
		synchronized (this) {
			if(isUpdated){
				LOGGER.warning("Dupulicated executeUpdate request, return directly");
				return;
			}
			isUpdated = true;
			compIdentifier = compObj.getIdentifier();
			
			compLifeCycleMgr.transitToUpdating();
		}
		
		//update
		compUpdator.executeUpdate(compIdentifier);
		
	}

	public ComponentUpdator getCompUpdator() {
		return compUpdator;
	}

	@Override
	public DynamicDepManager getDepMgr() {
		return depMgr;
	}

	@Override
	public OndemandSetupHelper getOndemandSetupHelper() {
		return ondemandSetupHelper;
	}

	@Override
	public DynamicUpdateContext getUpdateCtx() {
		return updateCtx;
	}

	@Override
	public Map<String, ArrayList<String>> getCompsVisitLogs() {
		return compsVisitLogs;
	}

	@Override
	public void initOldRootTxs() {
		if(!updateCtx.isOldRootTxsInitiated()){
			updateCtx.setAlgorithmOldRootTxs(depMgr.getAlgorithmOldVersionRootTxs());
			
			LOGGER.fine("getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
		}
	}

	@Override
	public boolean isDynamicUpdateRqstRCVD() {
		return updateCtx!=null && updateCtx.isLoaded();
	}
	
	private String manageDep(RequestObject reqObj) {
		boolean manageResult = depMgr.manageDependence(reqObj.getPayload());
		return "manageDepResult:" + manageResult;
	}

	/**
	 * this method is invoked during the ondemand process
	 * it means that during the ondemand process, all the message transfered among the 
	 * peer components are processed by this method
	 * @param reqObj
	 * @return ondemandResult
	 */
	private String manageOndemand(RequestObject reqObj) {
		boolean ondemandResult = false;
		ondemandResult = ondemandSetupHelper.ondemandSetup(reqObj.getSrcIdentifier(), reqObj.getProtocol(), reqObj.getPayload());
		return "ondemandResult:" + ondemandResult;
		
	}

	/**
	 * All remote configurations are processed by this method
	 * @param reqObj
	 * @return
	 */
	private String manageRemoteConf(RequestObject reqObj) {
	//		boolean updateResult = compLifeCycleMgr.remoteConf(reqObj.getPayload());
			boolean result = false;
			String payload = reqObj.getPayload();
			UpdateContextPayloadResolver payloadResolver = new UpdateContextPayloadResolver(payload);
			UpdateOperationType opTyep = payloadResolver.getOperation();
			String compIdentifier = payloadResolver.getParameter(UpdateContextPayload.COMP_IDENTIFIER);
			
			if(opTyep.equals(UpdateOperationType.UPDATE)){
				String baseDir = payloadResolver.getParameter(UpdateContextPayload.BASE_DIR);
				String classFilePath = payloadResolver.getParameter(UpdateContextPayload.CLASS_FILE_PATH);
				String contributionURI = payloadResolver.getParameter(UpdateContextPayload.CONTRIBUTION_URI);
				String compositeURI = payloadResolver.getParameter(UpdateContextPayload.COMPOSITE_URI);
				Scope scope = Scope.inverse(payloadResolver.getParameter(UpdateContextPayload.SCOPE));
				result = update(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier, scope);
			} else if(opTyep.equals(UpdateOperationType.ONDEMAND)){
				NodeManager nodeMgr = NodeManager.getInstance();
				OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
				Scope scope = Scope.inverse(payloadResolver.getParameter(UpdateContextPayload.SCOPE));
				result = ondemandHelper.ondemandSetup(scope);
			} else if(opTyep.equals(UpdateOperationType.QUERY)){
				
			}
			
			return "updateResult:" + result;
		}
	
	@Override
	public String processMsg(RequestObject reqObj) {
		MsgType msgType = reqObj.getMsgType();
		
		if(msgType.equals(MsgType.DEPENDENCE_MSG)){
			return manageDep(reqObj);
		} else if(msgType.equals(MsgType.ONDEMAND_MSG)){
			return manageOndemand(reqObj);
		} else if(msgType.equals(MsgType.REMOTE_CONF_MSG)){ 
			return manageRemoteConf(reqObj);
		} else if(msgType.equals(MsgType.EXPERIMENT_MSG)){
			return manageExp(reqObj);
		} else{
			//TODO manage negotiation msg
			return null;
		}
		
	}

	private String manageExp(RequestObject reqObj) {
		String payload = reqObj.getPayload();
		UpdateContextPayloadResolver updateCtxPayloadResolver = new UpdateContextPayloadResolver(payload);
		UpdateOperationType updateOperationType = updateCtxPayloadResolver.getOperation();
		if(updateOperationType.equals(UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP)){
			DisruptionExp.getInstance().setUpdateEndTime(System.nanoTime());
			LOGGER.info("Coordination receive NOTIFY_UPDATE_IS_DONE_EXP");
		} else if(updateOperationType.equals(UpdateOperationType.GET_EXECUTION_RECORDER)) {
			return ExecutionRecorder.getInstance(reqObj.getTargetIdentifier()).getActionsAndClear();
		}
		return "default message";
	}

	public void setCompUpdator(ComponentUpdator compUpdator) {
		this.compUpdator = compUpdator;
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
	}

	@Override
	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx) {
		this.updateCtx = updateCtx;
	}
	
	@Override
	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr) {
		this.compLifeCycleMgr = compLifeCycleMgr;
	}
	
	@Override
	public void setOndemandSetupHelper(OndemandSetupHelper ondemandSetupHelper) {
		this.ondemandSetupHelper = ondemandSetupHelper;
	}

	public boolean update(String baseDir, String classFilePath,
			String contributionURI, String compositeURI, String compIdentifier, Scope scope) {
		NodeManager nodeMgr = NodeManager.getInstance();
		assert compObj.getIdentifier().equals(compIdentifier);
		
		PerformanceRecorder.getInstance(compIdentifier).updateReceived(System.nanoTime());
		ExecutionRecorder.getInstance(compIdentifier).receiveUpdateRequest();
		
		synchronized (this) {
			//If a dynamic update has received, return.
			if(updateCtx!=null && updateCtx.isLoaded()){
				LOGGER.warning("duplicated update request!!!");
				return true;
			}
			
			isUpdated = false;
			
			//initiate updator
			compUpdator.initUpdator(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier);
//			depMgr.updateIsReceived();
//			compLifeCycleMgr.updateIsReceived();
			compObj.updateIsReceived();
		}
		assert(ondemandSetupHelper != null);
		//on-demand setup
		if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL)){
			OndemandSetupHelper ondemandHelper;
			ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
			ondemandHelper.ondemandSetup(scope);
		}
		
		AttemptUpdateThread attemptUpdaterThread;
		attemptUpdaterThread = new AttemptUpdateThread(this, compLifeCycleMgr);
		attemptUpdaterThread.start();

		return true;
	}

	@Override
	public void removeAlgorithmOldRootTx(String rootTxId) {
		if(isDynamicUpdateRqstRCVD() && getUpdateCtx().isOldRootTxsInitiated()){
			getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);

			LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

			if (compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)) {
				attemptToUpdate();
			}
		}
	}

	@Override
	public boolean isUpdatedTo(String newVerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dynamicUpdateIsDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean uninstall(String contributionURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean install(String contributionURI, String contributionURL) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void ondemandSetupIsDone() {
		Object ondemandSyncMonitor = compObj.getOndemandSyncMonitor();
		CompStatus compStatus = compLifeCycleMgr.getCompStatus();
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
				
//				compStatus = CompStatus.VALID;
				compLifeCycleMgr.transitToValid();
//				if(isUpdateRequestReceived){
				if(compObj.isTargetComp()){
					bufferEventType = BufferEventType.VALIDTOFREE;
//					CompLifecycleManager clMgr = NodeManager.getInstance().getCompLifecycleManager(compObj.getIdentifier());
//					UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(compObj.getIdentifier());
					if (!getUpdateCtx().isOldRootTxsInitiated()) {
						initOldRootTxs();
//						Printer printer = new Printer();
//						printer.printTxs(LOGGER, getTxs());
						
					}
					
				} else {
					bufferEventType = BufferEventType.WAITFORREMOTEUPDATE;
				}
				notifyInterceptors(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				OndemandSetupHelper ondemandSetupHelper = NodeManager.getInstance().getOndemandSetupHelper(compObj.getIdentifier());
				ondemandSetupHelper.onDemandIsDone();
				ondemandSyncMonitor.notifyAll();
				LOGGER.info("**** " + compObj.getIdentifier() + "ondemand setup is done, now notify all ****\n\n");
				Printer printer = new Printer();
				printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "In");
				printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "Out");
				
//				if(isUpdateRequestReceived){
				if(compObj.isTargetComp()){
					depMgr.ondemandSetupIsDone();
				}
			}
		}
	}

	private void notifyInterceptors(BufferEventType eventType){
		InterceptorStub interceptorStub = NodeManager.getInstance().getInterceptorStub(compObj.getIdentifier());
		interceptorStub.notifyInterceptors(eventType);
	}

	@Override
	public void remoteDynamicUpdateIsDone() {
		Object waitingRemoteCompUpdateDoneMonitor = compObj.getWaitingRemoteCompUpdateDoneMonitor();
		synchronized (waitingRemoteCompUpdateDoneMonitor) {
			CompStatus compStatus = compLifeCycleMgr.getCompStatus();
			assert compStatus.equals(CompStatus.NORMAL) || compStatus.equals(CompStatus.VALID);
//			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.VALID)){
//				LOGGER.warning("CompStatus is supposed to be NORMAL or VALID, but it's " + compStatus);
//			}
//			LOGGER.info(compObj.getIdentifier() + " receive remote_update_is_done, CompStatus: " + compStatus + ", now notify all");
			if(compStatus.equals(CompStatus.VALID)){
//				compStatus = CompStatus.NORMAL;
				compLifeCycleMgr.transitToNormal();
				//TODO updateManager
				bufferEventType = BufferEventType.NORMAL;
				notifyInterceptors(bufferEventType);
//				notifyObservers(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				String compIdentifier = compObj.getIdentifier();
				LOGGER.info(compIdentifier + " remote update is done, CompStatus: " + compStatus + ", now notify all");
				waitingRemoteCompUpdateDoneMonitor.notifyAll();
				
				// add for experiments
				// record update cost time
//				if(compIdentifier.equals("Coordination"))
//					DisruptionExp.getInstance().setUpdateEndTime(System.nanoTime());
			}
		}
	}

	@Override
	public void ondemandSetting() {
		Object ondemandSyncMonitor = compObj.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			CompStatus compStatus = compLifeCycleMgr.getCompStatus();
//			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.ONDEMAND))
//				System.out.println(compObj.getIdentifier() + "--> compStatus:" + compStatus);
			assert compStatus.equals(CompStatus.NORMAL) || compStatus.equals(CompStatus.ONDEMAND);
			if(compStatus.equals(CompStatus.NORMAL)){
//				this.compStatus = CompStatus.ONDEMAND;
				compLifeCycleMgr.transitToOndemand();
				bufferEventType = BufferEventType.ONDEMAND;
				notifyInterceptors(bufferEventType);
//				notifyObservers(bufferEventType);
			}
		}
	}
}
