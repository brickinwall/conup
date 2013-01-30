package cn.edu.nju.moon.conup.ext.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class PerformanceRecorder {
//	private static ExecutionRecorder exeRecorder = new ExecutionRecorder();
	private static Map<String, PerformanceRecorder> allExeRecorders = new ConcurrentSkipListMap<String, PerformanceRecorder>();
	
	private long startTime;
	
	private long endTime;
	
	private PerformanceRecorder(){
	}

	public static PerformanceRecorder getInstance(String compIdentifier){
		synchronized (allExeRecorders) {
			if(allExeRecorders.get(compIdentifier) == null){
				allExeRecorders.put(compIdentifier, new PerformanceRecorder());
			} 
			return allExeRecorders.get(compIdentifier);
		}
//		return exeRecorder;
	}
	
	
	public void updateIsDone(long endTime){
		this.endTime = endTime;
		System.out.println("update has taken time: " + (endTime - startTime) / 1000000.0 + " ms");
	}
	
	public void updateReceived(long startTime){
		this.startTime = startTime;
	}
	
}
