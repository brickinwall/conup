package cn.edu.nju.moon.conup.ext.comp.manager;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayload;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayloadResolver;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

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

	public UpdateManagerImpl(ComponentObject compObj){
		setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
		this.compObj = compObj;
	}

	@Override
	public void attemptToUpdate() {
		Object validToFreeSyncMonitor = compLifeCycleMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(compLifeCycleMgr.isValid()
				&& updateCtx != null && updateCtx.isLoaded()){
				//calculate old version root txs
				if(!updateCtx.isOldRootTxsInitiated()){
					initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(dynamicDepMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = compObj.getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
				if(freeness.isReadyForUpdate(compObj.getIdentifier())){
					compLifeCycleMgr.achievedFree();
				}
			}
		}
		
		Object updatingSyncMonitor = compLifeCycleMgr.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.Free)){
				executeUpdate();
				cleanupUpdate();
			}
		}
		
	}

	@Override
	public void checkFreeness(String hostComp) {
		Object validToFreeSyncMonitor = compLifeCycleMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(isDynamicUpdateRqstRCVD() && getUpdateCtx().isOldRootTxsInitiated()){

				if (compLifeCycleMgr.isValid()) {
					attemptToUpdate();
				}
			}
		}
	}

	@Override
	public void checkUpdate(Interceptor interceptor) {
		// TODO Auto-generated method stub
		
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
			
			compLifeCycleMgr.updating();
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
		boolean manageResult = depMgr.manageDependence(reqObj.getProtocol(), reqObj.getPayload());
		return "manageDepResult:" + manageResult;
	}

	private String manageOndemand(RequestObject reqObj) {
		boolean ondemandResult = false;
		ondemandResult = ondemandSetupHelper.ondemandSetup(reqObj.getSrcIdentifier(), reqObj.getProtocol(), reqObj.getPayload());
		return "ondemandResult:" + ondemandResult;
		
	}

	private String manageRemoteConf(RequestObject reqObj) {
	//		boolean updateResult = compLifeCycleMgr.remoteConf(reqObj.getPayload());
			boolean result = false;
			String payload = reqObj.getPayload();
			TuscanyPayloadResolver payloadResolver = new TuscanyPayloadResolver(payload);
			TuscanyOperationType opTyep = payloadResolver.getOperation();
			String compIdentifier = payloadResolver.getParameter(TuscanyPayload.COMP_IDENTIFIER);
			
			if(opTyep.equals(TuscanyOperationType.UPDATE)){
				String baseDir = payloadResolver.getParameter(TuscanyPayload.BASE_DIR);
				String classFilePath = payloadResolver.getParameter(TuscanyPayload.CLASS_FILE_PATH);
				String contributionURI = payloadResolver.getParameter(TuscanyPayload.CONTRIBUTION_URI);
				String compositeURI = payloadResolver.getParameter(TuscanyPayload.COMPOSITE_URI);
				result = update(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier);
			} else if(opTyep.equals(TuscanyOperationType.ONDEMAND)){
				NodeManager nodeMgr = NodeManager.getInstance();
				OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
				result = ondemandHelper.ondemandSetup();
			} else if(opTyep.equals(TuscanyOperationType.QUERY)){
				
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
//			return manageExperimentResult(reqObj);
		} else{
			//TODO manage negotiation msg
			return null;
		}
		
		return null;
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
	public void setOndemandSetupHelper(OndemandSetupHelper ondemandSetupHelper) {
		this.ondemandSetupHelper = ondemandSetupHelper;
	}

	public boolean update(String baseDir, String classFilePath, String contributionURI, String compositeURI, String compIdentifier){
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
			compLifeCycleMgr.updateIsReceived();
		}
		
		//on-demand setup
		if(compLifeCycleMgr.isNormal() ){
			OndemandSetupHelper ondemandHelper;
			ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
			ondemandHelper.ondemandSetup();
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

			if (compLifeCycleMgr.isValid()) {
				attemptToUpdate();
			}
		}
	}

}
