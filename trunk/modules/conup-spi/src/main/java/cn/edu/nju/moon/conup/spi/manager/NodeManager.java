package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
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
	private Map<String, ComponentObject> compObjects = 
			new ConcurrentHashMap<String, ComponentObject>();
	
	private NodeManager(){
	}
	
	public static NodeManager getInstance(){
		return nodeManager;
	}

	/**
	 * each component has only one DynamicDepManager
	 * @param compIdentifier component object identifier
	 * @return
	 */
	public DynamicDepManager getDynamicDepManager(String compIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * each component has only one OndemandSetupHelper
	 * @param compIdentifier component object identifier
	 * @return
	 */
	public OndemandSetupHelper getOndemandSetupHelper(String compIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @param identifier the identifier of the component
	 * @return
	 */
	public ComponentObject getComponentObject(String identifier){
		return null;
	}
	
	public void setComponentObject(String identifier, ComponentObject compObj){
		compObjects.put(identifier, compObj);
	}
}
