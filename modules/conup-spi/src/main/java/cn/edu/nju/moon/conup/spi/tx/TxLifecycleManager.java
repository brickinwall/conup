package cn.edu.nju.moon.conup.spi.tx;

import java.util.Map;

import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;

public interface TxLifecycleManager {

	/**
	 * create transaction id
	 * @return
	 */
	public String createID();

	/**
	 * create a temporary transaction id
	 * @return
	 */
	public String createFakeTxId();

	/**
	 * @param id the transaction id that needs to be destroyed
	 */
	public void destroyID(String txId);

	/**
	 * @return total transactions that are running
	 */
//	public int getTxs();
	public Map<String, TransactionContext> getTxs();
	
	/**
	 * the host component started a sub-transaction on a remote component
	 * @param subComp
	 * @param curComp
	 * @param rootTx
	 * @param parentTx
	 * @param subTx
	 * @return
	 */
//	public boolean startRemoteSubTx(String subComp, String curComp, String rootTx,
//			String parentTx, String subTx);
	
	/**
	 * a sub-transaction just ended and returned from a remote component 
	 * @param subComp
	 * @param curComp
	 * @param rootTx
	 * @param parentTx
	 * @param subTx
	 * @return
	 */
//	public boolean endRemoteSubTx(String subComp, String curComp, String rootTx,
//			String parentTx, String subTx);
	
	/**
	 * the host component is going to init a sub-transaction for another component.
	 * However, the sub-transaction has not truely been started.
	 * 
	 * @param hostComp
	 * @param fakeSubTx the fake tx id
	 * @param rootTx
	 * @param rootComp
	 * @param parentTx
	 * @param parentComp
	 * @return
	 */
//	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp);
	
	/**
	 * the host component is going to init a sub-transaction for another component.
	 * However, the sub-transaction has not truely been started.
	 * 
	 * @param hostComp
	 * @param fakeSubTx
	 * @param txCtxInCache
	 * @return
	 */
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, TransactionContext txCtxInCache);
	
	/**
	 * 
	 * @param hostComp
	 * @param fakeSubTx
	 * @return
	 */
	public String endLocalSubTx(String hostComp, String fakeSubTx);
	
	/**
	 * when a root tx ends, TxLifecycleManager should be notified.
	 * @param hostComp
	 * @param rootTxId
	 * @return
	 */
	public void rootTxEnd(String hostComp, String rootTxId);
	
	/**
	 * because every component type 1 to 1 correspondence to a TxLifecycleManager
	 * @return component identifier
	 */
	public String getCompIdentifier();

	/**
	 * get current component's txRegistry
	 * @return
	 */
	public TransactionRegistry getTxRegistry();

	/**
	 * Get Tx information from InvocationContext.
	 * for example: root tx id, root tx host component;
	 * parent tx id, parent tx host component etc.
	 * @param invocationContext
	 * @param hostComponent
	 */
	public void resolveInvocationContext(InvocationContext invocationContext,
			String hostComponent);

	/**
	 * create a invocation context which is used to attached to Message to transfer between components.
	 * InvocationContext contains 
	 * rootTxId:rootComponent, parentTxId:parentComponent, subTxId:subComponent, invocationSequence.
	 * invocationSequence contains all these invoked tx's id and host component.
	 * @param hostComponent
	 * @param serviceName
	 * @param txDepMonitor
	 * @return
	 */
	public InvocationContext createInvocationCtx(String hostComponent, String serviceName,
			TxDepMonitor txDepMonitor);

	/**
	 * a sub-transaction just ended and returned from a remote component
	 * @param invocationCtx
	 * @param proxyRootTxId 
	 * @return
	 */
	public boolean endRemoteSubTx(InvocationContext invocationCtx, String proxyRootTxId);

	public void updateTxContext(String currentTxID, TransactionContext txContext);

	public TransactionContext getTransactionContext(String curTxID);

	public void removeTransactionContext(String curTxID);

}