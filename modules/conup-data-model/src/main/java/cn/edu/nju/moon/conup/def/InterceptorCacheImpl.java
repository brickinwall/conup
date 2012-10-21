package cn.edu.nju.moon.conup.def;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InterceptorCacheImpl implements InterceptorCache{
	private static InterceptorCache interceptorCache = new InterceptorCacheImpl();
	private Map<String, TransactionDependency> cache;
	
	private InterceptorCacheImpl(){
		cache = new ConcurrentHashMap<String, TransactionDependency>();
	}

	public static InterceptorCache getInstance(){
		return interceptorCache;
	}

	@Override
	public void setCache(String threadID, TransactionDependency dependency) {
		cache.put(threadID, dependency);
	}

	@Override
	public TransactionDependency getDependency(String threadID) {
		return cache.get(threadID);
	}

	@Override
	public void removeDependecy(String threadID) {
		Iterator iterator = cache.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry entry = (Map.Entry) iterator.next(); 
		    String key = (String)entry.getKey();
		    if(key.equals(threadID))
		    	iterator.remove();
		}
//		cache.remove(threadID);
	}

	@Override
	public void removeAll() {
		cache.clear();
	}

	@Override
	public Set<Entry<String, TransactionDependency>> getDependencies() {
		return cache.entrySet();
	}
	
}
