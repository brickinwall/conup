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
	public void analyze(TransactionContext txContext);
	
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
