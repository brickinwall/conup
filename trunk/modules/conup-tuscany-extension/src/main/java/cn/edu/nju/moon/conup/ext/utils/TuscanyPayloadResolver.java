package cn.edu.nju.moon.conup.ext.utils;

import java.util.HashMap;
import java.util.Map;

import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;


/**
 * @author rgc
 * @version Dec 1, 2012 11:19:42 PM
 */
public class TuscanyPayloadResolver {
	private TuscanyOperationType operation;
	private Map<TuscanyPayload, String> parameters;
	
	public TuscanyPayloadResolver(String payload){
		parameters = new HashMap<TuscanyPayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public TuscanyOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(TuscanyPayload paraType){
		return parameters.get(paraType);
	}
	
	/* resolve payload */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(TuscanyPayload.class, pair[0]).equals(TuscanyPayload.OPERATION_TYPE)){
				operation = Enum.valueOf(TuscanyOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(TuscanyPayload.class, pair[0]), pair[1]);
			}
		}
	}
}
