package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


public interface TransactionRegistry  {
	
	public TransactionContext getDependency(String currentTransaction);

	public Map<String, TransactionContext> getDependencies();

	/** @param key current transaction
	 * @param transactionDependency */
	public void addDependency(String key, TransactionContext transactionDependency);

	/**
	 * @param key current transaction id
	 */
	public void removeDependecy(String key);
	
}
