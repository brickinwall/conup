package cn.edu.nju.moon.conup.comm.api.peer.services;

import cn.edu.nju.moon.conup.spi.datamodel.MsgType;

/**
 * exchange dependences between different nodes
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public interface DepNotifyService {
	/**
	 * synchronously notify target component
	 * @param srcIdentifier 
	 * @param targetIdentifier target component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param payload (msgType XML, JSON, etc.)
	 * @return
	 */
	public String synPost(String srcIdentifier, String targetIdentifier, String proctocol, MsgType msgType, String payload);
	
	/**
	 * asynchronously notify target component
	 * @param srcIdentifier 
	 * @param targetComp target component object identifier
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public void asynPost(String srcIdentifier, String targetIdentifier, String proctocol, MsgType msgType, String payload);
	
}
