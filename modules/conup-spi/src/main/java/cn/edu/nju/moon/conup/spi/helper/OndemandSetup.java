/**
 * 
 */
package cn.edu.nju.moon.conup.spi.helper;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;


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
	 * @param freenessSetup the strategy used to achieve freeness, i.e., CV/BF/WF
	 * @param scope
	 */
	public void ondemand(String freenessSetup, Scope scope);
	
	/**
	 * received on-demand notification from peer component
	 * @param srcIdentifier source component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean ondemandSetup(String srcIdentifier, String proctocol, String msgType, String payload);
	
	/**
	 * Since the on-demand setup is asynchronous, the method is used to query on-demand setup status
	 * @return
	 */
	public boolean isOndemandDone();
	
	/**
	 * Each algorithm implementation should have an identifier/type that 
	 * uniquely identify itself.
	 * @return
	 */
	public String getAlgorithmType();
	
}
