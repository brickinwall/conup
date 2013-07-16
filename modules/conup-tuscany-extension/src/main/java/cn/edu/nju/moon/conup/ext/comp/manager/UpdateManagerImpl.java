package cn.edu.nju.moon.conup.ext.comp.manager;

import java.util.logging.Logger;

import org.apache.tuscany.sca.invocation.Message;

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
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

public class UpdateManagerImpl implements UpdateManager {
	private static final Logger LOGGER = Logger.getLogger(UpdateManagerImpl.class.getName());
	
	private DynamicDepManager depMgr = null;
	private OndemandSetupHelper ondemandSetupHelper = null;
	private CompLifecycleManager compLifeCycleMgr = null;
	/** DynamicUpdateContext */
	private DynamicUpdateContext updateCtx = null;
	
	/** ComponentObject that represents current CompLifecycleManager*/
	private ComponentObject compObj;
	/** is updated? */
	private boolean isUpdated = false;
	
	private ComponentUpdator compUpdator = null;

	public UpdateManagerImpl(ComponentObject compObj){
		setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
		this.compObj = compObj;
	}

	@Override
	public void attemptToUpdate() {
		Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(depMgr.getCompStatus().equals(CompStatus.VALID) 
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
					depMgr.achievedFree();
				}
			}
		}
		
		Object updatingSyncMonitor = depMgr.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if(depMgr.getCompStatus().equals(CompStatus.Free)){
				executeUpdate();
				cleanupUpdate();
			}
		}
		
	}

	@Override
	public void checkFreeness(String hostComp) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(isDynamicUpdateRqstRCVD() && getUpdateCtx().isOldRootTxsInitiated()){

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)) {
					attemptToUpdate();
				}
			}
		}
	}

	@Override
	public boolean cleanupUpdate() {
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
		
		return true;
	}

	@Override
	public boolean executeUpdate() {
		String compIdentifier;
		//duplicated update
		synchronized (this) {
			if(isUpdated){
				LOGGER.warning("Dupulicated executeUpdate request, return directly");
				return true;
			}
			isUpdated = true;
			compIdentifier = compObj.getIdentifier();
			
			depMgr.updating();
		}
		
		//update
		compUpdator.executeUpdate(compIdentifier);
		
		return true;
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
	public String process(RequestObject reqObj) {
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
	
	@Override
	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx) {
		this.updateCtx = updateCtx;
	}

	public void setCompUpdator(ComponentUpdator compUpdator) {
		this.compUpdator = compUpdator;
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
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
			depMgr.updateIsReceived();
		}
		
		//on-demand setup
		if(depMgr.isNormal() ){
			OndemandSetupHelper ondemandHelper;
			ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
			ondemandHelper.ondemandSetup();
		}
		
		AttemptUpdateThread attemptUpdaterThread;
		attemptUpdaterThread = new AttemptUpdateThread(this, depMgr);
		attemptUpdaterThread.start();

		return true;
	}

	@Override
	public Message checkRemoteUpdate(TransactionContext txCtx, Object subTx,
			Interceptor interceptor, Message msg) {
		TxLifecycleManager txLifecycleMgr = depMgr.getTxLifecycleMgr();
		String hostComp = compObj.getIdentifier();
		String freenessConf = depMgr.getCompObject().getFreenessConf();
		FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
		Object waitingRemoteCompUpdateDoneMonitor = depMgr.getWaitingRemoteCompUpdateDoneMonitor();
		synchronized (waitingRemoteCompUpdateDoneMonitor) {
			if (getUpdateCtx() == null || getUpdateCtx().isLoaded() == false) {
				if( freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, false)){
					interceptor.freeze(waitingRemoteCompUpdateDoneMonitor);
//					try {
//						waitingRemoteCompUpdateDoneMonitor.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
				// the invoked transaction is not a root transaction
				if(txCtx.getRootTx() != null){
					assert txCtx.getParentTx() != null;
					assert txCtx.getParentComponent() != null;
					assert subTx != null;
					txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), 
							txCtx.getRootTx(), txCtx.getRootComponent(),
							txCtx.getParentTx(), txCtx.getParentComponent());
				}
				return msg;
			}
		}
		
		return null;
	}

	@Override
	public void checkUpdate(Interceptor interceptor) {
		// TODO Auto-generated method stub
		
	}

}
