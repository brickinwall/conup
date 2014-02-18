package cn.edu.nju.moon.conup.spi.utils;

import java.util.HashMap;
import java.util.Map;

public class DepPayloadResolver {
	private DepOperationType operation;
	private Map<DepPayload, String> parameters;
	
	public DepPayloadResolver(String payload){
		parameters = new HashMap<DepPayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public DepOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(DepPayload paraType){
		return parameters.get(paraType);
	}
	
	/**
	 * resolve payload
	 * @param payload
	 */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(DepPayload.class, pair[0]).equals(DepPayload.OPERATION_TYPE)){
				operation = Enum.valueOf(DepOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(DepPayload.class, pair[0]), pair[1]);
			}
		}
	}
}
