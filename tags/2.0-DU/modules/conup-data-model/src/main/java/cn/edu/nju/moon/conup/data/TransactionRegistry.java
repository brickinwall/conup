package cn.edu.nju.moon.conup.data;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.def.TransactionDependency;


/** it's used to manage transaction and its root and parent transactions. */
public interface TransactionRegistry {
	/** return given transaction's root and parent transactions, which is encapsulated in a
	 * TransactionDependency object.
	 *  */
	public TransactionDependency getDependency(String key);
	/** return registry */
	public Map<String, TransactionDependency> getDependencies();
	/** add transaction dependency */
	public void addDependency(String key, TransactionDependency transactionDependency);
	/** remove transaction dependency */
	public void removeDependecy(String key);

}
