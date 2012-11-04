/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


/**
 * @author Jiang Wang  <jiang.wang88@gmail.com>
 *
 */
public class VersionConsistencyImpl implements Algorithm {
	/** dependence type is "future" */
	public final static String FUTURE_DEP = "FUTURE_DEP";
	/** dependence type is "past" */
	public final static String PAST_DEP = "PAST_DEP";
	
	@Override
	public void analyze(TransactionContext txContext) {
		// TODO Auto-generated method stub
		
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
