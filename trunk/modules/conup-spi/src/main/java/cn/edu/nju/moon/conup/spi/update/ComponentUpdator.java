/**
 * 
 */
package cn.edu.nju.moon.conup.spi.update;

/**
 * 	A uniform interface for updating components implemented in different implementation type, 
 * 	e.g., Java POJO, EJB.
 * 	
 * 	Each implementation type should implement the interface
 * 
 * 	@author JiangWang<jiang.wang88@gmail.com>
 *
 */
public interface ComponentUpdator {
	/**
	 * This method is used to load new version component/classes, and initiate update environment.
	 * e.g., for concurrent version, it may create dispatcher.
	 * @param baseDir base directory of the new version component/classes
	 * @param classPath class name with its package name
	 * @param contributionURI
	 * @param compositeURI
	 * @return if loaded successfully return true, otherwise return false
	 */
	public boolean initUpdator(String baseDir, String classPath, 
			String contributionURI, String compositeURI, String compIdentifier);
	
	public boolean executeUpdate(String compIdentifier);
	
	/**
	 * finalize old version component
	 * @param compIdentifier
	 * @param oldVersion
	 * @param newVersion
	 * @return
	 */
	public boolean finalizeOld(String compIdentifier, Class<?> oldVersion, Class<?> newVersion,
			Transformer transfomer);
	
	/**
	 * initiate new version component, 
	 * @param compName
	 * @param oldVersion
	 * @param newVersion
	 * @return
	 */
	public boolean initNewVersion(String compName, Class<?> newVersion);
	
	/**
	 * 
	 * @return if dynamic update is done then return true, otherwise return false
	 */
	public boolean isUpdated();
	
	/**
	 * 	When dynamic update is done, cleanup is needed.
	 * 	@return 
	 */
	public boolean cleanUpdate(String compIdentifier);
	
	/**
	 * @return the implementation type of the component, e.g., POJO, EJB
	 */
	public String getCompImplType();
}
