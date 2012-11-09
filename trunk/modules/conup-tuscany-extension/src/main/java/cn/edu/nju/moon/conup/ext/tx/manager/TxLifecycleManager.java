/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

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
	private Map<String, TransactionContext> associateTx = new ConcurrentHashMap<String, TransactionContext>();
	
	/**
	 * create transaction id
	 * @return
	 */
	public String createID(){
		String txID = null;
		String threadID = null;
		
		//TODO generate txID
		//TOTO generate threadID
		
		TransactionContext txContext;
		txContext = associateTx.get(threadID);
		txContext.setCurrentTx(txID);
		
		//update interceptor cache
		
		//remove current tx from associateTx
		
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
	
	/**
	 * invoked by trace interceptor
	 * @param threadID
	 * @param txContext
	 */
	public void addTx(String threadID, TransactionContext txContext){
		associateTx.put(threadID, txContext);
	}
}
