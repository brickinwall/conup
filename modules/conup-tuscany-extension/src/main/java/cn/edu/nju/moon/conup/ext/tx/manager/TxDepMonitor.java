/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * It's used to monitor transaction status, maintain transaction context 
 * and possibly invoke related algorithm
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TxDepMonitor {
	/**
	 * 
	 * @param TxEventType 
	 * @param curTxID current tx id
	 * @return
	 */
	public boolean notify(TxEventType et, String curTxID){
		Map<String, TransactionContext> TX_IDS = TxLifecycleManager.TX_IDS;
		LocalDynamicDependencesManager ddm = LocalDynamicDependencesManager.getInstance(curTxID);
		TransactionContext txContext = TX_IDS.get(curTxID);
		txContext.setEventType(et);
		txContext.setFutureComponents(ddm.getFuture());
		txContext.setPastComponents(ddm.getRealPast());
		
//		TransactionRegistry txRegistry = TransactionRegistry.getInstance();
//		txRegistry.addTransactionContext(curTxID, txContext);
		/**
		 * use componentIdentifier to get specific DynamicDepManager
		 */
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(txContext.getHostComponent());
		return dynamicDepMgr.manageTx(txContext);
		
	}
	
	/**
	 * @param txStatus transaction status, i.e., start, running, end
	 * @param curTxID current tx id
	 * @param rootTxID root tx id
	 * @param rootComp root tx's host component name
	 * @param parentTxID parent tx id
	 * @param parentComp parent tx's host component name
	 * @param futureRef references that will be used in future
	 * @param pastRef references that have been used
	 * @return
	 */
	@Deprecated
	public boolean notify(String txStatus, String curTxID, String rootTxID, String rootComp, 
			String parentTxID, String parentComp, Set<String> futureRef, Set<String> pastRef){
		
		return true;
	}
	
//	/* return current thread ID. */
//	private String getThreadID() {
//		return new Integer(Thread.currentThread().hashCode()).toString();
//	}
}
