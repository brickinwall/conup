package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.core.algorithm.TranquillityImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TranquillityOndemandSetupImpl implements OndemandSetup {

	@Override
	public void ondemand(String freenessSetup,
			Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
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
		return TranquillityImpl.ALGORITHM_TYPE;
	}

}
