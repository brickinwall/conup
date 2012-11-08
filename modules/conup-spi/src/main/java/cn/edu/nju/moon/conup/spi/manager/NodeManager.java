package cn.edu.nju.moon.conup.spi.manager;

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

//	@Override
//	public DynamicUpdateManager getDynamicUpdateManager(String compName) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/**
	 * each component has only one OndemandSetupHelper
	 * @param compIdentifier component object identifier
	 * @return
	 */
	public OndemandSetupHelper getOndemandSetupHelper(String compIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}
}
