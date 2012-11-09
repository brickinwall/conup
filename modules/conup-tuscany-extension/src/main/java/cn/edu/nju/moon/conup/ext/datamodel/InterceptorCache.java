package cn.edu.nju.moon.conup.ext.datamodel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * InterceptorCache is designed for sharing root/parent tx/components between interceptors.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class InterceptorCache {
	/**
	 *  Each component has and only has one InterceptorCache.
	 *  Map<String, TransactionContext> takes threadID as the key.
	 */
	private Map<String, TransactionContext> compCache;
	
	/**
	 * 	A tuscany node may host multiple components, 
	 * 	each component has and only has one InterceptorCache
	 */
	private static Map<ComponentObject, InterceptorCache> nodeCache = 
			new ConcurrentHashMap<ComponentObject, InterceptorCache>();
	
	private InterceptorCache(){
		compCache = new ConcurrentHashMap<String, TransactionContext>();
	}

	/**
	 * @param compObj 
	 * @return a corresponding InterceptorCache
	 */
	public static InterceptorCache getInstance(ComponentObject compObj){
		synchronized (nodeCache) {
			if( !nodeCache.containsKey(compObj) ){
				nodeCache.put(compObj, new InterceptorCache());
			}
		}
		return nodeCache.get(compObj);
	}
	
	/**
	 * 
	 * @param identifier component identifier
	 * @return a corresponding InterceptorCache
	 */
	public static InterceptorCache getInstance(String identifier){
		ComponentObject compObj;
		compObj = NodeManager.getInstance().getComponentObject(identifier);
		synchronized (nodeCache) {
			if( !nodeCache.containsKey(compObj) ){
				nodeCache.put(compObj, new InterceptorCache());
			}
		}
		return nodeCache.get(compObj);
	}

	public void setCache(String threadID, TransactionContext txContext) {
		compCache.put(threadID, txContext);
	}

	public TransactionContext getTxContext(String threadID) {
		return compCache.get(threadID);
	}

	public void removeTxContext(String threadID) {
		compCache.remove(threadID);
	}

	public void removeAll() {
		compCache.clear();
	}

	public Set<Entry<String, TransactionContext>> getTxContexts() {
		return compCache.entrySet();
	}
	
}
