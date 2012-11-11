/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TranquillityImpl implements Algorithm {
	/** represent tranquillity algorithm */
	public final static String ALGORITHM_TYPE = "TRANQUILLITY_ALGORITHM";
	
	@Override
	public void manageDependence(TransactionContext txContext) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean manageDependence(String proctocol,
			String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlgorithmType() {
		return TranquillityImpl.ALGORITHM_TYPE;
	}

}
