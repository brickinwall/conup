package cn.edu.nju.moon.conup.spi.utils;

import java.util.HashMap;
import java.util.Map;

import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;


/**
 * @author rgc
 * @version Dec 1, 2012 11:19:42 PM
 */
public class UpdateContextPayloadResolver {
	private UpdateOperationType operation;
	private Map<UpdateContextPayload, String> parameters;
	
	public UpdateContextPayloadResolver(String payload){
		parameters = new HashMap<UpdateContextPayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public UpdateOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(UpdateContextPayload paraType){
		return parameters.get(paraType);
	}
	
	/* resolve payload */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(UpdateContextPayload.class, pair[0]).equals(UpdateContextPayload.OPERATION_TYPE)){
				operation = Enum.valueOf(UpdateOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(UpdateContextPayload.class, pair[0]), pair[1]);
			}
		}
	}
}
