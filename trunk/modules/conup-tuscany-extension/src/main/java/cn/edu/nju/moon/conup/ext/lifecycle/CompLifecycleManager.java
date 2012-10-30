/**
 * 
 */
package cn.edu.nju.moon.conup.ext.lifecycle;

/**
 * Component life cycle manager
 * 
 * @author Jiang Wang
 *
 */
public class CompLifecycleManager {

	/**
	 * 
	 * @param contributionUri
	 * @return
	 */
	public boolean uninstall(String contributionUri){
		boolean isUninstalled = false;
		
		return isUninstalled;
	}
	
	/**
	 * 
	 * @param contributionUri
	 * @return
	 */
	public boolean install(String contributionUri){
		boolean isInstalled = false;
		
		return isInstalled;
	}
	
	/**
	 * stop a contribution
	 * @param contributionUri
	 * @return
	 */
	public boolean stop(String contributionUri){
		boolean isStopped = false;
		
		return isStopped;
	}
	
	/**
	 * 
	 * @param implType the implementation type of the component, e.g., POJO, EJB.
	 * @param baseDir
	 * @param classPath
	 * @param contributionURI
	 * @param compositeURI
	 * @return
	 */
	public boolean update(String implType, String baseDir, String classPath, 
			String contributionURI, String compositeURI){
		return true;
	}
	
	//create a version_number for each comp
}
