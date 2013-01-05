package cn.edu.nju.moon.conup.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rgc
 * @version Dec 10, 2012 4:38:09 PM
 */
public class TranquillityOndemandPayloadResolver {
	private TranquillityOperationType operation;
	private Map<TranquillityPayload, String> parameters;
	
	public TranquillityOndemandPayloadResolver(String payload){
		parameters = new HashMap<TranquillityPayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public TranquillityOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(TranquillityPayload paraType){
		return parameters.get(paraType);
	}
	
	/* resolve payload */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(TranquillityPayload.class, pair[0]).equals(TranquillityPayload.OPERATION_TYPE)){
				operation = Enum.valueOf(TranquillityOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(TranquillityPayload.class, pair[0]), pair[1]);
			}
		}
	}
}
