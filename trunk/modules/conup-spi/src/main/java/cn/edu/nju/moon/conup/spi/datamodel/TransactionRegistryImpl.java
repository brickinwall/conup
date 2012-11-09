package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class TransactionRegistryImpl  {
	private static TransactionRegistryImpl transactionRegistry = new TransactionRegistryImpl();
	private Map<String, TransactionContext> txContexts;
	
	private TransactionRegistryImpl(){
//		dependencies = new HashMap<String, TransactionDependency>();
		txContexts = new ConcurrentHashMap<String, TransactionContext>();
	}
	
	public static TransactionRegistryImpl getInstance(){
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
	
	

}
