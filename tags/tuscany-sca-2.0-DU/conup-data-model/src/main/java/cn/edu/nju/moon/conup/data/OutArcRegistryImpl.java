package cn.edu.nju.moon.conup.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.def.Arc;

public class OutArcRegistryImpl implements ArcRegistry{
	private static ArcRegistry outArcRegistryImpl = new OutArcRegistryImpl();
//	private Set<Arc> outArcs = new HashSet<Arc>();
	private Set<Arc> outArcs = new ConcurrentSkipListSet<Arc>();
	
	private OutArcRegistryImpl(){
	}
	
	public static ArcRegistry getInstance(){
		return outArcRegistryImpl;
	}

	@Override
	public void addArc(Arc arc) {
		outArcs.add(arc);
	}

	@Override
	public void removeArc(Arc arc) {
		Iterator it = outArcs.iterator();
		while(it.hasNext()){
			if(it.next().equals(arc))
				it.remove();
		}
	}

	@Override
	public Set<Arc> getArcs() {
		return outArcs;
	}

	@Override
	public Set<Arc> getArcsViaType(String type) {
		Set<Arc> arcsOfType = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getType().equals(type)){
				arcsOfType.add(arc);
			}
		}
		return arcsOfType;
	}

	@Override
	public Set<Arc> getArcsViaRootTransaction(String rootTransaction) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getRootTransaction().equals(rootTransaction)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaSourceComponent(String sourceComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getSourceComponent().equals(sourceComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaTargetComponent(String targetComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getTargetComponent().equals(targetComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaSourceService(String sourceService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getSourceService().equals(sourceService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaTargetService(String targetService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : outArcs){
			if(arc.getTargetService().equals(targetService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public boolean update(Arc arc) {
		if(arc.getType().equals("future")){
			for(Arc arcInArcRegistry : outArcs){
				if(arcInArcRegistry.equals(arc)){
					Arc updatedArc = new Arc();
					updatedArc.setRootTransaction(arcInArcRegistry.getRootTransaction());
					updatedArc.setSourceComponent(arcInArcRegistry.getSourceComponent());
					updatedArc.setTargetComponent(arcInArcRegistry.getTargetComponent());
					updatedArc.setType("past");
					outArcs.remove(arcInArcRegistry);
					outArcs.add(updatedArc);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean contain(Arc arc) {
		for(Arc outArc : outArcs){
			if(outArc.equals(arc))
				return true;
		}
		return false;
//		if(outArcs.contains(arc))
//			return true;
//		else
//			return false;
	}

}