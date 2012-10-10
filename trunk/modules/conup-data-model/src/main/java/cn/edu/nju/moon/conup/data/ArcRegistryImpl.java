package cn.edu.nju.moon.conup.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.moon.conup.def.Arc;

@Deprecated
public class ArcRegistryImpl implements ArcRegistry {
	private Set<Arc> arcs;
	
	public ArcRegistryImpl(){
		arcs = new HashSet<Arc>();
	}

	@Override
	public Set<Arc> getArcs() {
		return arcs;
	}

	@Override
	public Set<Arc> getArcsViaType(String type) {
		return null;
	}

	@Override
	public Set<Arc> getArcsViaRootTransaction(String rootTransaction) {
		return null;
	}

	@Override
	public Set<Arc> getArcsViaSourceComponent(String sourceComponent) {
		return null;
	}

	@Override
	public Set<Arc> getArcsViaTargetComponent(String targetComponent) {
		return null;
	}

	@Override
	public Set<Arc> getArcsViaSourceService(String sourceService) {
		return null;
	}

	@Override
	public Set<Arc> getArcsViaTargetService(String targetService) {
		return null;
	}

	@Override
	public void addArc(Arc arc) {
		arcs.add(arc);
	}

	@Override
	public void removeArc(Arc arc) {
		arcs.remove(arc);
	}

	@Override
	public boolean update(Arc arc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contain(Arc arc) {
		// TODO Auto-generated method stub
		return false;
	}

}
