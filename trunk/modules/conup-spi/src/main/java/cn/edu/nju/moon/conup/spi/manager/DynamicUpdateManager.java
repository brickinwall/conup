/**
 * 
 */
package cn.edu.nju.moon.conup.spi.manager;

/**
 * DynamicUpdateManager provides a series of method for dynamic update.
 * It's supposed to be invoked by:
 * 	0) CompLifecycleManager and 
 * 	1) interceptor for deciding whether a request need to be intercepted
 * @author Jiang Wang
 *
 */
public class DynamicUpdateManager {
	
	/**
	 * is a component valid?
	 * @param compName
	 * @return 
	 */
	public boolean isValid(String compName){
		
		return false;
	}
	
	/**
	 * is a component ready?
	 * @param compName
	 * @return 
	 */
	public boolean isReadyForUpdate(String compName){
		
		return false;
	}
	
	/**
	 * It's used by interceptor for deciding whether a request needs to be intercepted
	 * @param compName component name
	 * @return
	 */
	public boolean isInterceptRequired(String compName){
		
		return false;
	}
}
