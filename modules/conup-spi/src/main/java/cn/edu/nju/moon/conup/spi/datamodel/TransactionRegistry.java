package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;


public interface TransactionRegistry  {
	
	public TransactionContext getTransactionContext(String currentTransactionID);

	public Map<String, TransactionContext> getTransactionContexts();

	/** @param key current transaction id
	 * @param transactionDependency */
	public void addTransactionContext(String transactionID, TransactionContext transactionContext);

	/**
	 * @param key current transaction id
	 */
	public void removeTransactionContext(String transactionID);
	
}
