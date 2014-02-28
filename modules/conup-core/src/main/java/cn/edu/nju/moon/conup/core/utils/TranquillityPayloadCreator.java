package cn.edu.nju.moon.conup.core.utils;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.DepPayload;

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
	
	/**
	 * method used to create payload which do not contain parent and sub tx infos
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @param operation
	 * @return
	 */
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			DepOperationType operation){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.ROOT_TX + ":" + rootTx + "," +
				DepPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			DepOperationType operation, Scope scope){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.ROOT_TX + ":" + rootTx + "," +
				DepPayload.OPERATION_TYPE + ":" + operation + "," +
				DepPayload.SCOPE + ":" + scope.toString();
		
		return result;
	}
	
	public static String createNormalRootTxEndPayload(String srcComp, String targetComp, String rootTx,
			DepOperationType operation){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.ROOT_TX + ":" + rootTx + "," +
				DepPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
				
	}
	
	public static String createRemoteUpdateIsDonePayload(String srcComp, String targetComp, DepOperationType opType){
		String result = null;
		
		result = DepPayload.SRC_COMPONENT + ":" + srcComp + "," +
				DepPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				DepPayload.OPERATION_TYPE + ":" + opType + "," ;
		
		return result;
	}
}
