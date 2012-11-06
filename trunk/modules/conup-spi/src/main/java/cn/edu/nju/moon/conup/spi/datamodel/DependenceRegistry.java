package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;


/** dependences for a component. */
public interface DependenceRegistry {
	public void addDependence(Dependence dependence);

	public void removeDependence(Dependence dependence);

	public Set<Dependence> getDependences();

	public Set<Dependence> getDependencesViaType(String type);

	public Set<Dependence> getDependencesViaRootTransaction(String rootTransaction);

	public Set<Dependence> getDependencesViaSourceComponent(String sourceComponent) ;

	public Set<Dependence> getDependencesViaTargetComponent(String targetComponent);

	public Set<Dependence> getDependencesViaSourceService(String sourceService);

	public Set<Dependence> getDependencesViaTargetService(String targetService);

	public boolean update(Dependence dependence);

	public boolean contain(Dependence dependence);

}
