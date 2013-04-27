package cn.edu.nju.moon.conup.ext.utils.experiments;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.TimelinessExp;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class PerformanceRecorder {
    //private static ExecutionRecorder exeRecorder = new ExecutionRecorder();
	private static Map<String, PerformanceRecorder> componentIdentifierToPerformanceRecorderMap = new ConcurrentSkipListMap<String, PerformanceRecorder>();
	
	
	private long updateStartTime;
	private long updateEndTime;
	
	public long getUpdateEndTime() {
		return updateEndTime;
	}

	private long ondemandStartTime;
	private long ondemandEndTime;
	
	private PerformanceRecorder(){
		
	}

	public static PerformanceRecorder getInstance(String compIdentifier){
		synchronized (componentIdentifierToPerformanceRecorderMap) {
			if(!componentIdentifierToPerformanceRecorderMap.containsKey(compIdentifier)){
				
				componentIdentifierToPerformanceRecorderMap.put(compIdentifier, new PerformanceRecorder());
			} 
			return componentIdentifierToPerformanceRecorderMap.get(compIdentifier);
		}
	}
	
	public void ondemandRqstReceived(long ondemandStartTime){
		this.ondemandStartTime = ondemandStartTime;
	}
	
	public void ondemandIsDone(long ondemandEndTime){
		this.ondemandEndTime = ondemandEndTime;
	}
	
	public void updateIsDone(long endTime) {
		this.updateEndTime = endTime;

		try {
			String data = (this.ondemandEndTime - this.ondemandStartTime)
					* 1e-6 + "," + (this.updateEndTime - this.updateStartTime)
					* 1e-6;
			TimelinessExp.getInstance().writeToFile(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void updateReceived(long startTime){
		this.updateStartTime = startTime;
	}
	
}
