package cn.edu.nju.moon.conup.spi.manager;

import cn.edu.nju.moon.conup.spi.datamodel.OndemandSetup;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.factory.OndemandSetupFactory;

/**
 * @author Jiang Wang
 *
 */
public class OndemandSetupManager {
	private OndemandSetup ondemandSetup = null;
	
	public OndemandSetupManager(){
		ondemandSetup = new OndemandSetupFactory().getOndemandSetup();
	}
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	public boolean ondemandSetup(String targetComponent, String freenessSetup, Scope scope){
		ondemandSetup.ondemand(targetComponent, freenessSetup, scope);
		return true;
	}
}
