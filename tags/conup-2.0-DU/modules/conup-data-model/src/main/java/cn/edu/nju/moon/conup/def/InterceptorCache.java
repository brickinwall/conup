package cn.edu.nju.moon.conup.def;

import java.util.Set;
import java.util.Map.Entry;

public interface InterceptorCache {
	public void setCache(String threadID, TransactionDependency dependency);
	public TransactionDependency getDependency(String threadID);
	public Set<Entry<String, TransactionDependency>> getDependencies();
	public void removeDependecy(String threadID);
	public void removeAll();

}
