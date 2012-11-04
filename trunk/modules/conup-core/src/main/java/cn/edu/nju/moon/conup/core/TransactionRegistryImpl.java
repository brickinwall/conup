package cn.edu.nju.moon.conup.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


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

	public TransactionContext getDependency(String currentTransaction) {
		return dependencies.get(currentTransaction);
	}

	public Map<String, TransactionContext> getDependencies() {
		return dependencies;
	}

	/** @param key current transaction
	 * @param transactionDependency */
	public void addDependency(String key, TransactionContext transactionDependency) {
		dependencies.put(key, transactionDependency);
	}

	public void removeDependecy(String key) {
		dependencies.remove(key);
	}
	
	

}
