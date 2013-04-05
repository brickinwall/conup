package cn.edu.nju.moon.conup.spi.utils;

import java.util.ArrayList;
import java.util.Iterator;
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
	
	private static final String RCVD_UPDATE_RQST = "RCVD_UPDATE_RQST";
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
	
	public synchronized void addAction(String rootTx, String action){
//		System.out.println("ExecutionRecorder.addAction(): " + rootTx + " " + action);
		if(exeProc.get(rootTx) == null){
			List<String> list = new ArrayList<String>();
			list.add(action);
			exeProc.put(rootTx, list);
		}  else{
			List<String> list = exeProc.get(rootTx);
			list.add(action);
		}
		
	}
	
	public synchronized String getActions(){
		StringBuffer buffer = new StringBuffer();
		Iterator<Entry<String, List<String>>> iterator = exeProc.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, List<String>> entry = iterator.next();
			buffer.append(entry.getKey() + ": " + entry.getValue().toString() + "\n");
		}
		return buffer.toString();
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
	
	public synchronized void receiveUpdateRequest(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(RCVD_UPDATE_RQST);
		}
	}
	
	public synchronized void updateIsDone(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(UPDATE_IS_DONE);
		}
	}
	
	public synchronized void ondemandIsDone(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(ONDEMAND_IS_DONE);
		}
	}
	
	public synchronized void achievedFree(){
		for (Entry<String, List<String>> entry : exeProc.entrySet()) {
			List<String> list = entry.getValue();
			list.add(COMP_IS_FREE);
		}
	}
}
