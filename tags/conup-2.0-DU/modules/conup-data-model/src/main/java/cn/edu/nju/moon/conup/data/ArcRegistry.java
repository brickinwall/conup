package cn.edu.nju.moon.conup.data;

import java.util.Set;

import cn.edu.nju.moon.conup.def.Arc;

public interface ArcRegistry {
	public void addArc(Arc arc);
	public void removeArc(Arc arc);
	public boolean update(Arc arc);
	public boolean contain(Arc arc);
	
	public Set<Arc> getArcs();
	public Set<Arc> getArcsViaType(String type);
	public Set<Arc> getArcsViaRootTransaction(String rootTransaction);
	public Set<Arc> getArcsViaSourceComponent(String sourceComponent);
	public Set<Arc> getArcsViaTargetComponent(String targetComponent);
	public Set<Arc> getArcsViaSourceService(String sourceService);
	public Set<Arc> getArcsViaTargetService(String targetService);

}
