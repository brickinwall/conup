package cn.edu.nju.moon.conup.spi.update;


import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

public interface UpdateManager {

	public void attemptToUpdate();

	void checkFreeness(String hostComp);
	
	public boolean cleanupUpdate();
	
	public boolean executeUpdate();

	public ComponentUpdator getCompUpdator();

	public DynamicDepManager getDepMgr();
	
	public OndemandSetupHelper getOndemandSetupHelper();

	public DynamicUpdateContext getUpdateCtx();

	public void initOldRootTxs();

	public boolean isDynamicUpdateRqstRCVD();

	public String process(RequestObject reqObj);
	
	public void setCompUpdator(ComponentUpdator compUpdator);

	public void setDepMgr(DynamicDepManager depMgr);

	void setDynamicUpdateContext(DynamicUpdateContext updateCtx);

	public void setOndemandSetupHelper(OndemandSetupHelper ondemandSetupHelper);

//	public Message checkRemoteUpdate(TransactionContext txCtx, Object subTx,
//			Interceptor interceptor, Message msg);

	public void checkUpdate(Interceptor interceptor);
	
}
