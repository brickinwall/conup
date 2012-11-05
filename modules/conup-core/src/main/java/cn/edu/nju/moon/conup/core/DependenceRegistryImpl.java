package cn.edu.nju.moon.conup.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;


/** dependences for a component. */
public class DependenceRegistryImpl {
	private static DependenceRegistryImpl depRegistryImpl = new DependenceRegistryImpl();
//	private Set<Arc> inArcs = new HashSet<Arc>();
	private Set<Dependence> dependences = new ConcurrentSkipListSet<Dependence>();
	
	private DependenceRegistryImpl(){
	}
	
	public static DependenceRegistryImpl getInstance(){
		return depRegistryImpl;
	}
	
	public void addDependence(Dependence dependence) {
		dependences.add(dependence);
	}

	public void removeDependence(Dependence dependence) {
		Iterator<Dependence> it = dependences.iterator();
		while(it.hasNext()){
			if(it.next().equals(dependence))
				it.remove();
		}
	}

	public Set<Dependence> getDependences() {
		return dependences;
	}

	public Set<Dependence> getDependencesViaType(String type) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getType().equals(type)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaRootTransaction(String rootTransaction) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getRootTransaction().equals(rootTransaction)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaSourceComponent(String sourceComponent) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getSourceComponent().equals(sourceComponent)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaTargetComponent(String targetComponent) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getTargetComponent().equals(targetComponent)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaSourceService(String sourceService) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getSourceService().equals(sourceService)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaTargetService(String targetService) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for(Dependence dependence : dependences){
			if(dependence.getTargetService().equals(targetService)){
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public boolean update(Dependence dependence) {
		if(dependence.getType().equals("future")){
			for(Dependence depRegistry : dependences){
				if(depRegistry.equals(dependence)){
					Dependence updatedDependence = new Dependence();
					updatedDependence.setRootTransaction(depRegistry.getRootTransaction());
					updatedDependence.setSourceComponent(depRegistry.getSourceComponent());
					updatedDependence.setTargetComponent(depRegistry.getTargetComponent());
					updatedDependence.setType("past");
					dependences.remove(depRegistry);
					dependences.add(updatedDependence);
					return true;
				}
			}
		}
		return false;
	}

	public boolean contain(Dependence dependence) {
		for(Dependence dep : dependences){
			if(dep.equals(dependence))
				return true;
		}
		return false;
	}

}
