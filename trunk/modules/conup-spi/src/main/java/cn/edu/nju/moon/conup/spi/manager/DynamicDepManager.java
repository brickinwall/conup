/**
 * 
 */
package cn.edu.nju.moon.conup.spi.manager;


import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface DynamicDepManager {
	
	/**
	 * maintain tx
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	public boolean manageTx(TransactionContext txContext);
	
	/**
	 * maintain dependences, e.g., arcs
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	public boolean manageDep(TransactionContext txContext);
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * @param compName component name
	 * @return
	 */
	public boolean isInterceptRequired(String compName);
	
	/**
	 * is a component valid?
	 * @param compName
	 * @return 
	 */
	public boolean isValid(String compName);
	
	/**
	 * is a component ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate(String compName);
	
}
