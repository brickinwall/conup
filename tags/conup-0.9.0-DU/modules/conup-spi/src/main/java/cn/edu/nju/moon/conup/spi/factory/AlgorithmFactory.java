package cn.edu.nju.moon.conup.spi.factory;

import java.util.ServiceLoader;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;


/**
 * Since three algorithms are available for dynamic update, this class return a related algorithm
 * according to user's current configuration.
 *
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class AlgorithmFactory {
	
	/**
	 * According to the algorithmTyep, i.e., Algorithm.CONSISTENCY_ALGORITHM/QUIESCENCE_ALGORITHM/TRANQUILLITY_ALGORITHM
	 * creating an Algorithm implementation
	 * @param algorithmType
	 * @return Algorithm
	 */
	public Algorithm createAlgorithm(String algorithmType){
		ServiceLoader<Algorithm> algorithms = ServiceLoader.load(Algorithm.class); 
		for(Algorithm algorithm : algorithms){
			if(algorithm.getAlgorithmType().equals(algorithmType)){
				return algorithm;
			}
		}
		return null;
	}
	
	/**
	 * according to the user configuration, creating an on-demand setup implementation
	 * @param algorithmType
	 * @return OndemandSetup
	 */
	public OndemandSetup createOndemandSetup(String algorithmType){
		ServiceLoader<OndemandSetup> ondemands = ServiceLoader.load(OndemandSetup.class); 
		for(OndemandSetup ondemand : ondemands){
			if(ondemand.getAlgorithmType().equals(algorithmType)){
				return ondemand;
			}
		}
		return null;
	}
	
	/**
	 * Currently, we have three strategies for achieving freeness,
	 * this factory is responsible for creating a FreenessStrategy implementation 
	 * according to the user configuration 
	 * @param strategy freeness strategy
	 * @return FreenessStrategy
	 */
	public FreenessStrategy createFreenessStrategy(String strategy){
		ServiceLoader<FreenessStrategy> strategies = ServiceLoader.load(FreenessStrategy.class); 
		for(FreenessStrategy fs : strategies){
			if(fs.getFreenessType().equals(strategy)){
				return fs;
			}
		}
		
		return null;
	}
	
}
