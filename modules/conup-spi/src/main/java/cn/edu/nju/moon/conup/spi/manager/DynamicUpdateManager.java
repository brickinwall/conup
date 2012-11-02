/**
 * 
 */
package cn.edu.nju.moon.conup.spi.manager;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.factory.FreenessStrategyFactory;

/**
 * DynamicUpdateManager provides a series of method for dynamic update.
 * It's supposed to be invoked by:
 * 	0) CompLifecycleManager and 
 * 	1) interceptor for deciding whether a request need to be intercepted
 * 
 * @author Jiang Wang
 *
 */
public class DynamicUpdateManager {
	private FreenessStrategy freenessStrategy = null;
	
	public DynamicUpdateManager(){
		freenessStrategy = new FreenessStrategyFactory().getFreenessStrategy();
	}
	
	/**
	 * manage() is an abstract operation for different freeness strategy.
	 * For BLOCKING, it's supposed to decide whether a request should be blocked.
	 * For WAITING, it will check whether a component is ready for update.
	 * FOr CONCURRENT_VERSION, it will create a dispatcher.
	 * 
	 * @param rootTxID root transaction id
	 * @param rootComp root component name
	 * @param parentComp parent component name
	 * @param curTxID current transaction id
	 * @param hostComp host component name
	 */
	public void manage(String rootTxID, String rootComp, String parentComp, 
			String curTxID, String hostComp){
		freenessStrategy.manage(rootTxID, rootComp, parentComp, curTxID, hostComp);
	}
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * @param compName component name
	 * @return
	 */
	public boolean isInterceptRequired(String compName){
		return freenessStrategy.isInterceptRequired(compName);
	}
	
	/**
	 * is a component valid?
	 * @param compName
	 * @return 
	 */
	public boolean isValid(String compName){
		return freenessStrategy.isValid(compName); 
	}
	
	/**
	 * is a component ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate(String compName){
		return freenessStrategy.isReadyForUpdate(compName);
	}
	
}
