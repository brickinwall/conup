package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:04:20 PM
 */
public class TransactionRegistry  {
	/** take tx_id as key*/
	private Map<String, TransactionContext> txContexts;
	
	public TransactionRegistry(){
		txContexts = new ConcurrentHashMap<String, TransactionContext>();
	}
	
	public TransactionContext getTransactionContext(String transactionID) {
		return txContexts.get(transactionID);
	}

	public Map<String, TransactionContext> getTransactionContexts() {
		return txContexts;
	}

	/**
	 * @param transactionID
	 * @param transactionContext
	 */
	public void addTransactionContext(String transactionID, TransactionContext transactionContext) {
		txContexts.put(transactionID, transactionContext);
	}

	/**
	 * @param transactionID
	 */
	public void removeTransactionContext(String transactionID) {
		txContexts.remove(transactionID);
	}
	
	/**
	 * check whether this transactionID have been stored in txRegistry?
	 * @param transactionID
	 * @return 
	 */
	public boolean contains(String transactionID){
		return txContexts.containsKey(transactionID);
	}

	/**
	 * update TransactionContext in txContexts with respect transactionID
	 * @param transactionID
	 * @param transactionContext
	 */
	public void updateTransactionContext(String transactionID, TransactionContext transactionContext){
		txContexts.remove(transactionID);
		txContexts.put(transactionID, transactionContext);
	}
	
	public Set<String> getAllTxIds(){
		return txContexts.keySet();
	}
}
