package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface DynamicDepManager{
	
	/**
	 * when dependency changed, we need to check whether ready for update
	 * here we check when pastDepCreate, pastDepRemove two events
	 * @param hostComp
	 */
	public void dependenceChanged(String hostComp);
	
	/**
	 * clean up information used by Algorithms during the update process
	 */
	public void dynamicUpdateIsDone();
	
	/**
	 * 
	 * @return root transactions that should be responded with old version component
	 */
	public Set<String> getAlgorithmOldVersionRootTxs();
	
	/**
	 * @return corresponding component object of the mgr
	 */
	public ComponentObject getCompObject();
	
	/**
	 * @return Dependences that current component depends on
	 */
	public Set<Dependence> getRuntimeDeps();
	
	/**
	 * @return Dependence that depends on current component
	 */
	public Set<Dependence> getRuntimeInDeps();
	
	/**
	 * @return the Scope
	 */
	public Scope getScope();
	
	/**
	 * @return identifiers of the components that current component statically depends on
	 */
	public Set<String> getStaticDeps();
	
	/**
	 * 
	 * @return current component's parent components
	 */
	public Set<String> getStaticInDeps();
	
	/**
	 * @return TxLifecycleManager
	 */
	public TxLifecycleManager getTxLifecycleMgr();
	
	/**
	 * @return transactions that are running on the component
	 */
	public Map<String, TransactionContext> getTxs();
	
	/**
	 * 
	 * @param txContext
	 * @return
	 */
	public boolean initLocalSubTx(TransactionContext txContext);
	
	/**
	 * 
	 * @param algorithmOldVersionRootTxs old root tx calculated by the concrete algorithm
	 * @param bufferOldVersionRootTxs old root tx calculated by the buffer interceptor
	 * @param txContext current tx's TransactionContext
	 * @param isUpdateReqRCVD is the component received dynamic update request
	 * @return
	 */
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD);

	/**
	 * is component object ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate();

	/**
	 * received dependences notification from peer component
	 * @param targetComp target component's name
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param payload
	 * @return
	 */
	public boolean manageDependence(String payload);
//	public boolean manageDependence(String proctocol, String payload);
	
	/**
	 * maintain tx
	 * @param txContext
	 * @return
	 */
	public boolean manageTx(TransactionContext txContext);
	
	/**
	 * @param subTxStatus sub tx status, i.e., TransactionStart and TransactionEnd
	 * @param subComp
	 * @param curComp
	 * @param rootTx
	 * @param parentTx
	 * @param subTx
	 * @return
	 */
//	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx, String parentTx, String subTx);
	
	/**
	 * when ondmenad is done, we need to notify algorithm to start to work
	 */
	public void ondemandSetupIsDone();
	
	/**
	 * 
	 * @param algorithmType
	 */
	public void setAlgorithm(Algorithm algorithm);

	public void setCompLifeCycleMgr(CompLifeCycleManager compLifecycleManager);

	/**
	 * @param compObj ComponentObject
	 */
	public void setCompObject(ComponentObject compObj);

	public void setScope(Scope scope);
	
	/**
	 * 
	 * @param txLifecycleMgr
	 */
	public void setTxLifecycleMgr(TxLifecycleManager txLifecycleMgr);

	public boolean notifySubTxStatus(TxEventType transactionstart,
			InvocationContext invocationCtx, CompLifeCycleManager compLifeCycleMgr);

}
