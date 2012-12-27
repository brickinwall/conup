package cn.edu.nju.moon.conup.core.utils;

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
	public static String createPayload(String srcComp, String targetComp, QuiescenceOperationType operation){
		String result = null;
		
		result = QuiescencePayload.SRC_COMPONENT + ":" + srcComp + "," +
				QuiescencePayload.TARGET_COMPONENT + ":" + targetComp + "," +
				QuiescencePayload.OPERATION_TYPE + ":" + operation + "," ;
		
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
			QuiescenceOperationType operation, String parentTxID, String subTxID){
		String result = null;
		
		result = QuiescencePayload.SRC_COMPONENT + ":" + srcComp + "," +
				QuiescencePayload.TARGET_COMPONENT + ":" + targetComp + "," +
				QuiescencePayload.ROOT_TX + ":" + rootTx + "," +
				QuiescencePayload.OPERATION_TYPE + ":" + operation + "," +
				QuiescencePayload.PARENT_TX + ":" + parentTxID + "," +
				QuiescencePayload.SUB_TX + ":" + subTxID;
		
		return result;
	}

	public static String createRootTxEndPayload(String srcComp,
			String targetComp, String rootTx,
			QuiescenceOperationType operation) {
		
		String result = null;
		
		result = QuiescencePayload.SRC_COMPONENT + ":" + srcComp + "," +
				QuiescencePayload.TARGET_COMPONENT + ":" + targetComp + "," +
				QuiescencePayload.ROOT_TX + ":" + rootTx + "," +
				QuiescencePayload.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
}
