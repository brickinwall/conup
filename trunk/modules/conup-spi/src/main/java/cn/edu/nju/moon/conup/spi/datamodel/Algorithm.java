/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Set;

/**
 * Interface for different dynamic update algorithm, i.e., Version-consistency, Quiescence 
 * and Tranquillity
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public interface Algorithm {
	/** represent version-consistency algorithm */
	public final static String CONSISTENCY_ALGORITHM = "CONSISTENCY_ALGORITHM";
	
	/** represent quiescence algorithm */
	public final static String QUIESCENCE_ALGORITHM = "QUIESCENCE_ALGORITHM";
	
	/** represent tranquillity algorithm */
	public final static String TRANQUILLITY_ALGORITHM = "TRANQUILLITY_ALGORITHM";
	
	/**
	 * 	It's used to analyze txStatus and maintain related dependences for it.
	 * 	@param txContext 
	 * 
	 * */
	public void manageDependence(TransactionContext txContext);
	
	/**
	 * received dependences notification from peer component
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean manageDependence(String proctocol, String msgType, String payload);
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * @return
	 */
	public boolean isInterceptRequired();
	
	/**
	 * is a component valid?
	 * @return 
	 */
	public boolean isValid();
	
	/**
	 * is a component ready?
	 * @return 
	 */
	public boolean isReadyForUpdate();
}
