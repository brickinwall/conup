/**
 * 
 */
package cn.edu.nju.moon.conup.ext.update;

/**
 * 	A uniform interface for updating components implemented in different implementation type, 
 * 	e.g., Java POJO, EJB.
 * 	
 * 	Each implementation type should implement the interface
 * 	@author Jiang Wang
 *
 */
public interface ComponentUpdate {
	/**
	 * This method is used to load new version component/classes, and initiate update environment.
	 * e.g., for concurrent version, it may create dispatcher.
	 * @param baseDir base directory of the new version component/classes
	 * @param classPath class name with its package name
	 * @param contributionURI
	 * @param compositeURI
	 * @return if loaded successfully return true, otherwise return false
	 */
	public boolean start(String baseDir, String classPath, String contributionURI, String compositeURI);
	
	/**
	 * 
	 * @return if dynamic update is done then return true, otherwise return false
	 */
	public boolean isUpdated();
	
	/**
	 * 	When dynamic update is done, cleanup is needed.
	 * 	@return 
	 */
	public boolean cleanUpdate();
	
	/**
	 * @return 
	 */
	public String getImplementationType();
}
