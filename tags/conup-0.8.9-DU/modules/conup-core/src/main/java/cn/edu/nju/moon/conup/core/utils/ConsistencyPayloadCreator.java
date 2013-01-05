package cn.edu.nju.moon.conup.core.utils;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;

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
			ConsistencyOperationType operation, String parentTxID, String subTxID){
		String result = null;
		
		result = ConsistencyPayload.SRC_COMPONENT + ":" + srcComp + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				ConsistencyPayload.ROOT_TX + ":" + rootTx + "," +
				ConsistencyPayload.OPERATION_TYPE + ":" + operation + "," +
				ConsistencyPayload.PARENT_TX + ":" + parentTxID + "," +
				ConsistencyPayload.SUB_TX + ":" + subTxID;
		
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
			ConsistencyOperationType operation){
		String result = null;
		
		result = ConsistencyPayload.SRC_COMPONENT + ":" + srcComp + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				ConsistencyPayload.ROOT_TX + ":" + rootTx + "," +
				ConsistencyPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
	}
	
	public static String createPayload(String srcComp, String targetComp, String rootTx,
			ConsistencyOperationType operation, Scope scope){
		String result = null;
		
		result = ConsistencyPayload.SRC_COMPONENT + ":" + srcComp + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				ConsistencyPayload.ROOT_TX + ":" + rootTx + "," +
				ConsistencyPayload.OPERATION_TYPE + ":" + operation + "," +
				ConsistencyPayload.SCOPE + ":" + scope.toString();
		
		return result;
	}
	
	public static String createNormalRootTxEndPayload(String srcComp, String targetComp, String rootTx,
			ConsistencyOperationType operation){
		String result = null;
		
		result = ConsistencyPayload.SRC_COMPONENT + ":" + srcComp + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + targetComp + "," +
				ConsistencyPayload.ROOT_TX + ":" + rootTx + "," +
				ConsistencyPayload.OPERATION_TYPE + ":" + operation;
		
		return result;
				
	}
	
}
