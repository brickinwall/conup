package cn.edu.nju.moon.conup.core.ondemand;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.core.algorithm.IdleImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

public class IdleOndemandSetupImpl implements OndemandSetup {
	private Logger LOGGER = Logger.getLogger(IdleOndemandSetupImpl.class.getName());
	private OndemandSetupHelper ondemandHelper;

	@Override
	public boolean ondemand(Scope scope) {
		LOGGER.info("receive ondemand message in IDLEOndemand...");
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
		UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(hostComp);
		updateMgr.ondemandSetting();
		updateMgr.ondemandSetupIsDone();
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
		return IdleImpl.ALGORITHM_TYPE;
	}

	@Override
	public void setOndemandHelper(OndemandSetupHelper ondemandHelper) {
		this.ondemandHelper = ondemandHelper;
	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDemandIsDone() {
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
