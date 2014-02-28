package cn.edu.nju.moon.conup.spi.utils;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;

/**
 * @author rgc
 * @version Dec 2, 2012 1:30:48 PM
 */
public class UpdateContextPayloadCreator {

	/**
	 * create udpate payload
	 * 
	 * @param operationType
	 * @param targetCompIdentifier
	 * @param baseDir
	 * @param classFilePath
	 * @param contributionUri
	 * @param compsiteUri
	 * @return
	 */
	public static String createPayload(UpdateOperationType operationType,
			String targetCompIdentifier, String baseDir, String classFilePath,
			String contributionUri, String compsiteUri) {
		String result = null;
		result = UpdateContextPayload.OPERATION_TYPE + ":" + operationType
				+ "," + UpdateContextPayload.COMP_IDENTIFIER + ":"
				+ targetCompIdentifier + "," + UpdateContextPayload.BASE_DIR
				+ ":" + baseDir + "," + UpdateContextPayload.CLASS_FILE_PATH
				+ ":" + classFilePath + ","
				+ UpdateContextPayload.CONTRIBUTION_URI + ":" + contributionUri
				+ "," + UpdateContextPayload.COMPOSITE_URI + ":" + compsiteUri;
		return result;
	}

	/**
	 * 
	 * @param operationType
	 * @param targetCompIdentifier
	 * @param baseDir
	 * @param classFilePath
	 * @param contributionUri
	 * @param compsiteUri
	 * @param scope
	 * @return
	 */
	public static String createPayload(UpdateOperationType operationType,
			String targetCompIdentifier, String baseDir, String classFilePath,
			String contributionUri, String compsiteUri, Scope scope) {
		StringBuffer result = new StringBuffer(createPayload(operationType,
				targetCompIdentifier, baseDir, classFilePath, contributionUri,
				compsiteUri));
		if (scope != null)
			result.append(",").append(UpdateContextPayload.SCOPE).append(":")
					.append(scope.toString());

		return result.toString();
	}

	/**
	 * create ondemand/query payload
	 * 
	 * @param operationType
	 * @param targetCompIdentifier
	 * @return
	 */
	public static String createPayload(UpdateOperationType operationType,
			String targetCompIdentifier) {
		String result = null;
		result = UpdateContextPayload.OPERATION_TYPE + ":" + operationType
				+ "," + UpdateContextPayload.COMP_IDENTIFIER + ":"
				+ targetCompIdentifier;
		return result;
	}
	
	public static String createPayload(UpdateOperationType operationType) {
		String result = null;
		result = UpdateContextPayload.OPERATION_TYPE + ":" + operationType;
		return result;
	}

	public static String createPayload(UpdateOperationType operationType,
			String targetCompIdentifier, Scope scope) {
		StringBuffer result = new StringBuffer(createPayload(operationType,
				targetCompIdentifier));
		if (scope != null)
			result.append(",").append(UpdateContextPayload.SCOPE).append(":")
					.append(scope.toString());

		return result.toString();
	}
}
