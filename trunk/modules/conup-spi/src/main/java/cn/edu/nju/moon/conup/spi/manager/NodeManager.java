package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.factory.ManagerFactory;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;

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
	
	private NodeManager(){
		compObjects = new ConcurrentHashMap<String, ComponentObject>();
		depMgrs = new ConcurrentHashMap<ComponentObject, DynamicDepManager>();
		ondemandHelpers = new ConcurrentHashMap<ComponentObject, OndemandSetupHelper>();
	}
	
	/**
	 * NodeManager is implemented as a singleton
	 * @return
	 */
	public static NodeManager getInstance(){
		return nodeManager;
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
			
			depMgr = depMgrs.get(compObj);
			if( !depMgrs.containsKey(compObj) ){
				depMgr = new ManagerFactory().createDynamicDepManager();
				depMgrs.put(compObj, depMgr);
			}
			depMgr.setCompObject(compObj);
		}
		return depMgrs.get(compObj);
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
			
			helper = ondemandHelpers.get(compObj);
			if( !ondemandHelpers.containsKey(compObj) ){
				helper = new ManagerFactory().createOndemandSetupHelper();
				ondemandHelpers.put(compObj, helper);
			}
			helper.setCompObject(compObj);
		}
		return ondemandHelpers.get(compObj);
	}
	
	/**
	 * @param identifier the identifier of the component
	 * @return
	 */
	public ComponentObject getComponentObject(String identifier){
		return compObjects.get(identifier);
	}
	
	public void setComponentObject(String identifier, ComponentObject compObj){
		compObjects.put(identifier, compObj);
	}
	
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
} 
