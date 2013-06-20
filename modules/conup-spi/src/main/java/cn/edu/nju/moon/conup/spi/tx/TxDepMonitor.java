package cn.edu.nju.moon.conup.spi.tx;

import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;

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
	
//	/**
//	 * when a root tx ends, TxDepMonitor should be notified.
//	 * Given a parentTxId, which means that only the root Txs associated with the parentTxId
//	 * @param hostComp
//	 * @param rootTxId
//	 * @return
//	 */
//	// move
//	public void rootTxEnd(String hostComp, String rootTxId);
	
//	/**
//	 * 
//	 * @return a new instance of TxDepMonitor
//	 */
//	public TxDepMonitor newInstance();
	
	public String convertServiceToComponent(String service, String hostComp);
	
//	/**
//	 * the host component started a sub-transaction on a remote component
//	 * @param subComp
//	 * @param curComp
//	 * @param rootTx
//	 * @param parentTx
//	 * @param subTx
//	 * @return
//	 */
//	// move
//	public boolean startRemoteSubTx(String subComp, String curComp, String rootTx,
//			String parentTx, String subTx);
//	
//	/**
//	 * a sub-transaction just ended and returned from a remote component 
//	 * @param subComp
//	 * @param curComp
//	 * @param rootTx
//	 * @param parentTx
//	 * @param subTx
//	 * @return
//	 */
//	// move
//	public boolean endRemoteSubTx(String subComp, String curComp, String rootTx,
//			String parentTx, String subTx);
//	
//	/**
//	 * the host component is going to init a sub-transaction for another component.
//	 * However, the sub-transaction has not truely been started.
//	 * 
//	 * @param hostComp
//	 * @param fakeSubTx the fake tx id
//	 * @param rootTx
//	 * @param rootComp
//	 * @param parentTx
//	 * @param parentComp
//	 * @return
//	 */
//	// move
//	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp);
//	
//	/**
//	 * 
//	 * @param hostComp
//	 * @param fakeSubTx
//	 * @return
//	 */
//	// move
//	public boolean endLocalSubTx(String hostComp, String fakeSubTx);
	
//	/**
//	 * check for freeness
//	 */
//	// move to CompLifeCycleMgr
//	public void checkFreeness(String hostComp);
	
}
