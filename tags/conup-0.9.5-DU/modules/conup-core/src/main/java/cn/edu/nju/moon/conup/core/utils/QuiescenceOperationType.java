package cn.edu.nju.moon.conup.core.utils;
/**
 * @author rgc
 * @version Dec 18, 2012 4:24:02 PM
 */
public enum QuiescenceOperationType {
	//ondemand event type
	REQ_ONDEMAND_SETUP,
	CONFIRM_ONDEMAND_SETUP,
	//tx operations
	ACK_SUBTX_INIT,
	NOTIFY_SUBTX_END,
	NOTIFY_ROOT_TX_END,
	
	/** request a component to passivate itself */
	REQ_PASSIVATE,
	/** ACK passivate */
	ACK_PASSIVATE,
	/** notify that a remote update is done */
	NOTIFY_REMOTE_UPDATE_DONE,
}
