package cn.edu.nju.moon.conup.ext.comp.manager;

import org.apache.tuscany.sca.Node;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;

import cn.edu.nju.moon.conup.ext.utils.TuscanyPayloadResolver;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayload;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
import cn.edu.nju.moon.conup.spi.datamodel.BufferEventType;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorStub;
import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

/**
 * CompLifecycleManager: manage the component's lifecyle 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 10:56:13 PM
 */
public class CompLifecycleManagerImpl implements CompLifeCycleManager {
	private final static Logger LOGGER = Logger.getLogger(CompLifecycleManagerImpl.class.getName());
	/** DynamicUpdateContext */
	/** a business tuscany node */
	private Node tuscanyNode = null;
	/** ComponentObject that represents current CompLifecycleManager*/
	private ComponentObject compObj;
	/** is updated? */
	private boolean isUpdated = false;
	/** current component's status */
	private CompStatus compStatus = CompStatus.NORMAL;
	
	private Object ondemandSyncMonitor = new Object();
	
	private Object validToFreeSyncMonitor = new Object();
	
	private Object updatingSyncMonitor = new Object();
	
	/** semaphore which is used to synchronize the non-target component */
	private Object waitingRemoteCompUpdateDoneMonitor = new Object();
	
	private Object freezeSyncMonitor = new Object();
	
	private BufferEventType bufferEventType = BufferEventType.NOTHING;
	
	/** used to identify whether received update request */
	private boolean isUpdateRequestReceived = false;
	
	private DynamicDepManager depMgr = null;

	public CompStatus getCompStatus() {
		return compStatus;
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
	public boolean isOndemandSetting() {
		synchronized (ondemandSyncMonitor) {
			return compStatus.equals(CompStatus.ONDEMAND);
		}
	}
	
	@Override
	public void ondemandSetting() {
		synchronized (ondemandSyncMonitor) {
			if(!compStatus.equals(CompStatus.NORMAL) && !compStatus.equals(CompStatus.ONDEMAND))
				System.out.println(compObj.getIdentifier() + "--> compStatus:" + compStatus);
			assert compStatus.equals(CompStatus.NORMAL) || compStatus.equals(CompStatus.ONDEMAND);
			if(compStatus.equals(CompStatus.NORMAL)){
				this.compStatus = CompStatus.ONDEMAND;
				bufferEventType = BufferEventType.ONDEMAND;
				notifyInterceptors(bufferEventType);
//				notifyObservers(bufferEventType);
			}
		}
	}
	
	@Override
	public void ondemandSetupIsDone() {
		
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
//						printer.printTxs(LOGGER, getTxs());
						
					}
					
				} else {
					bufferEventType = BufferEventType.WAITFORREMOTEUPDATE;
				}
				notifyInterceptors(bufferEventType);
//				System.out.println("in ddm:" + bufferEventType);
				
				OndemandSetupHelper ondemandSetupHelper = NodeManager.getInstance().getOndemandSetupHelper(compObj.getIdentifier());
				ondemandSetupHelper.resetIsOndemandRqstRcvd();
				
				LOGGER.info("-------------- " + compObj.getIdentifier() + "ondemand setup is done, now notify all...------\n\n");
				ondemandSetupHelper.onDemandIsDone();
				ondemandSyncMonitor.notifyAll();
				
				if(isUpdateRequestReceived){
					depMgr.ondemandSetupIsDone();
				}
			}
		}
	}
	
	@Override
	public void dynamicUpdateIsDone() {
		//FOR TEST
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(compObj.getIdentifier());
		exeRecorder.updateIsDone();
		
		synchronized (updatingSyncMonitor) {
			//before changing to NORMAL, compStatus is supposed to be UPDATING.
			LOGGER.info("-----------" + "CompStatus: " + compStatus + " -> NORMAL" + ", dynamic update is done, now notify all...\n\n");
			isUpdateRequestReceived = false;
			compStatus = CompStatus.NORMAL;
			bufferEventType = BufferEventType.NOTHING;
			notifyInterceptors(bufferEventType);
//			notifyObservers(bufferEventType);
//			System.out.println("in ddm:" + bufferEventType);
			
			updatingSyncMonitor.notifyAll();
			depMgr.dynamicUpdateIsDone();
		}
		
	}
	
	@Override
	public boolean isOndemandSetupRequired() {
		return compStatus.equals(CompStatus.NORMAL) 
				|| compStatus.equals(CompStatus.ONDEMAND);
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
		
		synchronized (validToFreeSyncMonitor) {
			LOGGER.fine("compStatus: " + compStatus);
			assert compStatus.equals(CompStatus.VALID) || compStatus.equals(CompStatus.Free);
			if(compStatus.equals(CompStatus.VALID)){
				compStatus = CompStatus.Free;
				bufferEventType = BufferEventType.EXEUPDATE;
				notifyInterceptors(bufferEventType);
				
				LOGGER.info("-----------component has achieved free,now nitify all...\n\n");
				validToFreeSyncMonitor.notifyAll();
			}
		}
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
				notifyInterceptors(bufferEventType);
//				notifyObservers(bufferEventType);
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
	public DynamicDepManager getDepMgr() {
		return depMgr;
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
	}


	public CompLifecycleManagerImpl(ComponentObject compObj){
		this.compObj = compObj;
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	public static CompLifecycleManagerImpl getInstance(String compIdentifier){
		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		
		return (CompLifecycleManagerImpl) nodeMgr.getCompLifecycleManager(compIdentifier);
	}
	
	@Override
	public boolean uninstall(String contributionURI){
	    //create Tuscany node
		boolean isUninstalled = true;
	    try {
			Contribution contribution = tuscanyNode.getContribution(contributionURI);
		} catch (ContributionReadException e1) {
			e1.printStackTrace();
		} catch (ValidationException e1) {
			e1.printStackTrace();
		}
	    assert contributionURI != null;
	    // key is contributionURI, List<String> is compositeURI
	    Map<String, List<String>> startedComposites = tuscanyNode.getStartedCompositeURIs();
	    boolean isEmpty = startedComposites.isEmpty();
	    Iterator<Entry<String, List<String>>> iterator = startedComposites.entrySet().iterator();
	    // stop all composite in this contribution
	    while(iterator.hasNext()){
	    	List<String> value = iterator.next().getValue();
	    	Iterator<String> compositeIterator = value.iterator();
	    	while(compositeIterator.hasNext()){
	    		try {
	    			tuscanyNode.stopCompositeAndUninstallUnused(contributionURI, compositeIterator.next());
				} catch (ActivationException e) {
					e.printStackTrace();
				}
	    	}
	    }
		
	    //try to load uninstalled contribution, it will throw a IllegalArgumentException
	    //it means that this contribution is uninstalled
	    try {
	    	tuscanyNode.getContribution(contributionURI);
		} catch (ContributionReadException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e){
			isUninstalled = true;
		}
		
		return isUninstalled;
	}

	@Override
	public boolean install(String contributionURI, String contributionURL){
		boolean isInstalled = false;
		String cd = null;
		try {
			cd = tuscanyNode.installContribution(contributionURL);
			tuscanyNode.startDeployables(contributionURI);
		} catch (ContributionReadException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (ActivationException e) {
			e.printStackTrace();
		}
		
		if(cd.equals(contributionURI)){
			isInstalled = true;
		}
		return isInstalled;
	}
	
	@Override
	public boolean stop(String contributionURI){
//		boolean isStopped = true;
		//TODO every node could install only one contribution?
		return uninstall(contributionURI);
	}
	

	@Override
	public void setCompObject(ComponentObject compObj){
		this.compObj = compObj;
	}
	
	@Override
	public ComponentObject getCompObject(){
		return compObj;
	}
	
	public Node getNode(){
		return tuscanyNode;
	}
	
	public void setNode(Node node){
		this.tuscanyNode = node;
	}
	
	@Override
	public boolean isUpdatedTo(String newVerId){
		return isUpdated;
	}
	
	public String experimentResult(String payload){
		TuscanyPayloadResolver payloadResolver = new TuscanyPayloadResolver(payload);
		TuscanyOperationType opTyep = payloadResolver.getOperation();
		String compIdentifier = payloadResolver.getParameter(TuscanyPayload.COMP_IDENTIFIER);
		if(opTyep.equals(TuscanyOperationType.GET_EXECUTION_RECORDER)){
			return ExecutionRecorder.getInstance(compIdentifier).getActionsAndClear();
		} else if(opTyep.equals(TuscanyOperationType.GET_UPDATE_ENDTIME)){
			return Long.toString(PerformanceRecorder.getInstance(compIdentifier).getUpdateEndTime());
		} else if(opTyep.equals(TuscanyOperationType.NOTIFY_COORDINATIONIN_TRANQUILLITY_EXP)){
			DisruptionExp.getInstance().setUpdateEndTime(System.nanoTime());
			return "ok";
		} else{
			LOGGER.warning("unsupported operation type for experiment");
		}
		//TODO 
		return "no results";
	}
	
	private void notifyInterceptors(BufferEventType eventType){
		InterceptorStub interceptorStub = NodeManager.getInstance().getInterceptorStub(compObj.getIdentifier());
		interceptorStub.notifyInterceptors(eventType);
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
	public void updateIsReceived() {
		LOGGER.info("Received dynamic update request.");
		isUpdateRequestReceived = true;
	}

	@Override
	public boolean isTargetComp() {
		return isUpdateRequestReceived;
	}

	@Override
	public boolean isReadyForUpdate() {
		return isValid() && depMgr.isReadyForUpdate();
	}
	
//	@Override
//	public void update(Subject subject, Object arg) {
//		RequestObject reqObj = (RequestObject) arg;
//		if(reqObj.getMsgType().equals(MsgType.EXPERIMENT_MSG)){
//			String expResult = experimentResult(reqObj.getPayload());
//			subject.setResult(expResult);
//		} else if(reqObj.getMsgType().equals(MsgType.REMOTE_CONF_MSG)){
////			boolean updateResult = remoteConf(reqObj.getPayload());
////			subject.setResult("updateResult:" + updateResult);
//		}
//	}

}
