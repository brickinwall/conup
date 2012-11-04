/**
 * 
 */
package cn.edu.nju.moon.conup.spi.datamodel;


/**
 * 
 * A general interface for different update algorithm(Version-consistency, quiescence and tranquillity).
 * Since the process of on-demand setup is different,  each algorithm should provide a class implements the interface
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface OndemandSetup {
	/**
	 * represent the process of on-demand setup
	 * @param targetComponent name of the component needs to be updated
	 * @param freenessSetup the strategy used to achieve freeness, i.e., CV/BF/WF
	 */
	public void ondemand(String targetComponent, String freenessSetup, Scope scope);
	
	/**
	 * Since the on-demand setup is asynchronous, the method is used to query on-demand setup status
	 * @return
	 */
	public boolean isOndemandDone();
	
	
}
