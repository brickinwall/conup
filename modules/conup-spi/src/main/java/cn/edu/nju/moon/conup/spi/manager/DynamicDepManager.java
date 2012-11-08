/**
 * 
 */
package cn.edu.nju.moon.conup.spi.manager;


import cn.edu.nju.moon.conup.spi.datamodel.DependenceRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface DynamicDepManager {
	
	/**
	 * maintain tx
	 * @param txContext
	 * @return
	 */
	public boolean manageTx(TransactionContext txContext);
	
	/**
	 * maintain dependences, e.g., dependences
	 * @param txContext
	 * @return
	 */
	public boolean manageDependence(TransactionContext txContext);
	
	/**
	 * received dependences notification from peer component
	 * @param targetComp target component's name
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean manageDependence(String proctocol, String msgType, String payload);
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * @param compName component name
	 * @return
	 */
	public boolean isInterceptRequired();
	
	/**
	 * is component object valid?
	 * @return 
	 */
	public boolean isValid();
	
	/**
	 * is component object ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate();
	
	/**
	 * 
	 * @return TransactionRegistry
	 */
	public TransactionRegistry getTxRegisty();
	
	/**
	 * @return the Scope
	 */
	public Scope getScope();
	/**
	 * 
	 * @return DependenceRegistry
	 */
	public DependenceRegistry getDepRegistry();
	
}
