package cn.edu.nju.moon.conup.spi.tx;

import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public interface TxDepMonitor {
	
	/**
	 * notified by suping's lddm to get Tx information(status, futureC, pastC etc.)
	 * @param et
	 * @param curTxID
	 * @return
	 */
	public boolean notify(TxEventType et, String curTxID);
	
	/**
	 * 
	 * @param txID current tx id
	 * @param compIdentifier target component
	 * @return
	 */
	public boolean isLastUse(String currentTxID, String targetCompIdentifier, String hostComp);
	
	/**
	 * every component has a TxDepRegistry which is stored in TxDepMonitor
	 * @return txDepRegistry
	 */
	public TxDepRegistry getTxDepRegistry();
	
	/**
	 * in some place, we can only know the serviceName, here we need to convert them
	 * @param serviceName
	 * @param hostComp
	 * @return
	 */
	public String convertServiceToComponent(String serviceName, String hostComp);
	
}
