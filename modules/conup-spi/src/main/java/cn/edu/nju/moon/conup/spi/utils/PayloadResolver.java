package cn.edu.nju.moon.conup.spi.utils;

import java.util.HashMap;
import java.util.Map;

public class PayloadResolver {
	private OperationType operation;
	private Map<PayloadType, String> parameters;
	
	public PayloadResolver(String payload){
		parameters = new HashMap<PayloadType, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public OperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(PayloadType paraType){
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
			if(Enum.valueOf(PayloadType.class, pair[0]).equals(PayloadType.OPERATION_TYPE)){
				operation = Enum.valueOf(OperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(PayloadType.class, pair[0]), pair[1]);
			}
		}
	}
}
