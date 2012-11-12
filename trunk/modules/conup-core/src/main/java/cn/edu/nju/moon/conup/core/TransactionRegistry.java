package cn.edu.nju.moon.conup.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;



public class TransactionRegistry  {
	private static TransactionRegistry transactionRegistry = new TransactionRegistry();
	private Map<String, TransactionContext> txContexts;
	
	private TransactionRegistry(){
//		dependencies = new HashMap<String, TransactionDependency>();
		txContexts = new ConcurrentHashMap<String, TransactionContext>();
	}
	
	public static TransactionRegistry getInstance(){
		return transactionRegistry;
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
}
