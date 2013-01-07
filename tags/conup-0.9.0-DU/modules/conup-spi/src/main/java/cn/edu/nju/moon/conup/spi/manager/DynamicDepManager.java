package cn.edu.nju.moon.conup.spi.manager;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

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
	public boolean manageDependence(String proctocol, String payload);
	
	/**
	 * try to achieve freeness via invoking FreenessStrategy
	 * @param rootTxID
	 * @param rootComp root component identifier
	 * @param parentComp parent component identifier
	 * @param curTxID
	 * @param hostComp host component identifier
	 * @param fcb 
	 * @return
	 */
//	public boolean achieveFreeness(String rootTxID, String rootComp, String parentComp, 
//			String curTxID, String hostComp, FreenessCallback fcb);
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * iff when component status is ondemand or updating return true
	 * @return
	 */
	public boolean isInterceptRequired();
	
	/**
	 * @return if the component status is NORMAL, return true
	 */
	public boolean isNormal();
	
	/**
	 * return whether the component is valid, which means that whether the component's 
	 * current status is exactly valid.
	 * Notice that if current status is updating, it would return false.
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
	
	/**
	 * 
	 * @param compStatus
	 */
	public void setCompStatus(CompStatus compStatus);

	/**
	 * @return if a component is in the process of the on-demand, return true. Otherwise, return false.
	 */
	public boolean isOndemandSetting();
	
	/**
	 * @return if a component needs on-demand setup or is in the process 
	 * of the on-demand setup, return true.
	 */
	public boolean isOndemandSetupRequired();
	
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
	
	/**
	 * @param freenessStrategy
	 */
//	public void setFreenessStrategy(FreenessStrategy freenessStrategy);
	
	public void setScope(Scope scope);
	
	/**
	 * when on-demand setup is done, DynamicDepManager should get notified.
	 */
	public void ondemandSetupIsDone();
	
	/**
	 * when dynamic update is done, DynamicDepManager should get notified.
	 */
	public void dynamicUpdateIsDone();
	
	/**
	 * in some concrete algorithm, e.g., Quiescence, some threads of a component may need to suspended because of
	 * the remote update is excuting on other component, which means that the component should be notified by the 
	 * remote component.
	 */
	public void remoteDynamicUpdateIsDone();
	
	/**
	 * when a component is free, DynamicDepManager should get notified.
	 */
	public void achievedFree();
	
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
	 * when a component is ready for update, DynamicDepManager should be notified.
	 */
	public void updating();
	
	/**
	 * 
	 * @param algorithmOldVersionRootTxs old root tx calculated by the concrete algorithm
	 * @param bufferOldVersionRootTxs old root tx calculated by the buffer interceptor
	 * @param txContext current tx's TransactionContext
	 * @param isUpdateReqRCVD is the component received dynamic update request
	 * @return
	 */
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs, Set<String> bufferOldVersionRootTxs, 
			TransactionContext txContext, boolean isUpdateReqRCVD);

	/**
	 * @return component's current status
	 */
	public CompStatus getCompStatus();
	
	/**
	 * @return a synchronization monitor for suspending threads while executing ondemand setup
	 */
	public Object getOndemandSyncMonitor();
	
	/**
	 * @return a synchronization monitor for suspending threads while the component trying to be free for dynamic update
	 */
	public Object getValidToFreeSyncMonitor();

	/**
	 * @return a synchronization monitor for suspending threads while executing dynamic update
	 */
	public Object getUpdatingSyncMonitor();
	
	/**
	 * 
	 * @return  a synchronization monitor for suspending threads while waiting for the finishing of other remote component 
	 */
	public Object getWaitingRemoteCompUpdateDoneMonitor();
	
	/**
	 * if a component received dynamic update request
	 * @return
	 */
	public boolean updateIsReceived();
	
	/**
	 * is a target component for update? 
	 * @return
	 */
	public boolean isUpdateRequiredComp();
//	/**
//	 * add a parent component
//	 */
//	public void addParent(String parent);

	/**
	 * With the given parentTx and rootTx, each algorithm should return the 'root transaction' 
	 * in the meaning of concrete algorithm.
	 * @param parentTx
	 * @param rootTx
	 * @return
	 */
	public String getAlgorithmRoot(String parentTx, String rootTx);
	
}
