package cn.edu.nju.moon.conup.spi.helper;

import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;


/**
 * 
 * A general interface for different update algorithm(Version-consistency, quiescence and tranquillity).
 * Since the process of on-demand setup is different,  each algorithm should provide a class implements the interface
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface OndemandSetup {
	/**
	 * represent the process of on-demand setup, this method is supposed to be 
	 * invoked by CompLifecycleManager.
	 */
	public boolean ondemand();
	
	/**
	 * received on-demand notification from peer component
	 * @param srcIdentifier source component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean ondemandSetup(String srcIdentifier, String proctocol, String payload);
	
	/**
	 * Since the on-demand setup is asynchronous, the method is used to query on-demand setup status
	 * @return
	 */
	public boolean isOndemandDone();
	
	/**
	 * Each algorithm implementation should have an identifier/type that 
	 * uniquely identify itself.
	 * @return
	 */
	public String getAlgorithmType();
	
	/**
	 * @param ondemandHelper
	 */
	public void setOndemandHelper(OndemandSetupHelper ondemandHelper);
	
	/**
	 * set a TxDepRegistry to OndemandSetup, although this parameter is not useful for Quiescence
	 * OndemandSetup and TxDepRegistry is 1-1 with ComponentObject
	 * @param txDepMonitor
	 */
	public void setTxDepRegistry(TxDepRegistry txDepRegistry);
	
	public void onDemandIsDone();

	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr);

	public void setDepMgr(DynamicDepManager depMgr);
	
}
