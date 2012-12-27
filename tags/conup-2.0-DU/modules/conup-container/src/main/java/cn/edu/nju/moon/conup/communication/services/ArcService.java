package cn.edu.nju.moon.conup.communication.services;

import org.oasisopen.sca.annotation.Remotable;

import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.Scope;

@Remotable
public interface ArcService {
	void createArc(Arc arc);
	void readArc();
	void update(Arc arc, String flag);	
	void removeArc(Arc arc);
	/** 
	 * When a sub transaction starts/ends, its parent component should be notified. 
	 * In these cases, a sub component will notify its parent component via 
	 * this method
	 * @param parentTx 
	 * @param subTxID 
	 * @param subTxHost
	 * @param subTxStatus
	 *  */
	void notifySubTxStatus(String parentTx, String subTxID, String subTxHost, String subTxStatus);
//	void notifySubTxEnd(Arc arc, String txID);
	
	/** when a root transaction starts, future arcs need to be created recursively. */
	boolean setUp(Arc arc, Scope scope);
	/** when a root transaction ends, future/past arcs need to be created recursively. */
	boolean cleanUp(String rootID, Scope scope);

}
