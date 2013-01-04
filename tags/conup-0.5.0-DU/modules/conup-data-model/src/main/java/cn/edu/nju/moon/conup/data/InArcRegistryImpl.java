package cn.edu.nju.moon.conup.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.def.Arc;

/** arcs that entering a component. */
public class InArcRegistryImpl implements ArcRegistry {
	private static ArcRegistry inArcRegistryImpl = new InArcRegistryImpl();
//	private Set<Arc> inArcs = new HashSet<Arc>();
	private Set<Arc> inArcs = new ConcurrentSkipListSet<Arc>();
	
	private InArcRegistryImpl(){
	}
	
	public static ArcRegistry getInstance(){
		return inArcRegistryImpl;
	}
	
	@Override
	public void addArc(Arc arc) {
		inArcs.add(arc);
	}

	@Override
	public void removeArc(Arc arc) {
		Iterator it = inArcs.iterator();
		while(it.hasNext()){
			if(it.next().equals(arc))
				it.remove();
		}
	}

	@Override
	public Set<Arc> getArcs() {
		return inArcs;
	}

	@Override
	public Set<Arc> getArcsViaType(String type) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getType().equals(type)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaRootTransaction(String rootTransaction) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getRootTransaction().equals(rootTransaction)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaSourceComponent(String sourceComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getSourceComponent().equals(sourceComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaTargetComponent(String targetComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getTargetComponent().equals(targetComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaSourceService(String sourceService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getSourceService().equals(sourceService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public Set<Arc> getArcsViaTargetService(String targetService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : inArcs){
			if(arc.getTargetService().equals(targetService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	@Override
	public boolean update(Arc arc) {
		if(arc.getType().equals("future")){
			for(Arc arcInArcRegistry : inArcs){
				if(arcInArcRegistry.equals(arc)){
					Arc updatedArc = new Arc();
					updatedArc.setRootTransaction(arcInArcRegistry.getRootTransaction());
					updatedArc.setSourceComponent(arcInArcRegistry.getSourceComponent());
					updatedArc.setTargetComponent(arcInArcRegistry.getTargetComponent());
					updatedArc.setType("past");
					inArcs.remove(arcInArcRegistry);
					inArcs.add(updatedArc);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean contain(Arc arc) {
		for(Arc inArc : inArcs){
			if(inArc.equals(arc))
				return true;
		}
		return false;
//		if(inArcs.contains(arc)){
//			System.out.println("true");
//			return true;
//		}
//		else{
//			System.out.println("false");
//			return false;
//		}
	}

}
