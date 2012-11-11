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
	 * @return
	 */
	public DynamicDepManager getDynamicDepManager(String compIdentifier) {
		synchronized (this) {
			if( !depMgrs.containsKey(compIdentifier) ){
				ComponentObject compObj;
				compObj = getComponentObject(compIdentifier);
				if(compObj == null)
					return null;
				depMgrs.put(compObj, new ManagerFactory().createDynamicDepManager());
			}
		}
		return depMgrs.get(compIdentifier);
	}

	/**
	 * each component has only one OndemandSetupHelper
	 * @param compIdentifier component object identifier
	 * @return
	 */
	public OndemandSetupHelper getOndemandSetupHelper(String compIdentifier) {
		synchronized (this) {
			if( !ondemandHelpers.containsKey(compIdentifier) ){
				ComponentObject compObj;
				compObj = getComponentObject(compIdentifier);
				if(compObj == null)
					return null;
				ondemandHelpers.put(compObj, 
						new ManagerFactory().createOndemandSetupHelper());
			}
		}
		return ondemandHelpers.get(compIdentifier);
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
}
