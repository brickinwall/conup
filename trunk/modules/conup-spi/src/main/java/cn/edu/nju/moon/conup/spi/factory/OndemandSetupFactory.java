/**
 * 
 */
package cn.edu.nju.moon.conup.spi.factory;

import cn.edu.nju.moon.conup.spi.datamodel.OndemandSetup;


/**
 * Since three algorithms are available for dynamic update, 
 * this class return a related ondemandsetup strategy according to user's current configuration.
 * @author Jiang Wang
 *
 */
public class OndemandSetupFactory {
	/**
	 * 
	 * according to the user configuration, creating an on-demand setup implementation
	 * 
	 * @return OndemandSetup
	 */
	public OndemandSetup getOndemandSetup(){
		return null;
	}
}
