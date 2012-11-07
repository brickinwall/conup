/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;

/**
 * 
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TxLifecycleManager {
	/**
	 * TX_IDS takes transactionID and ComponentObject as key and value respectively.
	 */
	public static Map<String, ComponentObject> TX_IDS = new ConcurrentHashMap<String, ComponentObject>();
	
	/**
	 * create transaction id
	 * @return
	 */
	public String createID(){
		String txID = null;
		
		return txID;
	}
	
	/**
	 * @param id the transaction id that needs to be destroyed
	 */
	public void destroyID(String id){
		
	}
	
	/**
	 * 
	 * @return total transactions that are running
	 */
	public int getTxs(){

		return 0;
	}
}
