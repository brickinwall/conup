package cn.edu.nju.moon.conup.core.utils;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;

/**
 * @author rgc
 * @version Dec 10, 2012 4:37:27 PM
 */
public class TranquillityPayloadCreator {
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
			TranquillityOperationType operation, String parentTxID, String subTxID){
		String result = null;
		
		result = TranquillityPayload.SRC_COMPONENT + ":" + srcComp + "," +
				TranquillityPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				TranquillityPayload.ROOT_TX + ":" + rootTx + "," +
				TranquillityPayload.OPERATION_TYPE + ":" + operation + "," +
				TranquillityPayload.PARENT_TX + ":" + parentTxID + "," +
				TranquillityPayload.SUB_TX + ":" + subTxID;
		
		return result;
	}
	
	/**
	 * method used to create payload which do not contain parent and sub tx infos
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @param operation
	 * @return
	 */
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			TranquillityOperationType operation){
		String result = null;
		
		result = TranquillityPayload.SRC_COMPONENT + ":" + srcComp + "," +
				TranquillityPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				TranquillityPayload.ROOT_TX + ":" + rootTx + "," +
				TranquillityPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			TranquillityOperationType operation, Scope scope){
		String result = null;
		
		result = TranquillityPayload.SRC_COMPONENT + ":" + srcComp + "," +
				TranquillityPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				TranquillityPayload.ROOT_TX + ":" + rootTx + "," +
				TranquillityPayload.OPERATION_TYPE + ":" + operation + "," +
				TranquillityPayload.SCOPE + ":" + scope.toString();
		
		return result;
	}
	
	public static String createNormalRootTxEndPayload(String srcComp, String targetComp, String rootTx,
			TranquillityOperationType operation){
		String result = null;
		
		result = TranquillityPayload.SRC_COMPONENT + ":" + srcComp + "," +
				TranquillityPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				TranquillityPayload.ROOT_TX + ":" + rootTx + "," +
				TranquillityPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
				
	}
	
	public static String createRemoteUpdateIsDonePayload(String srcComp, String targetComp, TranquillityOperationType opType){
		String result = null;
		
		result = ConsistencyPayload.SRC_COMPONENT + ":" + srcComp + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				ConsistencyPayload.OPERATION_TYPE + ":" + opType + "," ;
		
		return result;
	}
}
