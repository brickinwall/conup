/**
 * 
 */
package cn.edu.nju.moon.conup.spi.factory;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;

/**
 * Currently, we have three strategies for achieving freeness,
 * this factory is responsible for creating a FreenessStrategy object 
 * according to the user configuration 
 * @author Jiang Wang
 *
 */
public class FreenessStrategyFactory {
	
	/**
	 * 
	 * According to the configuration, creating an FreenessStrategy implementation
	 * 
	 * @return FreenessStrategy
	 */
	public FreenessStrategy getFreenessStrategy(){
		
		return null;
	}

}
