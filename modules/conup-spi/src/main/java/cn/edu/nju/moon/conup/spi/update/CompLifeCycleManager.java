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
public interface CompLifeCycleManager {

	/**
	 * the free condition has achieved, so need to set CompStatus to Free
	 */
	void achievedFree();

	/**
	 * update is finished, need to reset CompStatus to Normal
	 */
	void dynamicUpdateIsDone();

	public abstract ComponentObject getCompObject();

	public CompStatus getCompStatus();

	public DynamicDepManager getDepMgr();

	public Object getFreezeSyncMonitor();

	/**
	 * @return a synchronization monitor for suspending threads while executing ondemand setup
	 */
	public Object getOndemandSyncMonitor();

	/**
	 * @return a synchronization monitor for suspending threads while executing dynamic update
	 */
	public Object getUpdatingSyncMonitor();

	/**
	 * @return a synchronization monitor for suspending threads while the component trying to be free for dynamic update
	 */
	public Object getValidToFreeSyncMonitor();

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

	boolean isNormal();

	boolean isOndemandSetting();

	/**
	 * if CompStatus is Normal or ONDEMAND
	 * return true meands need to wait for ondemand finished
	 * @return
	 */
	boolean isOndemandSetupRequired();

	public boolean isTargetComp();
	
	/**
	 * whethe the component is updated to new version?
	 * ATTENTION: temporally, the parameter is of no use 
	 * @param newVerId new version id of the component
	 * @return
	 */
	public boolean isUpdatedTo(String newVerId);

	boolean isValid();

	/**
	 * ondemand setup is executing
	 * set CompStatus to ONDEMAND
	 */
	void ondemandSetting();

	/**
	 * ondemand setup finished, need to change CompStatus to Valid
	 */
	void ondemandSetupIsDone();

	/**
	 * remote update is finished, current CompStatus needs to set to Normal
	 */
	void remoteDynamicUpdateIsDone();

	/**
	 * if a component has received dynamic update request and is in the process of finishing update, return true.
	 * @return
	 */
//	public boolean isDynamicUpdateRqstRCVD();
	
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

	/**
	 * current component is target component
	 * set the flag to true
	 */
	public void updateIsReceived();

	/**
	 * the component is executing upate, need to set CompStatus to Updating
	 */
	public void updating();

	public boolean isReadyForUpdate();
}