package cn.edu.nju.moon.conup.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.spi.datamodel.Arc;


/** arcs that entering a component. */
public class ArcRegistryImpl {
	private static ArcRegistryImpl arcRegistryImpl = new ArcRegistryImpl();
//	private Set<Arc> inArcs = new HashSet<Arc>();
	private Set<Arc> arcs = new ConcurrentSkipListSet<Arc>();
	
	private ArcRegistryImpl(){
	}
	
	public static ArcRegistryImpl getInstance(){
		return arcRegistryImpl;
	}
	
	public void addArc(Arc arc) {
		arcs.add(arc);
	}

	public void removeArc(Arc arc) {
		Iterator<Arc> it = arcs.iterator();
		while(it.hasNext()){
			if(it.next().equals(arc))
				it.remove();
		}
	}

	public Set<Arc> getArcs() {
		return arcs;
	}

	public Set<Arc> getArcsViaType(String type) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getType().equals(type)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public Set<Arc> getArcsViaRootTransaction(String rootTransaction) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getRootTransaction().equals(rootTransaction)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public Set<Arc> getArcsViaSourceComponent(String sourceComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getSourceComponent().equals(sourceComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public Set<Arc> getArcsViaTargetComponent(String targetComponent) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getTargetComponent().equals(targetComponent)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public Set<Arc> getArcsViaSourceService(String sourceService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getSourceService().equals(sourceService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public Set<Arc> getArcsViaTargetService(String targetService) {
		Set<Arc> returnArcs = new HashSet<Arc>();
		for(Arc arc : arcs){
			if(arc.getTargetService().equals(targetService)){
				returnArcs.add(arc);
			}
		}
		return returnArcs;
	}

	public boolean update(Arc arc) {
		if(arc.getType().equals("future")){
			for(Arc arcInArcRegistry : arcs){
				if(arcInArcRegistry.equals(arc)){
					Arc updatedArc = new Arc();
					updatedArc.setRootTransaction(arcInArcRegistry.getRootTransaction());
					updatedArc.setSourceComponent(arcInArcRegistry.getSourceComponent());
					updatedArc.setTargetComponent(arcInArcRegistry.getTargetComponent());
					updatedArc.setType("past");
					arcs.remove(arcInArcRegistry);
					arcs.add(updatedArc);
					return true;
				}
			}
		}
		return false;
	}

	public boolean contain(Arc arc) {
		for(Arc inArc : arcs){
			if(inArc.equals(arc))
				return true;
		}
		return false;
	}

}
