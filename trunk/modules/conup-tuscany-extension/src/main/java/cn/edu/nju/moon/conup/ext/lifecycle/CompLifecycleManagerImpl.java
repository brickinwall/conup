package cn.edu.nju.moon.conup.ext.lifecycle;

import org.apache.tuscany.sca.Node;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayloadResolver;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayload;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
import cn.edu.nju.moon.conup.spi.complifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.complifecycle.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.complifecycle.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

/**
 * Component life cycle manager
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CompLifecycleManagerImpl implements CompLifecycleManager {
	private final static Logger LOGGER = Logger.getLogger(CompLifecycleManagerImpl.class.getName());
	/** DynamicUpdateContext */
	private DynamicUpdateContext updateCtx = null;
	/** a business tuscany node */
	private Node tuscanyNode = null;
	/** ComponentObject that represents current CompLifecycleManager*/
	private ComponentObject compObj;
	/** communication module */
//	private CommunicationServer commServer = null;
	/** is updated? */
	private boolean isUpdated = false;
	/** all the component life cycle managers */
//	private static Map<ComponentObject, CompLifecycleManagerImpl> compClMgrs = 
//			new ConcurrentHashMap<ComponentObject, CompLifecycleManagerImpl>();
	
	private ReflectiveInstanceFactory instanceFactory;
	
	private ComponentUpdator updator = null;
	
	private DynamicDepManager depMgr = null;
	
	@Override
	public DynamicDepManager getDepMgr() {
		return depMgr;
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
	}

	@Override
	public ComponentUpdator getCompUpdator() {
		return updator;
	}

	@Override
	public void setCompUpdator(ComponentUpdator compUpdator) {
		this.updator = compUpdator;
	}

	public CompLifecycleManagerImpl(ComponentObject compObj){
		setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
//		setDepMgr(NodeManager.getInstance().getDynamicDepManager(compObj.getIdentifier()));
		this.compObj = compObj;
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	public static CompLifecycleManagerImpl getInstance(String compIdentifier){
		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		
		return (CompLifecycleManagerImpl) nodeMgr.getCompLifecycleManager(compIdentifier);
		
//		ComponentObject compObj;
//		NodeManager nodeMgr;
//		CompLifecycleManagerImpl clMgr;
//		
//		nodeMgr = NodeManager.getInstance();
//		synchronized (compClMgrs) {
//			compObj = nodeMgr.getComponentObject(compIdentifier);
//			if(compObj == null)
//				return null;
//			
//			if( !compClMgrs.containsKey(compObj) ){
//				clMgr = new CompLifecycleManagerImpl();
//				compClMgrs.put(compObj, clMgr);
//				clMgr.setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
//				clMgr.setCompObject(compObj);
//				clMgr.setDepMgr(nodeMgr.getDynamicDepManager(compIdentifier));
//			} else{
//				clMgr = compClMgrs.get(compObj);
//			}
//			assert clMgr.getCompUpdator()!=null;
//			
//		}
//		return clMgr;
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
	public boolean update(String baseDir, String classFilePath, String contributionURI, String compositeURI, String compIdentifier){
		NodeManager nodeMgr;
		
		nodeMgr = NodeManager.getInstance();
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
			updator.initUpdator(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier);
//			depMgr.setTxDepMonitor(new TxDepMonitorImpl());
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
	public boolean attemptToUpdate(){
//		NodeManager nodeManager = NodeManager.getInstance();
//		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(compObj.getIdentifier());
		
		CompLifecycleManager compLcMgr;
		compLcMgr = CompLifecycleManagerImpl.getInstance(compObj.getIdentifier());
			
		
		Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(depMgr.getCompStatus().equals(CompStatus.VALID) 
				&& updateCtx != null && updateCtx.isLoaded()){
				//calculate old version root txs
				if(!updateCtx.isOldRootTxsInitiated()){
					compLcMgr.initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(dynamicDepMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = compObj.getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
				if(freeness.isReadyForUpdate(compObj.getIdentifier())){
					depMgr.achievedFree();
//				} else{
//					try {
//						LOGGER.fine("try to  free, try to update------------------CompLifecycleManager");
//						validToFreeSyncMonitor.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
			}
		}
		
		Object updatingSyncMonitor = depMgr.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if(depMgr.getCompStatus().equals(CompStatus.Free)){
				compLcMgr.executeUpdate();
				compLcMgr.cleanupUpdate();
			}
		}
		
		return true;
	}
	
	@Override
	public boolean executeUpdate(){
		String compIdentifier;
//		NodeManager nodeMgr;
//		DynamicDepManager depMgr;
		//duplicated update
		synchronized (this) {
			if(isUpdated){
				LOGGER.warning("Dupulicated executeUpdate request, return directly");
				return true;
			}
			isUpdated = true;
			compIdentifier = compObj.getIdentifier();
//			nodeMgr = NodeManager.getInstance();
//			depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
			
			depMgr.updating();
		}
		
		//update
		updator.executeUpdate(compIdentifier);
		
		return true;
	}
	
	@Override
	public boolean cleanupUpdate(){
//		NodeManager nodeMgr;
//		DynamicDepManager depMgr;
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
//		nodeMgr = NodeManager.getInstance();
//		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
//		updator = UpdateFactory.createCompUpdator(compObj.getImplType());
		// if dynamic update is done, cleanup is needed
		LOGGER.info("**** BeforeSetToNewVersion");
//		if (depMgr.getOldVersionRootTxs().isEmpty()) {
		updator.finalizeOld(compIdentifier, updateCtx.getOldVerClass(),
				updateCtx.getNewVerClass(), UpdateFactory.createTransformer());
		updator.initNewVersion(compIdentifier, updateCtx.getNewVerClass());
		updator.cleanUpdate(compIdentifier);
		updator = UpdateFactory.createCompUpdator(compObj.getImplType());
		
		LOGGER.info("**** HaveSetToNewVersion");
//		LOGGER.fine("clean up for updator is done!");
		LOGGER.info("clean up for updator is done!");
		
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
	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx) {
		this.updateCtx = updateCtx;
	}

	public ReflectiveInstanceFactory getInstanceFactory() {
		return instanceFactory;
	}

	public void setInstanceFactory(ReflectiveInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
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
	
//	public CommunicationServer getCommServer() {
//		return commServer;
//	}
//	
//	public void setCommServer(CommunicationServer commServer) {
//		this.commServer = commServer;
//	}
	
	@Override
	public boolean isUpdatedTo(String newVerId){
		return isUpdated;
	}
	
	@Override
	public boolean remoteConf(String payload){
		TuscanyPayloadResolver payloadResolver = new TuscanyPayloadResolver(payload);
		TuscanyOperationType opTyep = payloadResolver.getOperation();
		String compIdentifier = payloadResolver.getParameter(TuscanyPayload.COMP_IDENTIFIER);
		
		if(opTyep.equals(TuscanyOperationType.UPDATE)){
			String baseDir = payloadResolver.getParameter(TuscanyPayload.BASE_DIR);
			String classFilePath = payloadResolver.getParameter(TuscanyPayload.CLASS_FILE_PATH);
			String contributionURI = payloadResolver.getParameter(TuscanyPayload.CONTRIBUTION_URI);
			String compositeURI = payloadResolver.getParameter(TuscanyPayload.COMPOSITE_URI);
			update(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier);
		} else if(opTyep.equals(TuscanyOperationType.ONDEMAND)){
			NodeManager nodeMgr = NodeManager.getInstance();
			OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
			ondemandHelper.ondemandSetup();
		} else if(opTyep.equals(TuscanyOperationType.QUERY)){
			
		}
		return true;
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
	
	@Override
	public boolean isDynamicUpdateRqstRCVD(){
		return updateCtx!=null && updateCtx.isLoaded();
	}
	
	@Override
	public boolean initOldRootTxs(){
//		NodeManager nodeMgr;
//		DynamicDepManager depMgr;
//		String compIdentifier;
//		
//		compIdentifier = compObj.getIdentifier();
//		nodeMgr = NodeManager.getInstance();
//		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		if(!updateCtx.isOldRootTxsInitiated()){
			updateCtx.setAlgorithmOldRootTxs(depMgr.getAlgorithmOldVersionRootTxs());
			
			LOGGER.fine("getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
		}
		return true;
	}
	
	@Override
	public boolean reinitOldRootTxs(){
//		NodeManager nodeMgr;
//		DynamicDepManager depMgr;
//		String compIdentifier;
//		
//		compIdentifier = compObj.getIdentifier();
//		nodeMgr = NodeManager.getInstance();
//		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		updateCtx.setAlgorithmOldRootTxs(depMgr.getAlgorithmOldVersionRootTxs());
		LOGGER.fine("reinitOldRootTxs.getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());

		return true;
	}
	
//	public boolean removeBufferoldRootTxs(String parentTx, String rootTx){
//		NodeManager nodeMgr;
//		DynamicDepManager depMgr;
//		String compIdentifier;
//		
//		compIdentifier = compObj.getIdentifier();
//		nodeMgr = NodeManager.getInstance();
//		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
//		
//		updateCtx.removeBufferOldRootTx(depMgr.getAlgorithmRoot(parentTx, rootTx));
//		
//		return true;
//	}
	
	@Override
	public DynamicUpdateContext getUpdateCtx() {
		return updateCtx;
	}
	
	@Override
	public void checkFreeness(String hostComp) {
		CompLifecycleManager compLcMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		compLcMgr = CompLifecycleManagerImpl.getInstance(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(compLcMgr.isDynamicUpdateRqstRCVD() && compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)) {
					compLcMgr.attemptToUpdate();
				}
			}
		}
	}
}
