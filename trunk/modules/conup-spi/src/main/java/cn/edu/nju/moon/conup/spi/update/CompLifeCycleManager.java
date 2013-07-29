package cn.edu.nju.moon.conup.spi.update;

import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;

/**
 * CompLifecycleManager only manage the life cycle of the component
 * do not care about the update, the execution of update is delegate to UpdateManager
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * Jul 26, 2013 10:06:39 PM
 */
public interface CompLifeCycleManager {

	public ComponentObject getCompObject();

	public CompStatus getCompStatus();

	/**
	 * if CompStatus is Normal or ONDEMAND
	 * return true meands need to wait for ondemand finished
	 * @return
	 */
	boolean isOndemandSetupRequired();

	/**
	 * whethe the component is updated to new version?
	 * ATTENTION: temporally, the parameter is of no use 
	 * @param newVerId new version id of the component
	 * @return
	 * updateManager
	 */
//	public boolean isUpdatedTo(String newVerId);

	public void setCompObject(ComponentObject compObj);
	
	/**
	 * stop a contribution
	 * @param contributionURI
	 * @return
	 */
	public boolean stop(String contributionURI);
	
//	/**
//	 * 
//	 * @param contributionURI
//	 * @return
//	 */
//	public boolean uninstall(String contributionURI);

	public boolean isReadyForUpdate();

	public void transitToNormal();
	
	public void transitToOndemand();

	public void transitToValid();

	void transitToFree();

	/**
	 * when comp is executing update, we need to change compStatus to Updating
	 */
	public void transitToUpdating();
}