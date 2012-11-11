/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;


/**
 * Interface for different dynamic update algorithm, i.e., Version-consistency, Quiescence 
 * and Tranquillity
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public interface Algorithm {
	
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
	
	/**
	 * Each algorithm implementation should have an identifier/type that 
	 * uniquely identify itself.
	 * @return
	 */
	public String getAlgorithmType();
	
}
