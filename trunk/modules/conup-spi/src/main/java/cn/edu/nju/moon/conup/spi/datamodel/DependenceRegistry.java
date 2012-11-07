package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;


/** dependences for a component. */
public interface DependenceRegistry {
	public void addDependence(Dependence dependence);

	public void removeDependence(Dependence dependence);

	public Set<Dependence> getDependences();

	public Set<Dependence> getDependencesViaType(String type);

	public Set<Dependence> getDependencesViaRootTransaction(String rootTx);

	/**
	 * @param srcIdentifer source component object identifier
	 * @return
	 */
	public Set<Dependence> getDependencesViaSourceComponent(String srcIdentifer);

	/**
	 * @param targetIdentifer target component object identifier
	 * @return
	 */
	public Set<Dependence> getDependencesViaTargetComponent(String targetIdentifer);

	public Set<Dependence> getDependencesViaSourceService(String sourceService);

	public Set<Dependence> getDependencesViaTargetService(String targetService);

	public boolean update(Dependence dependence);

	public boolean contain(Dependence dependence);

}
