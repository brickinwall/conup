package cn.edu.nju.moon.conup.spi.update;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;

public interface CompLifecycleManager extends Observer{

	public DynamicDepManager getDepMgr();

	public void setDepMgr(DynamicDepManager depMgr);

//	public ComponentUpdator getCompUpdator();
//
//	public void setCompUpdator(ComponentUpdator compUpdator);

	/**
	 * 
	 * @param contributionURI
	 * @return
	 */
	public boolean uninstall(String contributionURI);

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
	 * stop a contribution
	 * @param contributionURI
	 * @return
	 */
	public boolean stop(String contributionURI);

	/**
	 * 
	 * @param implType the implementation type of the component, e.g., POJO, EJB.
	 * @param baseDir
	 * @param classPath
	 * @param contributionURI
	 * @param compositeURI
	 * @param compIdentifier target component's identifier
	 * @return
	 */
//	public boolean update(String baseDir, String classFilePath,
//			String contributionURI, String compositeURI, String compIdentifier);

//	public boolean attemptToUpdate();
//
//	public boolean executeUpdate();
//
//	public boolean cleanupUpdate();

//	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx);

	public void setCompObject(ComponentObject compObj);

	public abstract ComponentObject getCompObject();

	/**
	 * whethe the component is updated to new version?
	 * ATTENTION: temporally, the parameter is of no use 
	 * @param newVerId new version id of the component
	 * @return
	 */
	public boolean isUpdatedTo(String newVerId);

	/**
	 * it's a user-oriented method which is used for remote configuration
	 * @param payload
	 * @return
	 */
//	public boolean remoteConf(String payload);

	/**
	 * if a component has received dynamic update request and is in the process of finishing update, return true.
	 * @return
	 */
//	public boolean isDynamicUpdateRqstRCVD();

//	public boolean initOldRootTxs();

//	/**
//	 * re-calculate the old root tx sets, no matter whether the sets are initiated.
//	 * @return
//	 */
//	public boolean reinitOldRootTxs();

//	public DynamicUpdateContext getUpdateCtx();
	
//	/**
//	 * check for freeness
//	 * move from TxDepMonitor
//	 */
//	public void checkFreeness(String hostComp);

}