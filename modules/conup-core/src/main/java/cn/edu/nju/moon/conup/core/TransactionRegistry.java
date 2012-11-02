package cn.edu.nju.moon.conup.core;

import java.util.Map;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


/** it's used to manage transaction and its root and parent transactions. */
public interface TransactionRegistry {
	/** return given transaction's root and parent transactions, which is encapsulated in a
	 * TransactionDependency object.
	 *  */
	public TransactionContext getDependency(String key);
	/** return registry */
	public Map<String, TransactionContext> getDependencies();
	/** add transaction dependency */
	public void addDependency(String key, TransactionContext transactionDependency);
	/** remove transaction dependency */
	public void removeDependecy(String key);

}
