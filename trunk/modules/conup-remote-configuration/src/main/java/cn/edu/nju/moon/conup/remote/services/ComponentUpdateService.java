package cn.edu.nju.moon.conup.remote.services;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;



/**
 * A interface for remote management, such as on-demand, update
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface ComponentUpdateService {
//	boolean update();
	
	/**
	 * a user can start a dynamic update
	 * @param targetIdentifier target component object identifier
	 * @param baseDir
	 * @param classPath
	 * @param contributionURI
	 * @param compositeURI
	 * @return
	 */
	public boolean update(String targetIdentifier, String baseDir, String classPath, String contributionURI, String compositeURI);
	
	/**
	 * A user can start a on-demand setup by invoking the method
	 * @param targetIdentifier target component object identifier
	 * @param freenessSetup
	 * @param scope a Scope object
	 * @return
	 */
	public boolean ondemand(String targetIdentifier, String freenessSetup, Scope scope);
	
	/**
	 * create a version number for each component
	 * @param targetIdentifier target component object identifier
	 * @return
	 */
	public boolean isUpdated(String targetIdentifier);
}
