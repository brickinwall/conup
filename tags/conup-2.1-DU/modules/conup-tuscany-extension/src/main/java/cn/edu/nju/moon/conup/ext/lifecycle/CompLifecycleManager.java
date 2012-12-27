package cn.edu.nju.moon.conup.ext.lifecycle;

import org.apache.tuscany.sca.Node;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext;
import cn.edu.nju.moon.conup.ext.update.ComponentUpdator;
import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.ext.utils.TuscanyOperationType;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayloadResolver;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayload;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;

/**
 * Component life cycle manager
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CompLifecycleManager {
	private final static Logger LOGGER = Logger.getLogger(CompLifecycleManager.class.getName());
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
	private static Map<ComponentObject, CompLifecycleManager> compClMgrs = 
			new ConcurrentHashMap<ComponentObject, CompLifecycleManager>();
	
	private ReflectiveInstanceFactory instanceFactory;
	
	private ComponentUpdator updator = null;
	
	public ComponentUpdator getCompUpdator() {
		return updator;
	}

	public void setCompUpdator(ComponentUpdator compUpdator) {
		this.updator = compUpdator;
	}

	private CompLifecycleManager(){
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	public static CompLifecycleManager getInstance(String compIdentifier){
		ComponentObject compObj;
		NodeManager nodeMgr;
		CompLifecycleManager clMgr;
		
		nodeMgr = NodeManager.getInstance();
		synchronized (compClMgrs) {
			compObj = nodeMgr.getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			
			clMgr = compClMgrs.get(compObj);
			if( !compClMgrs.containsKey(compObj) ){
				clMgr = new CompLifecycleManager();
				compClMgrs.put(compObj, clMgr);
				clMgr.setCompUpdator(UpdateFactory.createCompUpdator(compObj.getImplType()));
			}
			clMgr.setCompObject(compObj);
			assert clMgr.getCompUpdator()!=null;
			
		}
		return compClMgrs.get(compObj);
	}
	
	/**
	 * 
	 * @param contributionURI
	 * @return
	 */
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

	/**
	 * 
	 * @param domainURI
	 * @param contributionURI
	 * @param contributionURL
	 * @param compositeURI
	 * @return
	 */
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
	
	/**
	 * stop a contribution
	 * @param contributionURI
	 * @return
	 */
	public boolean stop(String contributionURI){
//		boolean isStopped = true;
		//TODO every node could install only one contribution?
		return uninstall(contributionURI);
	}
	
	/**
	 * 
	 * @param implType the implementation type of the component, e.g., POJO, EJB.
	 * @param baseDir
	 * @param classPath
	 * @param contributionURI
	 * @param compositeURI
	 * @param compIdentifier target component's identifier
	 * @return
	 */
	//String baseDir, String classPath,String contributionURI, String compositeURI, String compIdentifier, Scope scope 
	public boolean update(String baseDir, String classFilePath, String contributionURI, String compositeURI, String compIdentifier){
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		
		synchronized (this) {
			//If a dynamic update has received, return.
			if(updateCtx!=null && updateCtx.isLoaded()){
				LOGGER.warning("duplicated update request!!!");
				return true;
			}
			
			isUpdated = false;
			
			//initiate updator
			updator.initUpdator(baseDir, classFilePath, contributionURI, compositeURI, compIdentifier);
			depMgr.updateIsReceived();
		}
		
		//on-demand setup
		if(depMgr.isNormal() ){
			OndemandSetupHelper ondemandHelper;
			ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
			ondemandHelper.ondemandSetup();
		}
		
		//waiting while on-demand setup
//		Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
//		synchronized (ondemandSyncMonitor) {
//			try {
//				System.out.println("in compLifeCycleMg, before depMgr.isOndemandSetupRequired()");
//				if (depMgr.isOndemandSetupRequired()) {
//					System.out.println("----------------in compLifeCycleMg, ondemandSyncMonitor.wait();compLifeCycleMg------------");
//					ondemandSyncMonitor.wait();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		//calculate old version root txs
//		Set<String> oldRootTxs = depMgr.getOldVersionRootTxs();
//		updateCtx.setOldVerionRootTxs(oldRootTxs);
		
//		attemptToUpdate();
//		OndemandSetupThread ondemandThread;
//		ondemandThread = new OndemandSetupThread(compIdentifier);
//		ondemandThread.start();
		
		AttemptUpdateThread attemptUpdaterThread;
		attemptUpdaterThread = new AttemptUpdateThread(this, depMgr);
		attemptUpdaterThread.start();

		return true;
	}
	
	public boolean attemptToUpdate(){
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(compObj.getIdentifier());
		
		CompLifecycleManager compLcMgr;
		compLcMgr = CompLifecycleManager.getInstance(compObj.getIdentifier());
			
		
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
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
					dynamicDepMgr.achievedFree();
//				} else{
//					try {
//						System.out.println("try to  free, try to update------------------CompLifecycleManager");
//						validToFreeSyncMonitor.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
			}
		}
		
		Object updatingSyncMonitor = dynamicDepMgr.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.Free)){
				compLcMgr.executeUpdate();
				compLcMgr.cleanupUpdate();
			}
		}
		
		return true;
	}
	
	public boolean executeUpdate(){
		String compIdentifier;
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		//duplicated update
		synchronized (this) {
			if(isUpdated){
				LOGGER.warning("Dupulicated executeUpdate request, return directly");
				return true;
			}
			isUpdated = true;
			compIdentifier = compObj.getIdentifier();
			nodeMgr = NodeManager.getInstance();
			depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
			
			depMgr.updating();
		}
		
		//update
		updator.executeUpdate(compIdentifier);
		
		return true;
	}
	
	public boolean cleanupUpdate(){
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
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
//		System.out.println("clean up for updator is done!");
		LOGGER.info("clean up for updator is done!");
		
		//FOR TEST
//		Set<Dependence> allInDeps;
//		allInDeps = depMgr.getRuntimeInDeps();
//		String inDepsStr = "";
//		for (Dependence dep : allInDeps) {
//			inDepsStr += "\n" + dep.toString();
//		}
//		System.out.println("in CompLcMgr, when cleanupUpdate is done:" + inDepsStr);
		
//		}
		
		depMgr.dynamicUpdateIsDone();
		
		return true;
	}
	
	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx) {
		this.updateCtx = updateCtx;
	}

	public ReflectiveInstanceFactory getInstanceFactory() {
		return instanceFactory;
	}

	public void setInstanceFactory(ReflectiveInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}
	
	public void setCompObject(ComponentObject compObj){
		this.compObj = compObj;
	}
	
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
	
	/**
	 * whethe the component is updated to new version?
	 * ATTENTION: temporally, the parameter is of no use 
	 * @param newVerId new version id of the component
	 * @return
	 */
	public boolean isUpdatedTo(String newVerId){
		return isUpdated;
	}
	
	/**
	 * it's a user-oriented method which is used for remote configuration
	 * @param payload
	 * @return
	 */
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
	
	/**
	 * if a component has received dynamic update request and is in the process of finishing update, return true.
	 * @return
	 */
	public boolean isDynamicUpdateRqstRCVD(){
		return updateCtx!=null && updateCtx.isLoaded();
	}
	
	public boolean initOldRootTxs(){
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		if(!updateCtx.isOldRootTxsInitiated()){
			updateCtx.setAlgorithmOldRootTxs(depMgr.getAlgorithmOldVersionRootTxs());
			updateCtx.setBufferOldRootTxs(TxLifecycleManager.copyOfOldRootTxs(compIdentifier));
			
			LOGGER.info("getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
			LOGGER.info("getBufferOldRootTxs:" + updateCtx.getBufferOldRootTxs().size() + updateCtx.getBufferOldRootTxs());
			
//			System.out.println("getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
//			System.out.println("getBufferOldRootTxs:" + updateCtx.getBufferOldRootTxs().size() + updateCtx.getBufferOldRootTxs());
		}
		return true;
	}
	
	/**
	 * re-calculate the old root tx sets, no matter whether the sets are initiated.
	 * @return
	 */
	public boolean reinitOldRootTxs(){
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		updateCtx.setAlgorithmOldRootTxs(depMgr.getAlgorithmOldVersionRootTxs());
		updateCtx.setBufferOldRootTxs(TxLifecycleManager.copyOfOldRootTxs(compIdentifier));
		LOGGER.fine("reinitOldRootTxs.getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
		LOGGER.fine("reinitOldRootTxs.getBufferOldRootTxs:" + updateCtx.getBufferOldRootTxs().size() + updateCtx.getBufferOldRootTxs());
		
//		System.out.println("reinitOldRootTxs.getAlgorithmOldRootTxs:" + updateCtx.getAlgorithmOldRootTxs().size() + updateCtx.getAlgorithmOldRootTxs());
//		System.out.println("reinitOldRootTxs.getBufferOldRootTxs:" + updateCtx.getBufferOldRootTxs().size() + updateCtx.getBufferOldRootTxs());
		return true;
	}
	
	public boolean removeBufferoldRootTxs(String parentTx, String rootTx){
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		String compIdentifier;
		
		compIdentifier = compObj.getIdentifier();
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		
		updateCtx.removeBufferOldRootTx(depMgr.getAlgorithmRoot(parentTx, rootTx));
		
		return true;
	}
	
	public DynamicUpdateContext getUpdateCtx() {
		return updateCtx;
	}
	
}
