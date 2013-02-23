package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class OndemandSetupHelperImpl implements OndemandSetupHelper {

	private ComponentObject compObj;
	
	@Override
	public boolean ondemandSetup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ondemandSetup(String sourceComponent, String proctocol,
			String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	@Override
	public void setOndemand(OndemandSetup ondemandSetup) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDynamicDepManager(DynamicDepManager depMgr) {
		// TODO Auto-generated method stub

	}

	@Override
	public DynamicDepManager getDynamicDepManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOndemandDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetIsOndemandRqstRcvd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDemandIsDone() {
		// TODO Auto-generated method stub
		
	}

}
