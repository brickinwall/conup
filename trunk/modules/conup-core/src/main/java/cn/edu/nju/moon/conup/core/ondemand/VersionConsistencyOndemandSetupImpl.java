/**
 * 
 */
package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;


/**
 * 
 * The on-demand setup process of Version-consistency algorithm
 * @author nju
 *
 */
public class VersionConsistencyOndemandSetupImpl implements OndemandSetup {

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.core.ondemand.Ondemand#ondemand(java.lang.String, java.lang.String)
	 */
	public void ondemand(String freenessSetup, Scope scope) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.core.ondemand.Ondemand#isOndemandDone()
	 */
	public boolean isOndemandDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ondemandSetup(String targetIdentifier, String proctocol,
			String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlgorithmType() {
		return VersionConsistencyImpl.ALGORITHM_TYPE;
	}

}
