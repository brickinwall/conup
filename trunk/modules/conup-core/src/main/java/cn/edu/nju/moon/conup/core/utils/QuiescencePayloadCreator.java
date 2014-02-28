package cn.edu.nju.moon.conup.core.utils;

import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.DepPayload;

/**
 * @author rgc
 * @version Dec 18, 2012 4:24:19 PM
 */
public class QuiescencePayloadCreator {

	/**
	 * 
	 * @param srcComp
	 * @param targetComp
	 * @param operation
	 * @return
	 */
	public static String createPayload(String srcComp, String targetComp, DepOperationType operation){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.OPERATION_TYPE + ":" + operation + "," ;
		
		return result;
	}
	
	/**
	 * payload contains parent_tx and sub_tx
	 * these msgs should be in payload when operationType is ACK_SUBTX_INIT
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @param operation
	 * @param parentTxID
	 * @param subTxID
	 * @return
	 */
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			DepOperationType operation, String parentTxID, String subTxID){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.ROOT_TX + ":" + rootTx + "," +
				DepPayload.OPERATION_TYPE + ":" + operation + "," +
				DepPayload.PARENT_TX + ":" + parentTxID + "," +
				DepPayload.SUB_TX + ":" + subTxID;
		
		return result;
	}

	public static String createRootTxEndPayload(String srcComp,
			String targetComp, String rootTx,
			DepOperationType operation) {
		
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.ROOT_TX + ":" + rootTx + "," +
				DepPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
}
