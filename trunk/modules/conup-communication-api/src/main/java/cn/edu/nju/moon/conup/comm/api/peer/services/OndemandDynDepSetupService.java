package cn.edu.nju.moon.conup.comm.api.peer.services;


/**
 * on-demand setup between different nodes
 * @author Jiang Wang
 *
 */
public interface OndemandDynDepSetupService {
	/**
	 * synchronously notify target component
	 * @param targetComp target component's name
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public String synPost(String targetComp, String proctocol, String msgType, String payload);
	
	/**
	 * asynchronously notify target component
	 * @param targetComp target component's name
	 * @param proctocol the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	 * @param msgType XML, JSON, etc.
	 * @param payload
	 * @return
	 */
	public String asynPost(String targetComp, String proctocol, String msgType, String payload);

}
