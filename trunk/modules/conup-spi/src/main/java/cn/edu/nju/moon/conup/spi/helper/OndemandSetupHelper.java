package cn.edu.nju.moon.conup.spi.helper;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;

/**
 * An OndemandSetupHelper may be invoked by tuscany-extension module
 * or peer commponents.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface OndemandSetupHelper {
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	public boolean ondemandSetup(String freenessSetup, Scope scope);
	
	/**
	 * received on-demand notification from peer component
	 * @param sourceComponent source component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean ondemandSetup(String sourceComponent, String proctocol, String msgType, String payload);

	/**
	 * @return corresponding component object of the helper
	 */
	public ComponentObject getCompObject();
}
