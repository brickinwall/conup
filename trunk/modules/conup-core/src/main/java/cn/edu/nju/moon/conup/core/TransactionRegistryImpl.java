package cn.edu.nju.moon.conup.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


public class TransactionRegistryImpl implements TransactionRegistry {
	private static TransactionRegistry transactionRegistry = new TransactionRegistryImpl();
	private Map<String, TransactionContext> dependencies;
	
	private TransactionRegistryImpl(){
//		dependencies = new HashMap<String, TransactionDependency>();
		dependencies = new ConcurrentHashMap<String, TransactionContext>();
	}
	
	public static TransactionRegistry getInstance(){
		return transactionRegistry;
	}

	@Override
	public TransactionContext getDependency(String currentTransaction) {
		return dependencies.get(currentTransaction);
	}

	@Override
	public Map<String, TransactionContext> getDependencies() {
		return dependencies;
	}

	/** @param key current transaction
	 * @param transactionDependency */
	@Override
	public void addDependency(String key, TransactionContext transactionDependency) {
		dependencies.put(key, transactionDependency);
	}

	@Override
	public void removeDependecy(String key) {
		dependencies.remove(key);
	}
	
	

}
