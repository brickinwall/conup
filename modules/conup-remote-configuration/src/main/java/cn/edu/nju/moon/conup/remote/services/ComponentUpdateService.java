package cn.edu.nju.moon.conup.remote.services;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;



/**
 * A interface for remote management, such as on-demand, update
 * @author Jiang Wang
 *
 */
public interface ComponentUpdateService {
//	boolean update();
	
	/**
	 * a user can start a dynamic update
	 * @param targetComp
	 * @param baseDir
	 * @param classPath
	 * @param contributionURI
	 * @param compositeURI
	 * @return
	 */
	public boolean update(String targetComp, String baseDir, String classPath, String contributionURI, String compositeURI);
	
	/**
	 * A user can start a on-demand setup by invoking the method
	 * @param targetComponent
	 * @param freenessSetup
	 * @param scope a Scope object
	 * @return
	 */
	public boolean ondemand(String targetComponent, String freenessSetup, Scope scope);
	
	/**
	 * create a version number for each component
	 * @param targetComponent
	 * @return
	 */
	public boolean isUpdated(String targetComponent);
}
