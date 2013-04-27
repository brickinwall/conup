package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

/**
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
 */
public class TxLifecycleManager {
	
	private static final Logger LOGGER = Logger.getLogger(TxLifecycleManager.class.getName());
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
	 * key: parent tx id, value : root tx id
	 * For maintaining root tx that are running on the component
	 */
//	private static Set<String> OLD_ROOT_TXS = new ConcurrentSkipListSet<>();
	/**
	 * key compIdentifier
	 * key: parent tx, value: root tx
	 */
//	private static Map<String, Map<String, String>>	OLD_ROOT_TXS = new ConcurrentHashMap<String, Map<String, String>>();
	
	/**
	 * create transaction id
	 * @return
	 */
	public String createID(){
		String txID = null;
		
		// use UUID to generate txID
		UUID uuid = UUID.randomUUID();
		txID = uuid.toString();
		
		String threadID = getThreadID();
		String componentIdentifier = associateTx.get(threadID);
		assert(componentIdentifier != null);
		associateTx.remove(threadID);
		
		
		// get info from interceptor cache
		// according threadID 
		InterceptorCache interceptorCache = InterceptorCache.getInstance(componentIdentifier);
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		String parentTx = txContextInCache.getParentTx();
		String currentTx = txContextInCache.getCurrentTx();
		String hostComponent = txContextInCache.getHostComponent();
		String rootComponent = txContextInCache.getRootComponent();
		String parentComponent = txContextInCache.getParentComponent();
		
		// associate currentTxId with parent/root
		if(rootTx==null && parentTx==null 
				&& currentTx==null && hostComponent!=null){
			//current transaction is root
			assert(rootTx==null && parentTx==null && currentTx==null && hostComponent!=null);
			currentTx = txID;
			rootTx = currentTx;
			parentTx = currentTx;
			//update interceptor cache transactionContext
			txContextInCache.setCurrentTx(currentTx);
			txContextInCache.setParentTx(parentTx);
			txContextInCache.setRootTx(rootTx);
			rootComponent = hostComponent;
			parentComponent = hostComponent;
			txContextInCache.setHostComponent(hostComponent);
			txContextInCache.setRootComponent(rootComponent);
			txContextInCache.setParentComponent(parentComponent);
			LOGGER.fine("created root tx id:" + rootTx);
//		} else if(rootTx!=null && parentTx!=null 
//				&& currentTx==null && hostComponent!=null){
		} else if(rootTx!=null && parentTx!=null 
				&& hostComponent!=null){
			assert(rootTx!=null && parentTx!=null && hostComponent!=null);
			//current transaction is a sub-transaction
			//update interceptor cache dependency
			currentTx = txID;
			txContextInCache.setCurrentTx(currentTx);
		} else{
			LOGGER.warning("Error: dirty data in InterceptroCache, " +
					"because [rootTx, parentTx, currentTx, hostComp] = " + rootTx + ", " +
					parentTx + ", " + currentTx + ", " + hostComponent);
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
	 * create a temporary transaction id
	 * @return
	 */
	public String createFakeTxId(){
		UUID uuid = UUID.randomUUID();
		String txID = uuid.toString();
		return "FAKE_TX_ID" + txID;
	}
	
	/**
	 * @param id the transaction id that needs to be destroyed
	 */
	public void destroyID(String id){
		TX_IDS.remove(id);
	}
	
	/**
	 * @return total transactions that are running
	 */
	public int getTxs(){
		return TX_IDS.size();
	}

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
	/**
	 * invoked by JavaImplementationInvoker
	 * add <threadID, componentIdentifier> To AssociateTx
	 * @param threadID
	 * @param txContext
	 */
	public void addToAssociateTx(String threadID, String identifier){
		associateTx.put(threadID, identifier);
	}
	
}
