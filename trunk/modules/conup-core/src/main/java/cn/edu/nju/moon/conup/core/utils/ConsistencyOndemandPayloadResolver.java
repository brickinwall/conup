package cn.edu.nju.moon.conup.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A resolver for resolving on-demand setup payload of version-consistency algorithm
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ConsistencyOndemandPayloadResolver {
	private ConsistencyOperationType operation;
	private Map<ConsistencyPayload, String> parameters;
	
	public ConsistencyOndemandPayloadResolver(String payload){
		parameters = new HashMap<ConsistencyPayload, String>();
		resolve(payload);
	}
	
	/**
	 * @return operation 
	 */
	public ConsistencyOperationType getOperation(){
		return operation;
	}
	
	/**
	 * parameter of the operation
	 * @param paraType 
	 * @return
	 */
	public String getParameter(ConsistencyPayload paraType){
		return parameters.get(paraType);
	}
	
	/* resolve payload */
	private void resolve(String payload){
		String [] keyValues = payload.split(",");
		for(String kv : keyValues){
			String [] pair = kv.split(":");
			if(Enum.valueOf(ConsistencyPayload.class, pair[0]).equals(ConsistencyPayload.OPERATION_TYPE)){
				operation = Enum.valueOf(ConsistencyOperationType.class, pair[1]);
			} else{
				parameters.put(Enum.valueOf(ConsistencyPayload.class, pair[0]), pair[1]);
			}
		}
	}
}
