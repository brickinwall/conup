package cn.edu.nju.moon.conup.spi.manager;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface OndemandSetupManager {
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	public boolean ondemandSetup(String targetComponent, String freenessSetup, Scope scope);
}
