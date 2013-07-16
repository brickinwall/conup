/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

import cn.edu.nju.moon.conup.spi.helper.FreenessCallback;

/**
 * An abstract description of the strategies for achieving freeness, 
 * i.e., Blocking, waiting and concurrent_version
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface FreenessStrategy {
	/**
	 * This method is an abstract operation for different freeness strategy.
	 * For BLOCKING, it's supposed to decide whether a request should be blocked.
	 * For WAITING, it will check whether a component is ready for update.
	 * FOr CONCURRENT_VERSION, it will create a dispatcher.
	 * 
	 * @param rootTxID root transaction id
	 * @param rootComp root component object identifier
	 * @param parentComp parent component object identifier
	 * @param curTxID current transaction id
	 * @param hostComp host component object identifier
	 */
//	public void achieveFreeness(String rootTxID, String rootComp, String parentComp, 
//			String curTxID, String hostComp, FreenessCallback fcb);
	public Class<?> achieveFreeness(String rootTxID, String rootComp, String parentComp, 
			String curTxID, String hostComp);
	
	/**
	 * Each freeness strategy implementation should have a unique type identifier.
	 * @return the type of the freeness strategy
	 */
	public String getFreenessType();
	
	/**
	 * check whether current tx needs to be intercepted?
	 * @param rootTx
	 * @param compIdentifer 
	 * @param txCtx
	 * @param isUpdateReqRCVD is dynamic update request received for the component
	 * @return
	 */
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateReqRCVD);
	
	/**
	 * The concrete freeness strategy should take both strategy impl and algorithm impl into 
	 * consideration while judging whether the component is ready for update.
	 * @return
	 */
	public boolean isReadyForUpdate(String hostComp);

}
