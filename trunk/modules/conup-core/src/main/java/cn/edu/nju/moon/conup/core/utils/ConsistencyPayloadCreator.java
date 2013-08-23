package cn.edu.nju.moon.conup.core.utils;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.utils.OperationType;
import cn.edu.nju.moon.conup.spi.utils.PayloadType;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ConsistencyPayloadCreator {
	
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
			OperationType operation, String parentTxID, String subTxID){
		String result = null;
		
		result = PayloadType.SRC_COMPONENT + ":" + srcComp + "," +
				PayloadType.TARGET_COMPONENT + ":" + targetComp + "," +
				PayloadType.ROOT_TX + ":" + rootTx + "," +
				PayloadType.OPERATION_TYPE + ":" + operation + "," +
				PayloadType.PARENT_TX + ":" + parentTxID + "," +
				PayloadType.SUB_TX + ":" + subTxID;
		
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
			OperationType operation){
		String result = null;
		
		result = PayloadType.SRC_COMPONENT + ":" + srcComp + "," +
				PayloadType.TARGET_COMPONENT + ":" + targetComp + "," +
				PayloadType.ROOT_TX + ":" + rootTx + "," +
				PayloadType.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			OperationType operation, Scope scope){
		String result = null;
		
		result = PayloadType.SRC_COMPONENT + ":" + srcComp + "," +
				PayloadType.TARGET_COMPONENT + ":" + targetComp + "," +
				PayloadType.ROOT_TX + ":" + rootTx + "," +
				PayloadType.OPERATION_TYPE + ":" + operation + "," +
				PayloadType.SCOPE + ":" + scope.toString();
		
		return result;
	}
	
	public static String createNormalRootTxEndPayload(String srcComp, String targetComp, String rootTx,
			OperationType operation){
		String result = null;
		
		result = PayloadType.SRC_COMPONENT + ":" + srcComp + "," +
				PayloadType.TARGET_COMPONENT + ":" + targetComp + "," +
				PayloadType.ROOT_TX + ":" + rootTx + "," +
				PayloadType.OPERATION_TYPE + ":" + operation;
		
		return result;
				
	}
	
	public static String createRemoteUpdateIsDonePayload(String srcComp, String targetComp, OperationType opType){
		String result = null;
		
		result = PayloadType.SRC_COMPONENT + ":" + srcComp + "," +
				PayloadType.TARGET_COMPONENT + ":" + targetComp + "," +
				PayloadType.OPERATION_TYPE + ":" + opType + "," ;
		
		return result;
	}
	
}
