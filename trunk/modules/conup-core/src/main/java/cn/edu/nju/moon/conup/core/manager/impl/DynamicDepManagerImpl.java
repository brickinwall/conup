/**
 * 
 */
package cn.edu.nju.moon.conup.core.manager.impl;


import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.DependenceRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
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
		algorithm = new AlgorithmFactory().createAlgorithm();
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
	public boolean manageDependence(TransactionContext txContext){
		algorithm.manageDependence(txContext);
		return true;
	}

	@Override
	public boolean isInterceptRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean manageDependence(String proctocol,
			String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TransactionRegistry getTxRegisty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DependenceRegistry getDepRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadyForUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Scope getScope() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
