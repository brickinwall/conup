/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.Set;


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
	 * @param 
	 * @param payload(:msgType XML, JSON, etc.)
	 * @return
	 */
	public boolean manageDependence(String payload);
	
//	/**
//	 * It's used by interceptor for deciding whether a request needs to be intercepted
//	 * @return
//	 */
//	public boolean isInterceptRequired();
//	
//	/**
//	 * is a component valid?
//	 * @return 
//	 */
//	public boolean isValid();
	
	/**
	 * is a component ready?
	 * @return 
	 */
	public boolean isReadyForUpdate(String compIdentifier);
	
	/**
	 * @param all the dependences depended by other components
	 * @return root transactions that should be responded with old version component
	 */
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps);
	
	/**
	 * Each algorithm implementation should have an identifier/type that 
	 * uniquely identify itself.
	 * @return
	 */
	public String getAlgorithmType();
	
	/**
	 * 
	 * @param algorithmOldVersionRootTxs
	 * @param bufferOldVersionRootTxs
	 * @param txContext
	 * @param isUpdateReqRCVD
	 * @return
	 */
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			Set<String> bufferOldVersionRootTxs, TransactionContext txContext,
			boolean isUpdateReqRCVD);
	
	/**
	 * when dynamic update is done, the concrete algorithm should get notified to execute cleanup
	 * @param hostComp
	 * @return
	 */
	public boolean updateIsDone(String hostComp);

	/**
	 * when necessary, algorithm may need to be notified to execute related initiation.
	 * @param identifier
	 */
	public void start(String identifier);

	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs);

	
	public String getAlgorithmRoot(String parentTx, String rootTx);
	
}