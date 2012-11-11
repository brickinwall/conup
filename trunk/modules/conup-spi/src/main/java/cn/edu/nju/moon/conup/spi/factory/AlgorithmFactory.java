/**
 * 
 */
package cn.edu.nju.moon.conup.spi.factory;

import java.util.Iterator;
import java.util.ServiceLoader;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;


/**
 * Since three algorithms are available for dynamic update, this class return a related algorithm
 * according to user's current configuration.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface AlgorithmFactory {
	
	/**
	 * 
	 * According to the algorithmTyep, i.e., Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * creating an Algorithm implementation
	 * 
	 * @return Algorithm
	 */
	public Algorithm createAlgorithm();
	
	/**
	 * 
	 * according to the user configuration, creating an on-demand setup implementation
	 * 
	 * @return OndemandSetup
	 */
	public OndemandSetup createOndemandSetup();
	
	/**
	 * Currently, we have three strategies for achieving freeness,
	 * this factory is responsible for creating a FreenessStrategy implementation 
	 * according to the user configuration 
	 * 
	 * @return FreenessStrategy
	 */
	public FreenessStrategy createFreenessStrategy();
	
}
