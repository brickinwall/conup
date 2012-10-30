/**
 * 
 */
package cn.edu.nju.moon.conup.spi.factory;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;


/**
 * Since three algorithms are available for dynamic update, this class return a related algorithm
 * according to user's current configuration.
 * @author Jiang Wang
 *
 */
public class AlgorithmFactory {
	
	/**
	 * 
	 * @param algorithmTyep Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * @return according to the algorithmType, return related algorithm
	 */
	public Algorithm getAlgorithm(String algorithmTyep){
		return null;
	}
	
}
