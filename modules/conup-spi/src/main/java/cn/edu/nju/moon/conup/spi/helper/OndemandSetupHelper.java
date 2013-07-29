package cn.edu.nju.moon.conup.spi.helper;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * An OndemandSetupHelper may be invoked by tuscany-extension module
 * or peer commponents.
 * 
 * @author Jiang Wang<jiang.wang88@gmail.com>
 *
 */
public interface OndemandSetupHelper{
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	public boolean ondemandSetup();
	
	/**
	 * received on-demand notification from peer component
	 * @param sourceComponent source component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean ondemandSetup(String sourceComponent, String proctocol, String payload);

	/**
	 * @return corresponding component object of the helper
	 */
	public ComponentObject getCompObject();
	
	/**
	 * @param compObj ComponentObject
	 */
	public void setCompObject(ComponentObject compObj);
	
	/**
	 * @param ondemandSetup
	 */
	public void setOndemand(OndemandSetup ondemandSetup);
	
	/**
	 * @returnOndemandSetup
	 */
	public OndemandSetup getOndemand();
	
	/**
	 * @param depMgr
	 */
	public void setDynamicDepManager(DynamicDepManager depMgr);
	
	/**
	 * @return
	 */
//	public DynamicDepManager getDynamicDepManager();
	
	/**
	 * @return whether ondemnad setup is done
	 */
	public boolean isOndemandDone();
	
	/**
	 * reset isOndemandRqstRcvd to false
	 */
//	public void resetIsOndemandRqstRcvd();
	
	/**
	 * do cleanup works when ondemand is done
	 */
	public void onDemandIsDone();

	public void setCompLifeCycleMgr(CompLifeCycleManager compLifecycleManager);
}
