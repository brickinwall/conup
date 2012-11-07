/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * An abstract description of the strategies for achieving freeness, 
 * i.e., Blocking, waiting and concurrent_version
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface FreenessStrategy {
	/** represent blocking strategy */
	public final static String BLOCKING = "BLOCKING_FOR_FREENESS";
	
	/** represent waiting strategy */
	public final static String WAITING = "WAITING_FOR__FREENESS";
	
	/** represent concurrent version strategy */
	public final static String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
	
	/**
	 * manage() is an abstract operation for different freeness strategy.
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
	public void manage(String rootTxID, String rootComp, String parentComp, 
			String curTxID, String hostComp);
	

}
