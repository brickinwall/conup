/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

/**
 * 
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TxLifecycleManager {
	
	private Logger LOGGER = Logger.getLogger(TxLifecycleManager.class.getName());
	/**
	 * TX_IDS takes transactionID and TransactionContext as key and value respectively.
	 */
	public static Map<String, TransactionContext> TX_IDS = new ConcurrentHashMap<String, TransactionContext>();
	
	/**
	 * maintained by trace interceptor and TxLifecycleManager
	 * key : threadID
	 * value is component identifier
	 * it is just used to as a temporary to hold host component name
	 */
	private Map<String, String> associateTx = new ConcurrentHashMap<String, String>();
	
	/**
	 * create transaction id
	 * @return
	 */
	public String createID(){
		String txID = null;
		
		/**
		 * use UUID to generate txID
		 */
		UUID uuid = UUID.randomUUID();
		txID = uuid.toString();
		
		String threadID = getThreadID();
		String componentIdentifier = associateTx.get(threadID);
		assert(componentIdentifier != null);
		associateTx.remove(threadID);
		
		/**
		 * get info from interceptor cache
		 * according threadID 
		 */
		InterceptorCache interceptorCache = InterceptorCache.getInstance(componentIdentifier);
		TransactionContext txContextInCache = interceptorCache.getTxContext(threadID);
		String rootTx = txContextInCache.getRootTx();
		String parentTx = txContextInCache.getParentTx();
		String currentTx = txContextInCache.getCurrentTx();
		String hostComponent = txContextInCache.getHostComponent();
		String rootComponent = txContextInCache.getRootComponent();
		String parentComponent = txContextInCache.getParentComponent();
		
		/**
		 * associate currentTxId with parent/root
		 */
		if(rootTx==null && parentTx==null 
				&& currentTx==null && hostComponent!=null){
			//current transaction is root
			assert(rootTx==null && parentTx==null && currentTx==null && hostComponent!=null);
			currentTx = txID;
			rootTx = currentTx;
			parentTx = currentTx;
			//update interceptor cache dependency
			txContextInCache.setCurrentTx(currentTx);
			txContextInCache.setParentTx(parentTx);
			txContextInCache.setRootTx(rootTx);
			rootComponent = hostComponent;
			parentComponent = hostComponent;
			txContextInCache.setHostComponent(hostComponent);
			txContextInCache.setRootComponent(rootComponent);
			txContextInCache.setParentComponent(parentComponent);
		} else if(rootTx!=null && parentTx!=null 
				&& currentTx==null && hostComponent!=null){
			assert(rootTx!=null && parentTx!=null && currentTx==null && hostComponent!=null);
			//current transaction is a sub-transaction
			//update interceptor cache dependency
			currentTx = txID;
			txContextInCache.setCurrentTx(currentTx);
		} else{
			LOGGER.warning("Error: dirty data in InterceptroCache.");
		}
		
		/**
		 * add new txContext to TX_IDS
		 */
		TransactionContext txContext = new TransactionContext();
		txContext.setCurrentTx(currentTx);
		txContext.setHostComponent(hostComponent);
		txContext.setParentComponent(parentComponent);
		txContext.setRootComponent(rootComponent);
		txContext.setParentTx(parentTx);
		txContext.setRootTx(rootTx);
		TX_IDS.put(txID, txContext);
		return txID;
	}
	
	/**
	 * @param id the transaction id that needs to be destroyed
	 */
	public void destroyID(String id){
		
	}
	
	/**
	 * 
	 * @return total transactions that are running
	 */
	public int getTxs(){

		return 0;
	}

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
	/**
	 * invoked by trace interceptor
	 * @param threadID
	 * @param txContext
	 */
	public void addTx(String threadID, String identifier){
		associateTx.put(threadID, identifier);
	}
}
