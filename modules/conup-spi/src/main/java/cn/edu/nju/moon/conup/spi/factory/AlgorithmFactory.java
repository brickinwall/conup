/**
 * 
 */
package cn.edu.nju.moon.conup.spi.factory;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.OndemandSetup;


/**
 * Since three algorithms are available for dynamic update, this class return a related algorithm
 * according to user's current configuration.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class AlgorithmFactory {
	
	/**
	 * 
	 * According to the algorithmTyep, i.e., Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * creating an Algorithm implementation
	 * 
	 * @return Algorithm
	 */
	public Algorithm getAlgorithm(){
		return null;
	}
	
	/**
	 * 
	 * according to the user configuration, creating an on-demand setup implementation
	 * 
	 * @return OndemandSetup
	 */
	public OndemandSetup getOndemandSetup(){
		return null;
	}
	
	/**
	 * Currently, we have three strategies for achieving freeness,
	 * this factory is responsible for creating a FreenessStrategy implementation 
	 * according to the user configuration 
	 * 
	 * @return FreenessStrategy
	 */
	public FreenessStrategy getFreenessStrategy(){
		
		return null;
	}
	
}
