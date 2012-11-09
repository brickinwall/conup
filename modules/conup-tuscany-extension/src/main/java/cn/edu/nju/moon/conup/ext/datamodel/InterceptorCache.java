package cn.edu.nju.moon.conup.ext.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

public class InterceptorCache {
	private static InterceptorCache interceptorCache = new InterceptorCache();
	private Map<String, TransactionContext> cache;
	
	private InterceptorCache(){
		cache = new HashMap<String, TransactionContext>();
	}

	public static InterceptorCache getInstance(){
		return interceptorCache;
	}

	public void setCache(String threadID, TransactionContext txContext) {
		cache.put(threadID, txContext);
	}

	public TransactionContext getTxContext(String threadID) {
		return cache.get(threadID);
	}

	public void removeTxContext(String threadID) {
		cache.remove(threadID);
	}

	public void removeAll() {
		cache.clear();
	}

	public Set<Entry<String, TransactionContext>> getTxContexts() {
		return cache.entrySet();
	}
	
}
