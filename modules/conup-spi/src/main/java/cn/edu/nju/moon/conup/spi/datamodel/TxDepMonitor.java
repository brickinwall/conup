package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public interface TxDepMonitor {
	public boolean notify(TxEventType et, String curTxID);
	
	/**
	 * 
	 * @param txID current tx id
	 * @param compIdentifier target component
	 * @return
	 */
	public boolean isLastUse(String currentTxID, String targetCompIdentifier, String hostComp);
	
	/**
	 * when a root tx ends, TxDepMonitor should be notified.
	 * Given a parentTxId, which means that only the root Txs associated with the parentTxId
	 * @param hostComp
	 * @param rootTxId
	 * @return
	 */
	public void rootTxEnd(String hostComp, String rootTxId);
	
	/**
	 * With the given parentTx/rootTx, exactly remove 
	 * @param hostComp
	 * @param parentTxId
	 * @param rootTxId
	 */
	public void rootTxEnd(String hostComp, String parentTxId, String rootTxId);
	
	/**
	 * 
	 * @return a new instance of TxDepMonitor
	 */
	public TxDepMonitor newInstance();
	
	public boolean notifySubTxStart(String subComp, String curComp, String rootTx,
			String parentTx, String subTx);
	
	public boolean notifySubTxEnd(String subComp, String curComp, String rootTx,
			String parentTx, String subTx);
}
