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
	public void setCompObject(ComponentObject compObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stop(String contributionURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CompStatus getCompStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadyForUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void transitToValid() {
		// TODO Auto-generated method stub

	}

	@Override
	public void transitToNormal() {
		// TODO Auto-generated method stub

	}

	@Override
	public void transitToUpdating() {
		// TODO Auto-generated method stub

	}

	@Override
	public void transitToFree() {
		// TODO Auto-generated method stub

	}

	@Override
	public void transitToOndemand() {
		// TODO Auto-generated method stub
		
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
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFree() {
		// TODO Auto-generated method stub
		return false;
	}

}
