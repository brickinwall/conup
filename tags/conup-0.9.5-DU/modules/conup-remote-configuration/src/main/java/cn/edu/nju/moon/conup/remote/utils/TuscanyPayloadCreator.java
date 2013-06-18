package cn.edu.nju.moon.conup.remote.utils;

import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
/**
 * @author rgc
 * @version Dec 2, 2012 1:30:48 PM
 */
public class TuscanyPayloadCreator {
	
	/**
	 * create udpate payload 
	 * @param operationType
	 * @param targetCompIdentifier
	 * @param baseDir
	 * @param classFilePath
	 * @param contributionUri
	 * @param compsiteUri
	 * @return
	 */
	public static String createPayload(TuscanyOperationType operationType, String targetCompIdentifier, String baseDir, String classFilePath, String contributionUri, String compsiteUri){
		String result = null;
		result = TuscanyPayload.OPERATION_TYPE + ":" + operationType + "," + 
				TuscanyPayload.COMP_IDENTIFIER + ":" + targetCompIdentifier + "," +
				TuscanyPayload.BASE_DIR + ":" + baseDir + "," + 
				TuscanyPayload.CLASS_FILE_PATH + ":" + classFilePath + "," +
				TuscanyPayload.CONTRIBUTION_URI + ":" + contributionUri + "," +
				TuscanyPayload.COMPOSITE_URI + ":" + compsiteUri;
		return result;
	}
	
	/**
	 * create ondemand/query payload
	 * @param operationType
	 * @param targetCompIdentifier
	 * @return
	 */
	public static String createPayload(TuscanyOperationType operationType, String targetCompIdentifier){
		String result = null;
		result = TuscanyPayload.OPERATION_TYPE + ":" + operationType + "," +
				TuscanyPayload.COMP_IDENTIFIER + ":" + targetCompIdentifier;
		return result;
	}
	
	public static String createGetExecutionRecorderPayload(TuscanyOperationType operationType, String targetCompIdentifier){
		String result = null;
		result = TuscanyPayload.OPERATION_TYPE + ":" + operationType + "," +
				TuscanyPayload.COMP_IDENTIFIER + ":" + targetCompIdentifier;
		return result;
	}
}
