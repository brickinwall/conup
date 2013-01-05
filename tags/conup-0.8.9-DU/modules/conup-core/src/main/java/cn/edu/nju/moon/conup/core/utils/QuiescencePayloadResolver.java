package cn.edu.nju.moon.conup.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rgc
 * @version Dec 18, 2012 4:24:34 PM
 */
public class QuiescencePayloadResolver {
	private QuiescenceOperationType operation;
	private Map<QuiescencePayload, String> parameters;
	
	public QuiescencePayloadResolver(String payload){
		parameters = new HashMap<QuiescencePayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public QuiescenceOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(QuiescencePayload paraType){
		return parameters.get(paraType);
	}
	
	/* resolve payload */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(QuiescencePayload.class, pair[0]).equals(QuiescencePayload.OPERATION_TYPE)){
				operation = Enum.valueOf(QuiescenceOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(QuiescencePayload.class, pair[0]), pair[1]);
			}
		}
	}
}
