package cn.edu.nju.moon.conup.def;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InterceptorCacheImpl implements InterceptorCache{
	private static InterceptorCache interceptorCache = new InterceptorCacheImpl();
	private Map<String, TransactionDependency> cache;
	
	private InterceptorCacheImpl(){
		cache = new HashMap<String, TransactionDependency>();
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
		cache.remove(threadID);
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
