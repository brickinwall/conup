package cn.edu.nju.moon.conup.core.manager.impl;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.manager.DynamicUpdateManager;

/**
 * DynamicUpdateManager provides a series of method for dynamic update.
 * It's supposed to be invoked by:
 * 	0) CompLifecycleManager and 
 * 	1) interceptor for deciding whether a request need to be intercepted
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class DynamicUpdateManagerImpl implements DynamicUpdateManager {
	private FreenessStrategy freenessStrategy = null;
	
	public DynamicUpdateManagerImpl(){
		freenessStrategy = new AlgorithmFactory().getFreenessStrategy();
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
	@Override
	public void manage(String rootTxID, String rootComp, String parentComp, 
			String curTxID, String hostComp){
		freenessStrategy.manage(rootTxID, rootComp, parentComp, curTxID, hostComp);
	}
	
}
