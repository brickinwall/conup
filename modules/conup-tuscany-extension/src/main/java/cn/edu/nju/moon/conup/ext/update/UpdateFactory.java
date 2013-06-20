package cn.edu.nju.moon.conup.ext.update;


import cn.edu.nju.moon.conup.ext.freeness.BlockingStrategy;
import cn.edu.nju.moon.conup.ext.freeness.ConcurrentVersionStrategy;
import cn.edu.nju.moon.conup.ext.freeness.WaitingStrategy;
import cn.edu.nju.moon.conup.spi.complifecycle.ComponentUpdator;
import cn.edu.nju.moon.conup.spi.complifecycle.Transformer;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.helper.FreenessCallback;


/**
 * There are many different implementation type for a component, different implementation may have different
 * strategies for dynamic update.
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class UpdateFactory {
	/**
	 * 
	 * @param implType implementation type of the component
	 * @return
	 */
	public static ComponentUpdator createCompUpdator(String implType){
//		ServiceLoader<ComponentUpdator> updators = ServiceLoader.load(ComponentUpdator.class); 
//		for(ComponentUpdator updator : updators){
//			if(updator.getCompImplType().equals(implType)){
//				return updator;
//			}
//		}
//		return null;
		
		return new JavaCompUpdatorImpl();
	}
	
	/**
	 * 
	 * @return
	 */
	public static Transformer createTransformer(){
		
		return null;
	}
	
	/**
	 * 
	 * @param compImplType 
	 * @return
	 */
	public static FreenessCallback createFreenessCallback(String compImplType){
		return new JavaPojoFreenessCallback();
	}
	
	/**
	 * Currently, we have three strategies for achieving freeness,
	 * this factory is responsible for creating a FreenessStrategy implementation 
	 * according to the user configuration 
	 * @param strategy freeness strategy
	 * @return FreenessStrategy
	 */
	public static FreenessStrategy createFreenessStrategy(String strategy){
		if(strategy.equals(BlockingStrategy.BLOCKING)){
			return new BlockingStrategy();
		} else if(strategy.equals(WaitingStrategy.WAITING)){
			return new WaitingStrategy();
		} else if(strategy.equals(ConcurrentVersionStrategy.CONCURRENT_VERSION)){
			return new ConcurrentVersionStrategy();
		} else{
			return null;
		}
	}

}
