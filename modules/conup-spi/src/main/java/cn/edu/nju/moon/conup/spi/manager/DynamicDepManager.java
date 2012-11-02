/**
 * 
 */
package cn.edu.nju.moon.conup.spi.manager;


import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang
 *
 */
public class DynamicDepManager {
	private Algorithm algorithm = null;
	
	public DynamicDepManager(){
		algorithm = new AlgorithmFactory().getAlgorithm();
	}
	
	/**
	 * maintain tx
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	public boolean manageTx(TransactionContext txContext){

		return true;
	}
	
	/**
	 * maintain dependences, e.g., arcs
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	public boolean manageDep(TransactionContext txContext){
		algorithm.analyze(txContext);
		return true;
	}
	
}
