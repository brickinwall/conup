package cn.edu.nju.moon.conup.spi.datamodel;

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
	 * For BLOCKING, it does nothing
	 * For WAITING, it does nothing
	 * FOr CONCURRENT_VERSION, it will return which version implementation should be returned
	 * 
	 * @param rootTxID root transaction id
	 * @param rootComp root component object identifier
	 * @param parentComp parent component object identifier
	 * @param curTxID current transaction id
	 * @param hostComp host component object identifier
	 */
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
