package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.core.algorithm.QuiescenceImpl;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class QuiescenceOndemandSetupImpl implements OndemandSetup {
	private OndemandSetupHelper ondemandHelper;
	@Override
	public boolean ondemand() {
		ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
		return true;
	}

	@Override
	public boolean ondemandSetup(String targetIdentifier, String proctocol, String payload) {
		ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
		return true;
	}

	@Override
	public String getAlgorithmType() {
		return QuiescenceImpl.ALGORITHM_TYPE;
	}

	@Override
	public void setOndemandHelper(OndemandSetupHelper ondemandHelper) {
		this.ondemandHelper = ondemandHelper;
	}

	@Override
	public boolean isOndemandDone() {
		return true;
	}

	@Override
	public void onDemandIsDone() {
		// TODO Auto-generated method stub
		
	}
}
