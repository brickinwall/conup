/**
 * 
 */
package cn.edu.nju.moon.conup.core.manager.impl;


import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class DynamicDepManagerImpl implements DynamicDepManager{
	private Algorithm algorithm = null;
	
	public DynamicDepManagerImpl(){
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
	@Override
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
	@Override
	public boolean manageDep(TransactionContext txContext){
		algorithm.analyze(txContext);
		return true;
	}

	@Override
	public boolean isInterceptRequired(String compName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid(String compName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadyForUpdate(String compName) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
