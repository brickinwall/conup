package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorStub;
import cn.edu.nju.moon.conup.spi.exception.ConupMgrNotFoundException;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.factory.ManagerFactory;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.XMLUtil;

/**
 * A Tuscany node can host many components, we provide one NodeManager for each node.
 * It's responsible for managing multiple component managers on the node.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class NodeManager{
	private static NodeManager nodeManager = new NodeManager();
	
	/**
	 * 	A node may host many components. In our implementation, each component is mapped to 
	 * 	a ComponentObject. 
	 * 	Map<String, ComponentObject> takes component identifier as the key.
	 */
	private Map<String, ComponentObject> compObjects;
	
	/**
	 * 	Each component has one DynamicDepManager
	 */
	private Map<ComponentObject, DynamicDepManager> depMgrs;
	
	/**
	 * 	Each component has one OndemandSetupHelper
	 */
	private Map<ComponentObject, OndemandSetupHelper> ondemandHelpers;
	
	/**
	 * 	Each component has one TxLifecycleManager
	 */
	private Map<ComponentObject, TxLifecycleManager> txLifecycleMgrs;
	
	/**
	 * 	Each component has one TxDepMonitor
	 */
	private Map<ComponentObject, TxDepMonitor> txDepMonitors;
	
	/**
	 * 	Each component has one CompLifecycleManager
	 */
	private Map<ComponentObject, CompLifeCycleManager> compLifecycleMgrs;
	
	/**
	 * 	Each component has one UpdateManager 
	 */
	private Map<ComponentObject, UpdateManager> updateMgrs;
	
	private Map<ComponentObject, InterceptorStub> interceptorStubs;
	
	private Object tuscanyNode;
	
	private NodeManager(){
		compObjects = new ConcurrentHashMap<String, ComponentObject>();
		depMgrs = new ConcurrentHashMap<ComponentObject, DynamicDepManager>();
		ondemandHelpers = new ConcurrentHashMap<ComponentObject, OndemandSetupHelper>();
		txLifecycleMgrs = new ConcurrentHashMap<ComponentObject, TxLifecycleManager>();
		txDepMonitors = new ConcurrentHashMap<ComponentObject, TxDepMonitor>();
		compLifecycleMgrs = new ConcurrentHashMap<ComponentObject, CompLifeCycleManager>();
		updateMgrs = new ConcurrentHashMap<ComponentObject, UpdateManager>();
		interceptorStubs = new ConcurrentHashMap<ComponentObject, InterceptorStub>();
	}
	
	/**
	 * NodeManager is implemented as a singleton
	 * @return
	 */
	public static NodeManager getInstance(){
		return nodeManager;
	}
	
	public Object getTuscanyNode() {
		return tuscanyNode;
	}

	public void setTuscanyNode(Object tuscanyNode) {
		this.tuscanyNode = tuscanyNode;
	}

	/**
	 * each component has only one CompLifecycleManager
	 * @param compIdentifier component object identifier
	 * @return corresponding CompLifecycleManager of the specified compIdentifier, 
	 * 		   if the compIdentifier is invalid, return null
	 */
	public CompLifeCycleManager getCompLifecycleManager(String compIdentifier){
		ComponentObject compObj;
		CompLifeCycleManager compLifecycleMgr;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			if( !compLifecycleMgrs.containsKey(compObj) ){
				throw new ConupMgrNotFoundException("CompLifecycleManager not found.");
			} else{
				compLifecycleMgr = compLifecycleMgrs.get(compObj);
			}
		}
		return compLifecycleMgr;
	}
	
	/**
	 * each component has only one TxDepMonitor
	 * @param compIdentifier component object identifier
	 * @return corresponding TxDepMonitor of the specified compIdentifier, 
	 * 		   if the compIdentifier is invalid, return null
	 */
	public TxDepMonitor getTxDepMonitor(String compIdentifier){
		ComponentObject compObj;
		TxDepMonitor txDepMonitor;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			if( !txDepMonitors.containsKey(compObj) ){
				throw new ConupMgrNotFoundException("TxDepMonitor not found.");
			} else{
				txDepMonitor = txDepMonitors.get(compObj);
				DynamicDepManager depMgr = depMgrs.get(compObj);
				depMgr.setTxDepRegistry(txDepMonitor.getTxDepRegistry());
				OndemandSetupHelper ondemandHelpler = ondemandHelpers.get(compObj);
				ondemandHelpler.getOndemand().setTxDepRegistry(txDepMonitor.getTxDepRegistry());
			}
		}
		return txDepMonitor;
	}
	
	/**
	 * each component has only one TxLifecycleManager
	 * @param compIdentifier component object identifier
	 * @return corresponding TxLifecycleManager of the specified compIdentifier, 
	 * 		   if the compIdentifier is invalid, return null
	 */
	public TxLifecycleManager getTxLifecycleManager(String compIdentifier){
		ComponentObject compObj;
		TxLifecycleManager txLifecycleMgr;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			if( !txLifecycleMgrs.containsKey(compObj) ){
				throw new ConupMgrNotFoundException("TxLifecycleManager not found.");
			} else{
				txLifecycleMgr = txLifecycleMgrs.get(compObj);
			}
		}
		return txLifecycleMgr;
	}
	
	/**
	 * each component has only one DynamicDepManager
	 * @param compIdentifier component object identifier
	 * @return corresponding DynamicDepManager of the specified compIdentifier, 
	 * 		   if the compIdentifier is invalid, return null
	 */
	public DynamicDepManager getDynamicDepManager(String compIdentifier) {
		ComponentObject compObj;
		DynamicDepManager depMgr;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			
			if( !depMgrs.containsKey(compObj) ){
				depMgr = new ManagerFactory().createDynamicDepManager();
				depMgrs.put(compObj, depMgr);
				depMgr.setCompObject(compObj);
				
				Algorithm algorithm;
				algorithm = new AlgorithmFactory().createAlgorithm(compObj.getAlgorithmConf());
				if(algorithm == null){
					throw new NullPointerException("alogrithm should not be null after createAlgorithm(...) in NodeManager");
				}
				depMgr.setAlgorithm(algorithm);
			} else{
				depMgr = depMgrs.get(compObj);
			}
			
		}
		return depMgr;
	}

	/**
	 * each component has only one OndemandSetupHelper
	 * @param compIdentifier component object identifier
	 * @return
	 */
	public OndemandSetupHelper getOndemandSetupHelper(String compIdentifier) {
		ComponentObject compObj;
		OndemandSetupHelper helper;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			
			if( !ondemandHelpers.containsKey(compObj) ){
				helper = new ManagerFactory().createOndemandSetupHelper();
				ondemandHelpers.put(compObj, helper);
				helper.setCompObject(compObj);
				
				OndemandSetup ondemandSetup;
				ondemandSetup = new AlgorithmFactory().createOndemandSetup(compObj.getAlgorithmConf());
				helper.setOndemand(ondemandSetup);
				helper.setDynamicDepManager(getDynamicDepManager(compIdentifier));
				helper.setCompLifeCycleMgr(getCompLifecycleManager(compIdentifier));
//				ondemandSetup.setCompLifeCycleMgr(getCompLifecycleManager(compIdentifier));
			} else{
				helper = ondemandHelpers.get(compObj);
			}
		}
		return helper;
	}
	
	/**
	 * @param identifier the identifier of the component
	 * @return
	 */
	public ComponentObject getComponentObject(String identifier){
		return compObjects.get(identifier);
	}
	
	public void addComponentObject(String identifier, ComponentObject compObj){
		compObjects.put(identifier, compObj);
	}
	
//	public void setDynamicDependencyMgr(ComponentObject cmpObj, DynamicDepManager ddm){
//		depMgrs.put(cmpObj, ddm);
//	}
	
	/**
	 * remove a component.
	 * When a component stopped or leave from the domain, this method should be invoked.
	 * @param compIdentifier
	 */
	public void removeCompObject(String compIdentifier){
		depMgrs.remove(getComponentObject(compIdentifier));
		ondemandHelpers.remove(getComponentObject(compIdentifier));
		compObjects.remove(compIdentifier);
	}
	
	/**
	 * remove a component.
	 * When a component stopped or leave from the domain, this method should be invoked.
	 * @param compObj
	 */
	public void removeCompObject(ComponentObject compObj){
		removeCompObject(compObj.getIdentifier());
	}
	
	/**
	 * create ComponentObject by using conup.xml, add to collection
	 * @param compIdentifier
	 * @param versionNum
	 * @return
	 */
	public boolean loadConupConf(String compIdentifier, String versionNum){
		XMLUtil xmlUtil = new XMLUtil();
		String algorithmType = xmlUtil.getAlgorithmConf();
		String freenessStrategy = xmlUtil.getFreenessStrategy();
		Set<String> parentComps = xmlUtil.getParents(compIdentifier);
		Set<String> childrenComps = xmlUtil.getChildren(compIdentifier);
		String compImplType = "JAVA_POJO";
		ComponentObject compObj = new ComponentObject(compIdentifier, versionNum, algorithmType, freenessStrategy, childrenComps, parentComps, compImplType);
		addComponentObject(compIdentifier, compObj);
		return true;
	}
	
	/**
	 * create ComponentObject by using conup.xml, add to collection
	 * @param compIdentifier
	 * @param versionNum
	 * @param xmlPath
	 * @return
	 */
	public boolean loadConupConf(String compIdentifier, String versionNum, String xmlPath){
		XMLUtil xmlUtil = new XMLUtil(xmlPath);
		String algorithmType = xmlUtil.getAlgorithmConf();
		String freenessStrategy = xmlUtil.getFreenessStrategy();
		Set<String> parentComps = xmlUtil.getParents(compIdentifier);
		Set<String> childrenComps = xmlUtil.getChildren(compIdentifier);
		String compImplType = "JAVA_POJO";
		ComponentObject compObj = new ComponentObject(compIdentifier, versionNum, algorithmType, freenessStrategy, childrenComps, parentComps, compImplType);
		addComponentObject(compIdentifier, compObj);
		return true;
	}

	public boolean setCompLifecycleManager(String compIdentifier, CompLifeCycleManager compLifecycleMgr){
		ComponentObject compObj;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if( !compLifecycleMgrs.containsKey(compObj) ){
				compLifecycleMgrs.put(compObj, compLifecycleMgr);
				return true;
			} else{
				return false;
			}
		}
	}

	public boolean setTxDepMonitor(String compIdentifier, TxDepMonitor txDepMonitor) {
		ComponentObject compObj;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if( !txDepMonitors.containsKey(compObj) ){
				txDepMonitors.put(compObj, txDepMonitor);
				return true;
			} else{
				return false;
			}
		}
	}

	public boolean setTxLifecycleManager(String compIdentifier, TxLifecycleManager txLifecycleMgr) {
		ComponentObject compObj;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if( !txLifecycleMgrs.containsKey(compObj) ){
				txLifecycleMgrs.put(compObj, txLifecycleMgr);
				return true;
			} else{
				return false;
			}
		}
	}

	public UpdateManager getUpdateManageer(String compIdentifier) {
		ComponentObject compObj;
		UpdateManager updateMgr;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			if( !updateMgrs.containsKey(compObj) ){
				throw new ConupMgrNotFoundException("UpdateManager not found.");
			} else{
				updateMgr = updateMgrs.get(compObj);
				DynamicDepManager depMgr = depMgrs.get(compObj);
				OndemandSetupHelper ondemandHelpler = ondemandHelpers.get(compObj);
				updateMgr.setDepMgr(depMgr);
				updateMgr.setOndemandSetupHelper(ondemandHelpler);
			}
		}
		return updateMgr;
	}
	
	public boolean setUpdateManager(String compIdentifier,	UpdateManager updateMgr) {
		ComponentObject compObj;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if (!updateMgrs.containsKey(compObj)) {
				updateMgrs.put(compObj, updateMgr);
				return true;
			} else {
				return false;
			}
		}
	}
	
	public InterceptorStub getInterceptorStub(String compIdentifier){
		ComponentObject compObj;
		InterceptorStub interceptorStub;
		synchronized (this) {
			compObj = getComponentObject(compIdentifier);
			if(compObj == null)
				return null;
			if( !interceptorStubs.containsKey(compObj) ){
				interceptorStub = new InterceptorStub();
				interceptorStubs.put(compObj, interceptorStub);
			} else{
				interceptorStub = interceptorStubs.get(compObj);
			}
			return interceptorStub;
		}
	}
	
} 
