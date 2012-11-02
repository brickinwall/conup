/**
 * 
 */
package cn.edu.nju.moon.conup.core.freeness;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;

/**
 * Implementation of waiting strategy for achieving freeness
 * @author Jiang Wang
 *
 */
public class WaitingStrategy implements FreenessStrategy {

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy#isInterceptRequired(java.lang.String)
	 */
	@Override
	public boolean isInterceptRequired(String compName) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String compName) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy#isReadyForUpdate(java.lang.String)
	 */
	@Override
	public boolean isReadyForUpdate(String compName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void manage(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp) {
		// TODO Auto-generated method stub
		
	}

}
