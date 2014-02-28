/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;


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
	public void manageDependence(TransactionContext txContext, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr);
	
	/**
	 * received dependences notification from peer component
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param 
	 * @param payload(:msgType XML, JSON, etc.)
	 * @return
	 */
//	public boolean manageDependence(String payload);

	/**
	 * 
	 * @param dep
	 * @param operationType
	 * @param depMgr
	 * @param compLifeCycleMgr 
	 * @param extraParams
	 * @return
	 */
	public boolean manageDependence(DepOperationType operationType, Map<String, String> params, 
			DynamicDepManager depMgr,
			CompLifeCycleManager compLifeCycleMgr);

	/**
	 * is a component ready?
	 * @return 
	 */
	public boolean readyForUpdate(String compIdentifier, DynamicDepManager depMgr);
	
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
			TransactionContext txContext, boolean isUpdateReqRCVD, DynamicDepManager depMgr);
	
	/**
	 * when dynamic update is done, the concrete algorithm should get notified to execute cleanup
	 * @param hostComp
	 * @return
	 */
	boolean updateIsDone(String hostComp, DynamicDepManager depMgr);
//	public boolean updateIsDone(String hostComp);

//	/**
//	 * when necessary, algorithm may need to be notified to execute related initiation.
//	 * @param identifier
//	 */
//	public void initiate(String identifier);

//	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs);

	
//	public String getAlgorithmRoot(String parentTx, String rootTx);
	
	/**
	 * when a sub transaction started or ended, parent component should get notified.
	 * @param subTxStatus the status of sub transaction, which can only be TxEventType.TransactionStart or TxEventType.TransactionEnd
	 * @param subComp host component of the sub transaction
	 * @param curComp current component
	 * @param rootTx 
	 * @param parentTx the parent transaction of the sub transaction, i.e., current transaction
	 * @param subTx 
	 * @return
	 */
//	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx, String parentTx, String subTx);

	/**
	 * the host component is going to init a sub-transaction by another component.
	 * However, the sub-transaction has not truely been started.
	 * during initLocalSubTx, if the component status become ondemand, in Tranquillity, Consistency algorithm
	 * we need to make sure consistency, so we add future, past edge to itself
	 * 
	 * @param txContext
	 * @param dynamicDepManagerImpl 
	 */
	public boolean initLocalSubTx(TransactionContext txContext,
			CompLifeCycleManager compLifeCycleMgr,
			DynamicDepManager depMgr);

	/**
	 * 
	 * @param subTxStatus
	 * @param invocationCtx
	 * @param compLifeCycleMgr
	 * @param depMgr
	 * @param proxyRootTxId 
	 * @return
	 */
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr, String proxyRootTxId);

	/**
	 * when necessary, algorithm may need to be notified to execute related initiation.
	 * @param hostComp
	 * @param depMgr
	 */
	public void initiate(String hostComp, DynamicDepManager depMgr);

	/**
	 * set a depMgr to algorithm, although this parameter is not useful for Quiescence
	 * Algorithm and depMgr is 1-1 with ComponentObject
	 * @param depMgr
	 */
//	public void setDynamicDepMgr(DynamicDepManager depMgr);
	
	/**
	 * set a TxDepRegistry to algorithm, although this parameter is not useful for Quiescence
	 * Algorithm and TxDepRegistry is 1-1 with ComponentObject
	 * @param txDepMonitor
	 */
//	public void setTxDepRegistry(TxDepRegistry txDepRegistry);
}
