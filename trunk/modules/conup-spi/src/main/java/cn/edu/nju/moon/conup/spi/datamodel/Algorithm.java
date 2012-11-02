/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Set;

/**
 * Interface for different dynamic update algorithm, i.e., Version-consistency, Quiescence 
 * and Tranquillity
 * 
 * @author Jiang Wang
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
}
