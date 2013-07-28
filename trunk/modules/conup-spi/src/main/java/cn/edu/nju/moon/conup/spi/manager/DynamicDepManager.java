package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface DynamicDepManager extends Observer{
	
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
	public boolean manageDependence(String proctocol, String payload);
	
	/**
	 * is component object ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate();
	
	/**
	 * @return the Scope
	 */
	public Scope getScope();
	
	/**
	 * @return corresponding component object of the mgr
	 */
	public ComponentObject getCompObject();
	
	/**
	 * @param compObj ComponentObject
	 */
	public void setCompObject(ComponentObject compObj);
	
	/**
	 * 
	 * @param algorithmType
	 */
	public void setAlgorithm(Algorithm algorithm);
	
	public Algorithm getAlgorithm();
	
	/**
	 * 
	 * @return root transactions that should be responded with old version component
	 */
	public Set<String> getAlgorithmOldVersionRootTxs();
	
	/**
	 * 
	 * @param oldRootTxs it takes parentTx and rootTx as the the key and value respectively
	 * @return
	 */
	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs);
	
	public void setScope(Scope scope);
	
	/**
	 * @return identifiers of the components that current component statically depends on
	 */
	public Set<String> getStaticDeps();
	
	/**
	 * @return Dependences that current component depends on
	 */
	public Set<Dependence> getRuntimeDeps();
	
	/**
	 * @return Dependence that depends on current component
	 */
	public Set<Dependence> getRuntimeInDeps();
	
	/**
	 * @return transactions that are running on the component
	 */
	public Map<String, TransactionContext> getTxs();
	
	/**
	 * 
	 * @return current component's parent components
	 */
	public Set<String> getStaticInDeps();

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
	 * With the given parentTx and rootTx, each algorithm should return the 'root transaction' 
	 * in the meaning of concrete algorithm.
	 * @param parentTx
	 * @param rootTx
	 * @return
	 */
	public String getAlgorithmRoot(String parentTx, String rootTx);
	
	/**
	 * @param subTxStatus sub tx status, i.e., TransactionStart and TransactionEnd
	 * @param subComp
	 * @param curComp
	 * @param rootTx
	 * @param parentTx
	 * @param subTx
	 * @return
	 */
	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx, String parentTx, String subTx);
	
	/**
	 * @return TxLifecycleManager
	 */
	public TxLifecycleManager getTxLifecycleMgr();
	
	/**
	 * 
	 * @param txContext
	 * @return
	 */
	public boolean initLocalSubTx(TransactionContext txContext);
	
	/**
	 * when dependency changed, we need to check whether ready for update
	 * here we check when pastDepCreate, pastDepRemove two events
	 * @param hostComp
	 */
	public void dependenceChanged(String hostComp);
	
	/**
	 * 
	 * @param txLifecycleMgr
	 */
	public void setTxLifecycleMgr(TxLifecycleManager txLifecycleMgr);

	/**
	 * clean up information used by Algorithms during the update process
	 */
	public void dynamicUpdateIsDone();

	/**
	 * when ondmenad is done, we need to notify algorithm to start to work
	 */
	public void ondemandSetupIsDone();

	public void setCompLifeCycleMgr(CompLifeCycleManager compLifecycleManager);
	
	public CompLifeCycleManager getCompLifeCycleMgr();

}
