package cn.edu.nju.moon.conup.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.def.TransactionDependency;

public class TransactionRegistryImpl implements TransactionRegistry {
	private static TransactionRegistry transactionRegistry = new TransactionRegistryImpl();
	/**
	 * @param
	 * tx id as key
	 */
	private Map<String, TransactionDependency> dependencies;
	
	private TransactionRegistryImpl(){
		dependencies = new ConcurrentHashMap<String, TransactionDependency>();
	}
	
	public static TransactionRegistry getInstance(){
		return transactionRegistry;
	}

	@Override
	public TransactionDependency getDependency(String currentTransaction) {
		return dependencies.get(currentTransaction);
	}

	@Override
	public Map<String, TransactionDependency> getDependencies() {
		return dependencies;
	}

	/** @param key current transaction
	 * @param transactionDependency */
	@Override
	public void addDependency(String key, TransactionDependency transactionDependency) {
		dependencies.put(key, transactionDependency);
	}

	@Override
	public void removeDependecy(String key) {
		dependencies.remove(key);
	}
	
	

}
