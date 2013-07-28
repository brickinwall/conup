package cn.edu.nju.moon.conup.spi.update;

import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * CompLifecycleManager only manage the life cycle of the component
 * do not care about the update, the execution of update is delegate to UpdateManager
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * Jul 26, 2013 10:06:39 PM
 */
public interface CompLifeCycleManager{

	public abstract ComponentObject getCompObject();

	public DynamicDepManager getDepMgr();

	/**
	 * 
	 * @param domainURI
	 * @param contributionURI
	 * @param contributionURL
	 * @param compositeURI
	 * @return
	 */
	public boolean install(String contributionURI,
			String contributionURL);

	/**
	 * whethe the component is updated to new version?
	 * ATTENTION: temporally, the parameter is of no use 
	 * @param newVerId new version id of the component
	 * @return
	 */
	public boolean isUpdatedTo(String newVerId);

	public void setCompObject(ComponentObject compObj);

	public void setDepMgr(DynamicDepManager depMgr);

	/**
	 * stop a contribution
	 * @param contributionURI
	 * @return
	 */
	public boolean stop(String contributionURI);

	/**
	 * 
	 * @param contributionURI
	 * @return
	 */
	public boolean uninstall(String contributionURI);

	boolean isValid();

	boolean isNormal();

	boolean isOndemandSetting();

	/**
	 * ondemand setup is executing
	 * set CompStatus to ONDEMAND
	 */
	void ondemandSetting();

	/**
	 * if CompStatus is Normal or ONDEMAND
	 * return true meands need to wait for ondemand finished
	 * @return
	 */
	boolean isOndemandSetupRequired();

	/**
	 * ondemand setup finished, need to change CompStatus to Valid
	 */
	void ondemandSetupIsDone();
	
	/**
	 * update is finished, need to reset CompStatus to Normal
	 */
	void dynamicUpdateIsDone();

	/**
	 * the component is executing upate, need to set CompStatus to Updating
	 */
	void updating();

	/**
	 * the free condition has achieved, so need to set CompStatus to Free
	 */
	void achievedFree();

	/**
	 * remote update is finished, current CompStatus needs to set to Normal
	 */
	void remoteDynamicUpdateIsDone();

	public CompStatus getCompStatus();

	/**
	 * if a component has received dynamic update request and is in the process of finishing update, return true.
	 * @return
	 */
//	public boolean isDynamicUpdateRqstRCVD();
	
	/**
	 * @return a synchronization monitor for suspending threads while executing ondemand setup
	 */
	public Object getOndemandSyncMonitor();
	
	/**
	 * @return a synchronization monitor for suspending threads while the component trying to be free for dynamic update
	 */
	public Object getValidToFreeSyncMonitor();

	/**
	 * @return a synchronization monitor for suspending threads while executing dynamic update
	 */
	public Object getUpdatingSyncMonitor();
	
	public Object getFreezeSyncMonitor();

	/**
	 * current component is target component
	 * set the flag to true
	 */
	public void updateIsReceived();

	public boolean isTargetComp();
}