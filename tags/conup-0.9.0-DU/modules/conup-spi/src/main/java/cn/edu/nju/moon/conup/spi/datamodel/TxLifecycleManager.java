package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * It's supposed to manage the transactions that are running on a tuscany node.
 * @author Jiang Wang <jiang.wang88@gmail.com>
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
	 * key: parent tx id, value : root tx id
	 * For maintaining root tx that are running on the component
	 */
//	private static Set<String> OLD_ROOT_TXS = new ConcurrentSkipListSet<>();
	/**
	 * key: parent tx
	 * value: root tx
	 */
	private static Map<String, String>	OLD_ROOT_TXS = new ConcurrentHashMap<String, String>();
	
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
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
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
			//update interceptor cache transactionContext
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
	
	/** 
	 * when a business request with root/parent txs accepted, TxLifecycleManager should 
	 * get notified. 
	 */
	public static void addRootTx(String parentTxId, String rootTxId){
		synchronized (OLD_ROOT_TXS) {
			if(rootTxId != null)
				OLD_ROOT_TXS.put(parentTxId, rootTxId);
//				OLD_ROOT_TXS.add(rootTxId);
			
		}
	}
	
	public static String getRootTx(String parentTxId){
		synchronized (OLD_ROOT_TXS) {
			return OLD_ROOT_TXS.get(parentTxId);
		}
	}
	
	/**
	 * when a root tx ended on the component, TxLifecycleManager should 
	 * get notified. 
	 * remove all the txs whose root is marked with given rootTx or parentTx
	 * @param rootTx
	 */
	public static void removeRootTx(String rootTx){
		synchronized (OLD_ROOT_TXS) {
			Iterator<Entry<String, String>> iterator;
			iterator = OLD_ROOT_TXS.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, String> entry;
				entry = iterator.next();
				if(entry.getValue().equals(rootTx) || entry.getKey().equals(rootTx))
					iterator.remove();
			}
		}
	}
	
	/**
	 * Exactly remove a entry from the OLD_ROOT_TXS
	 * @param parentTx
	 * @param rootTx
	 */
	public static void removeRootTx(String parentTx, String rootTx){
		synchronized (OLD_ROOT_TXS) {
			OLD_ROOT_TXS.remove(parentTx);
		}
	}
	
	/**
	 * @return a copy of all the root txs that are running on the component
	 */
	public static Set<String>  copyOfOldRootTxs(String hostComp){
		synchronized (OLD_ROOT_TXS) {
			DynamicDepManager depMgr;
			depMgr = NodeManager.getInstance().getDynamicDepManager(hostComp);
			System.out.println("\nOLD_ROOT_TXS,size:" + OLD_ROOT_TXS.size() + " before convertToAlgorithmRoots:" + OLD_ROOT_TXS);
			Set<String> result = depMgr.convertToAlgorithmRootTxs(OLD_ROOT_TXS);
			System.out.println("\ncopyOfOldRootTxs,size:" + result.size() + " after convertToAlgorithmRoots:" + result);
			return result;
		}
////		Set<String> copy = new HashSet<String>();
//		synchronized (OLD_ROOT_TXS) {
//			copy.addAll(OLD_ROOT_TXS);
//			copy.putAll(OLD_ROOT_TXS);
//		}
//		return copy;
	}
	
}
