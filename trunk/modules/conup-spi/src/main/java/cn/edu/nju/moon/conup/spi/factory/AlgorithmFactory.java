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
	 * According to the algorithmTyep, i.e., Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * creating an Algorithm implementation
	 * 
	 * @return Algorithm
	 */
	public Algorithm getAlgorithm(){
		return null;
	}
	
}
