package cn.edu.nju.moon.conup.spi.update.impl;

import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

public class CompLifeCycleManagerImpl implements CompLifeCycleManager {

	@Override
	public ComponentObject getCompObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DynamicDepManager getDepMgr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean install(String contributionURI, String contributionURL) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUpdatedTo(String newVerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stop(String contributionURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean uninstall(String contributionURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNormal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOndemandSetting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void ondemandSetting() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOndemandSetupRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void ondemandSetupIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dynamicUpdateIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updating() {
		// TODO Auto-generated method stub

	}

	@Override
	public void achievedFree() {
		// TODO Auto-generated method stub

	}

	@Override
	public void remoteDynamicUpdateIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public CompStatus getCompStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getOndemandSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValidToFreeSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getUpdatingSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getFreezeSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateIsReceived() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isTargetComp() {
		// TODO Auto-generated method stub
		return false;
	}

}
