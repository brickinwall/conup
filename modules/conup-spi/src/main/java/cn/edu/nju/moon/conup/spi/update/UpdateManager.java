package cn.edu.nju.moon.conup.spi.update;


import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

public interface UpdateManager {

	/**
	 * this method is invoked on target component
	 * there are 3 scenario where we call this method: 
	 * 1.when a tx finished; 
	 * 2.when a root tx finished;
	 * 3.when the CompStatus is VALID and a new request come in.(this need to be discussed) 
	 */
	public void attemptToUpdate();

	/**
	 * this method is used when some events have happened which will make the CompStatus becomes Free
	 * these events are: PastCreate(sub tx finished); PastRemove(root tx finished) 
	 * @param hostComp
	 */
	public void checkFreeness(String hostComp);
	
	/**
	 * do some clean works and also notify update is done
	 */
	public void cleanupUpdate();
	
	/**
	 * invoke specific ComponentUpdator to execute update
	 */
	public void executeUpdate();

	public ComponentUpdator getCompUpdator();

	public DynamicDepManager getDepMgr();
	
	public OndemandSetupHelper getOndemandSetupHelper();

	public DynamicUpdateContext getUpdateCtx();

	/**
	 * record all these old root tx id(definition in VC paper)
	 */
	public void initOldRootTxs();

	/**
	 * whether current component is target component
	 * @return
	 */
	public boolean isDynamicUpdateRqstRCVD();

	/**
	 * process message come from communication module
	 * @param reqObj
	 * @return
	 */
	public String processMsg(RequestObject reqObj);
	
	public void setCompUpdator(ComponentUpdator compUpdator);

	public void setDepMgr(DynamicDepManager depMgr);

	public void setDynamicUpdateContext(DynamicUpdateContext updateCtx);

	public void setOndemandSetupHelper(OndemandSetupHelper ondemandSetupHelper);

	public void checkUpdate(Interceptor interceptor);

	public void removeAlgorithmOldRootTx(String rootTxId);
	
	public boolean isUpdatedTo(String newVerId);
	
	public void dynamicUpdateIsDone();
	
	public boolean uninstall(String contributionURI);
	
	public boolean install(String contributionURI, String contributionURL);

	/**
	 * ondemand setup finished, need to change CompStatus and notify interceptors
	 */
	public void ondemandSetupIsDone();

	void achieveFree();

	public void remoteDynamicUpdateIsDone();

	public void ondemandSetting();

	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr);
}
