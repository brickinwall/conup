package cn.edu.nju.moon.conup.communication.services;

import org.oasisopen.sca.annotation.Remotable;

import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.Scope;

@Remotable
public interface OndemandService {
	public boolean reqOndemandSetup(String currentComponent,
			String requestSourceComponent, Scope scope, String freenessSetup);
	public boolean confirmOndemandSetup(String parentComponent, String currentComponent);
//	public boolean onDemandSetUp();
	public boolean notifyFutureOndemand(Arc arc);
	public boolean notifyPastOndemand(Arc arc);
	public boolean notifySubFutureOndemand(Arc arc);
	public boolean notifySubPastOndemand(Arc arc);
}
