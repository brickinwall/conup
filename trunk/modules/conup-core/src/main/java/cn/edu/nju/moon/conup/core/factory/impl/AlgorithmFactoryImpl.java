/**
 * 
 */
package cn.edu.nju.moon.conup.core.factory.impl;

import java.util.ServiceLoader;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;


/**
 * Since three algorithms are available for dynamic update, this class return a related algorithm
 * according to user's current configuration.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class AlgorithmFactoryImpl implements AlgorithmFactory {
	
	@Override
	public Algorithm createAlgorithm(){
		ServiceLoader<Algorithm> algorithms = ServiceLoader.load(Algorithm.class); 
//		for(Algorithm algorithm : algorithms){
//			if(algorithm.getAlgorithmType().equals(Algorithm.CONSISTENCY_ALGORITHM)){
//				System.out.println("Algorithm.CONSISTENCY_ALGORITHM) created.");
//				return algorithm;
//			}
//		}
		return null;
	}
	
	@Override
	public OndemandSetup createOndemandSetup(){
		return null;
	}
	
	@Override
	public FreenessStrategy createFreenessStrategy(){
		
		return null;
	}
	
}
