package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class VersionConsistencyOndemandSetupImpl implements OndemandSetup {
	/** represent version-consistency algorithm */
	public final static String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
	@Override
	public boolean ondemand(Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ondemandSetup(String srcIdentifier, String proctocol,
			String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOndemandDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlgorithmType() {
		return ALGORITHM_TYPE;
	}

	@Override
	public void setOndemandHelper(OndemandSetupHelper ondemandHelper) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDemandIsDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		
	}

}
