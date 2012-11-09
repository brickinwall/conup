package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class TransactionRegistryImpl  {
	private static TransactionRegistryImpl transactionRegistry = new TransactionRegistryImpl();
	private Map<String, TransactionContext> dependencies;
	
	private TransactionRegistryImpl(){
//		dependencies = new HashMap<String, TransactionDependency>();
		dependencies = new ConcurrentHashMap<String, TransactionContext>();
	}
	
	public static TransactionRegistryImpl getInstance(){
		return transactionRegistry;
	}
	
	public TransactionContext getTransactionContext(String transactionID) {
		return dependencies.get(transactionID);
	}

	public Map<String, TransactionContext> getTransactionContexts() {
		return dependencies;
	}

	/**
	 * @param transactionID
	 * @param transactionDependency
	 */
	public void addTransactionContext(String transactionID, TransactionContext transactionDependency) {
		dependencies.put(transactionID, transactionDependency);
	}

	/**
	 * @param transactionID
	 */
	public void removeTransactionContext(String transactionID) {
		dependencies.remove(transactionID);
	}
	
	

}
