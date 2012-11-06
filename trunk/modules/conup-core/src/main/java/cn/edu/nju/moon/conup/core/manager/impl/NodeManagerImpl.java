package cn.edu.nju.moon.conup.core.manager.impl;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * A Tuscany node can host many components, we provide one NodeManager for each node.
 * It's responsible for managing multiple component managers on the node.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class NodeManagerImpl implements NodeManager{
	private static NodeManagerImpl nodeManagerImpl = new NodeManagerImpl();
	
	
	private NodeManagerImpl(){
	}
	
	public static NodeManagerImpl getInstance(){
		return nodeManagerImpl;
	}

	@Override
	public DynamicDepManager getDynamicDepManager(String compName) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public DynamicUpdateManager getDynamicUpdateManager(String compName) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public OndemandSetupHelper getOndemandSetupManager(String compName) {
		// TODO Auto-generated method stub
		return null;
	}
}
