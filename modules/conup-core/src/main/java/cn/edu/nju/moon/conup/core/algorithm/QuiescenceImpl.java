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
public class QuiescenceImpl implements Algorithm {
	/** dependence type is static dependent */
	public final static String STATIC_DEP = "STATIC_DEP";

	@Override
	public void manageDependence(TransactionContext txContext) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @param compName
	 * @return
	 */
	public boolean isPassive(String compName){
		return false;
	}

	@Override
	public boolean isInterceptRequired() {
		// TODO Auto-generated method stub
		return false;
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
	public boolean manageDependence(String proctocol,
			String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}

}
