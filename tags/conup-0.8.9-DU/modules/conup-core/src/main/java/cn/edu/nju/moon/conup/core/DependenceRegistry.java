package cn.edu.nju.moon.conup.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

/** dependences for a component. */
public class DependenceRegistry {
	private Set<Dependence> dependences = new ConcurrentSkipListSet<Dependence>();

	public DependenceRegistry() {
	}

	public void addDependence(Dependence dependence) {
		dependences.add(dependence);
		
		DepRecorder depRecorder = DepRecorder.getInstance();
		String key = dependence.getSrcCompObjIdentifier() + DepRecorder.SEPERATOR
				+ dependence.getTargetCompObjIdentifer() + DepRecorder.SEPERATOR + dependence.getRootTx();
		String action = dependence.getType() + DepRecorder.SEPERATOR + DepRecorder.CREATION;
		depRecorder.addAction(key, action);
	}

	public boolean removeDependence(Dependence dependence) {
		Iterator<Dependence> it = dependences.iterator();
		while (it.hasNext()) {
			if (it.next().equals(dependence)){
				
				// for test
				DepRecorder depRecorder = DepRecorder.getInstance();
				String key = dependence.getSrcCompObjIdentifier() + DepRecorder.SEPERATOR
						+ dependence.getTargetCompObjIdentifer() + DepRecorder.SEPERATOR + 
						dependence.getRootTx();
				String action = dependence.getType() + DepRecorder.SEPERATOR + DepRecorder.REMOVAL;
				depRecorder.addAction(key, action);
				
				
				it.remove();
				return true;
			}
		}
		return false;
	}

	public boolean removeDependence(String type, String rootTx,
			String srcCompObjIdentifier, String targetCompObjIdentifer) {
		Iterator<Dependence> it = dependences.iterator();
		while (it.hasNext()) {
			Dependence dep = (Dependence) it.next();
			if (dep.getType().equals(type)
					&& dep.getRootTx().equals(rootTx)
					&& dep.getSrcCompObjIdentifier().equals(srcCompObjIdentifier)
					&& dep.getTargetCompObjIdentifer().equals(targetCompObjIdentifer)) {
				
				// for test
				DepRecorder depRecorder = DepRecorder.getInstance();
				String key = dep.getSrcCompObjIdentifier() + DepRecorder.SEPERATOR
						+ dep.getTargetCompObjIdentifer() + DepRecorder.SEPERATOR + 
						dep.getRootTx();
				String action = dep.getType() + DepRecorder.SEPERATOR + DepRecorder.REMOVAL;
				depRecorder.addAction(key, action);
				
				it.remove();
				return true;
			}
		}
		return false;
	}

	public Set<Dependence> getDependences() {
		return dependences;
	}

	public Set<Dependence> getDependencesViaType(String type) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getType().equals(type)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaRootTransaction(
			String rootTransaction) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getRootTx().equals(rootTransaction)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaSourceComponent(
			String sourceComponent) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getSrcCompObjIdentifier().equals(sourceComponent)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaTargetComponent(
			String targetComponent) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getTargetCompObjIdentifer().equals(targetComponent)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaSourceService(String sourceService) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getSourceService().equals(sourceService)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}

	public Set<Dependence> getDependencesViaTargetService(String targetService) {
		Set<Dependence> returnDependences = new HashSet<Dependence>();
		for (Dependence dependence : dependences) {
			if (dependence.getTargetService().equals(targetService)) {
				returnDependences.add(dependence);
			}
		}
		return returnDependences;
	}
	
	/**
	 * this method should not conforms to algorithm
	 * @param dependence
	 * @return
	 */
//	@Deprecated
//	public boolean update(Dependence dependence) {
//		if (dependence.getType().equals("future")) {
//			for (Dependence depRegistry : dependences) {
//				if (depRegistry.equals(dependence)) {
//					Dependence updatedDependence = new Dependence();
//					updatedDependence.setRootTx(depRegistry.getRootTx());
//					updatedDependence.setSrcCompObjIdentifier(depRegistry
//							.getSrcCompObjIdentifier());
//					updatedDependence.setTargetCompObjIdentifer(depRegistry
//							.getTargetCompObjIdentifer());
//					updatedDependence.setType("past");
//					dependences.remove(depRegistry);
//					dependences.add(updatedDependence);
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	public boolean contain(Dependence dependence) {
		for (Dependence dep : dependences) {
			if (dep.equals(dependence))
				return true;
		}
		return false;
	}
	
	public int size(){
		int num = 0;
		Iterator<Dependence> iterator = dependences.iterator();
		while(iterator.hasNext()){
			iterator.next();
			num ++;
		}
		return num;
	}

}
