package cn.edu.nju.moon.conup.spi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ExecutionRecorder {
//	private static ExecutionRecorder exeRecorder = new ExecutionRecorder();
	private static Map<String, ExecutionRecorder> allExeRecorders = new ConcurrentSkipListMap<String, ExecutionRecorder>();
	/** 
	 * 	key: rootTx 
	 * 	value: execution process
	 */
	private Map<String, List<String>> exeProc = new ConcurrentSkipListMap<String, List<String>>();
	/**
	 * An action: update is done
	 */
	public static final String UPDATE_IS_DONE = "UPDATE_IS_DONE";
	/**
	 * An action: component is free now
	 */
	public static final String COMP_IS_FREE = "COMP_IS_FREE";
	/**
	 * An action: ondemand setup is done
	 */
	public static final String ONDEMAND_IS_DONE = "ONDEMAND_IS_DONE";
	
	private ExecutionRecorder(){
	}

	public static ExecutionRecorder getInstance(String compIdentifier){
		synchronized (allExeRecorders) {
			if(allExeRecorders.get(compIdentifier) == null){
				allExeRecorders.put(compIdentifier, new ExecutionRecorder());
			} 
			return allExeRecorders.get(compIdentifier);
		}
//		return exeRecorder;
	}
	
	public void addAction(String rootTx, String action){
		if(exeProc.get(rootTx) == null){
			List<String> list = new ArrayList<String>();
			list.add(action);
			exeProc.put(rootTx, list);
		}  else{
			List<String> list = exeProc.get(rootTx);
			list.add(action);
		}
		
	}
	
	public String getAction(String rootTx){
		String result = null;
		
		if(exeProc.get(rootTx) != null){
			result = exeProc.get(rootTx).toString();
		}
		
		result = result.substring(1, result.length()-1);
		
		return result;
	}
	
	public String getCompleteAction(String rootTx){
		String result = null;
		
		if(exeProc.get(rootTx) != null){
			result = exeProc.get(rootTx).toString();
			result = result.substring(1, result.length()-1);
			result = rootTx + ":" + result;
		}
		
		return result;
	}
	
	public void updateIsDone(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(UPDATE_IS_DONE);
		}
	}
	
	public void ondemandIsDone(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(ONDEMAND_IS_DONE);
		}
	}
	
	public void achievedFree(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(COMP_IS_FREE);
		}
	}
}
