package cn.edu.nju.moon.conup.core.factory.impl;

import cn.edu.nju.moon.conup.spi.factory.ManagerFactory;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * A factory for creating different managers. 
 * We've got several managers here:
 * <ul>
 * 	<li> DynamicDepManager
 *  <li> DynamicUpdateManager
 *  <li> OndemandSetupManager
 * </ul>
 * 
 * This factory is not visible to other modules.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ManagerFactoryImpl implements ManagerFactory {
	/**
	 * @return an instance of DynamicDepManager
	 */
	@Override
	public DynamicDepManager createDynamicDepManager(){
		
		return null;
	}
	
//	/**
//	 * 
//	 * @return an instance of DynamicUpdateManager
//	 */
//	public DynamicUpdateManager createDynamicUpdateManager(){
//		
//		return null;
//	}
	
	/**
	 * 
	 * @return an instance of OndemandSetupManager
	 */
	@Override
	public OndemandSetupHelper createOndemandSetupManager(){
		
		return null;
	}
}
