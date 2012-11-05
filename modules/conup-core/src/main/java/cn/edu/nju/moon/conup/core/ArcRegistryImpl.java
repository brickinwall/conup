package cn.edu.nju.moon.conup.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;


/** arcs that entering a component. */
public class ArcRegistryImpl {
//	private Set<Arc> inArcs = new HashSet<Arc>();
	private Set<Dependence> dependences = new ConcurrentSkipListSet<Dependence>();
	
	public ArcRegistryImpl(){
	}
	
	
	public void addArc(Dependence dependence) {
		dependences.add(dependence);
	}

	public void removeArc(Dependence dependence) {
		Iterator<Dependence> it = dependences.iterator();
		while(it.hasNext()){
			if(it.next().equals(dependence))
				it.remove();
		}
	}

	public Set<Dependence> getArcs() {
		return dependences;
	}

	public Set<Dependence> getArcsViaType(String type) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getType().equals(type)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public Set<Dependence> getArcsViaRootTransaction(String rootTransaction) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getRootTransaction().equals(rootTransaction)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public Set<Dependence> getArcsViaSourceComponent(String sourceComponent) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getSourceComponent().equals(sourceComponent)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public Set<Dependence> getArcsViaTargetComponent(String targetComponent) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getTargetComponent().equals(targetComponent)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public Set<Dependence> getArcsViaSourceService(String sourceService) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getSourceService().equals(sourceService)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public Set<Dependence> getArcsViaTargetService(String targetService) {
		Set<Dependence> returnArcs = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getTargetService().equals(targetService)){
				returnArcs.add(dependence);
			}
		}
		return returnArcs;
	}

	public boolean update(Dependence dependence) {
		if(dependence.getType().equals("future")){
			for(Dependence arcInArcRegistry : dependences){
				if(arcInArcRegistry.equals(dependence)){
					Dependence updatedArc = new Dependence();
					updatedArc.setRootTransaction(arcInArcRegistry.getRootTransaction());
					updatedArc.setSourceComponent(arcInArcRegistry.getSourceComponent());
					updatedArc.setTargetComponent(arcInArcRegistry.getTargetComponent());
					updatedArc.setType("past");
					dependences.remove(arcInArcRegistry);
					dependences.add(updatedArc);
					return true;
				}
			}
		}
		return false;
	}

	public boolean contain(Dependence dependence) {
		for(Dependence inArc : dependences){
			if(inArc.equals(dependence))
				return true;
		}
		return false;
	}

}
