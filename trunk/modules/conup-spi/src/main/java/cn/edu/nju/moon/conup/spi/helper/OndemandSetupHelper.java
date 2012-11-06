package cn.edu.nju.moon.conup.spi.helper;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;

/**
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
	 * @param sourceComponent source component's name
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public boolean ondemandSetup(String sourceComponent, String proctocol, String msgType, String payload);
}
