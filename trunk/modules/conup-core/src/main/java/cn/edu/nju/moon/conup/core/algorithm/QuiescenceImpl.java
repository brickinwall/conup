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
	public void analyze(TransactionContext txContext) {
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
