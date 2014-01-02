package cn.edu.nju.moon.conup.spi.factory;

import java.util.ServiceLoader;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * A factory for creating different managers. 
 * We've got several managers here:
 * <ul>
 * 	<li> DynamicDepManager
 *  <li> OndemandSetupHelper
 * </ul>
 * 
 * This factory is not visible to other modules.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ManagerFactory {
	/**
	 * @return an instance of DynamicDepManager. if no DynamicDepManager is available, return null 
	 */
	public DynamicDepManager createDynamicDepManager(){
		ServiceLoader<DynamicDepManager> depMgrs = ServiceLoader.load(DynamicDepManager.class); 
		for(DynamicDepManager mgr : depMgrs){
			return mgr;
		}
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
	 * @return an instance of OndemandSetupHelper
	 */
	public OndemandSetupHelper createOndemandSetupHelper(){
		ServiceLoader<OndemandSetupHelper> helpers = ServiceLoader.load(OndemandSetupHelper.class); 
		for(OndemandSetupHelper helper : helpers){
			return helper;
		}
		return null;
	}

	/**
	 * @author Guochao Ren
	 * @return an instance of UpdateManager
	 */
	public UpdateManager createUpdateManager() {
		ServiceLoader<UpdateManager> updateMgrs = ServiceLoader.load(UpdateManager.class);
		for(UpdateManager updateMgr : updateMgrs){
			return updateMgr;
		}
		return null;
	}
}
