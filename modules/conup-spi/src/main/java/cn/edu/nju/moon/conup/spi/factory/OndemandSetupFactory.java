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
	 * @param algorithmTyep Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * @return according to the algorithmType, return related OndemandSetup implementation
	 */
	public OndemandSetup getOndemand(String algorithmTyep){
		return null;
	}
}
