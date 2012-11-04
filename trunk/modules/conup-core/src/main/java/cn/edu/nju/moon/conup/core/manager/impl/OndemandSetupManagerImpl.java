package cn.edu.nju.moon.conup.core.manager.impl;

import cn.edu.nju.moon.conup.spi.datamodel.OndemandSetup;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.manager.OndemandSetupManager;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class OndemandSetupManagerImpl implements OndemandSetupManager{
	private OndemandSetup ondemandSetup = null;
	
	public OndemandSetupManagerImpl(){
		ondemandSetup = new AlgorithmFactory().getOndemandSetup();
	}
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	@Override
	public boolean ondemandSetup(String targetComponent, String freenessSetup, Scope scope){
		ondemandSetup.ondemand(targetComponent, freenessSetup, scope);
		return true;
	}
}
