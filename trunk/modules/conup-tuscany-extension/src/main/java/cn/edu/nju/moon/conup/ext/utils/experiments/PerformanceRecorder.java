package cn.edu.nju.moon.conup.ext.utils.experiments;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.Experiment;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class PerformanceRecorder {
    //private static ExecutionRecorder exeRecorder = new ExecutionRecorder();
	private static Map<String, PerformanceRecorder> componentIdentifierToPerformanceRecorderMap = new ConcurrentSkipListMap<String, PerformanceRecorder>();
	
	
	private long startTime;
	
	private long endTime;
	
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
	
	
	public void updateIsDone(long endTime){
		this.endTime = endTime;
		String data ="update has taken time:," + (this.endTime - startTime) / 1000000.0 + "\n";
		ExpSetting expSetting = Experiment.getInstance().getExpSetting();
		if(expSetting.getType().contains("timeliness"))
			Experiment.getInstance().writeToFile(data);
	}
	
	public void updateReceived(long startTime){
		this.startTime = startTime;
	}
	
}
