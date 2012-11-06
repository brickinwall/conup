package cn.edu.nju.moon.conup.spi.manager;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;

/**
 * A Tuscany node can host many components, we provide one NodeManager for each node.
 * It's responsible for managing multiple component managers on the node.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface NodeManager {
	/**
	 * each component has only one DynamicDepManager
	 * @param compName
	 * @return
	 */
	public DynamicDepManager getDynamicDepManager(String compName);
	
//	/**
//	 * each component has only one DynamicUpdateManager
//	 * @param compName
//	 * @return
//	 */
//	public DynamicUpdateManager getDynamicUpdateManager(String compName);
	
	/**
	 * each component has only one OndemandSetupManager
	 * @param compName
	 * @return
	 */
	public OndemandSetupHelper getOndemandSetupManager(String compName);
	
}
