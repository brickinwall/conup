package cn.edu.nju.moon.conup.ext.utils.experiments;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseTimeRecorder {
	private Map<Integer, Long> normalRes = new ConcurrentHashMap<Integer, Long>();
	private Map<Integer, Long> updateRes = new ConcurrentHashMap<Integer, Long>();
	
	public void addNormalResponse(int threadId, long time){
		normalRes.put(threadId, time);
	}
	
	public void addUpdateResponse(int threadId, long time){
		updateRes.put(threadId, time);
	}
	
	public double getTotalNormalResTime(){
		long totalTime = 0;
		Iterator<Entry<Integer, Long>> iterator;
		
		iterator = normalRes.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer, Long> entry = iterator.next();
			totalTime += entry.getValue();
		}
		return totalTime / 1000000.0;
	}
	
	public double getTotalUpdateResTime(){
		long totalTime = 0;
		Iterator<Entry<Integer, Long>> iterator;
		
		iterator = updateRes.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer, Long> entry = iterator.next();
			totalTime += entry.getValue();
		}
		return totalTime / 1000000.0;
		
	}
	
	public double getAverageNormalResTime(){
		int totalSize = normalRes.size();
		return getTotalNormalResTime() / totalSize;
	}
	
	public double getAverageUpdateResTime(){
		int totalSize = updateRes.size();
		return getTotalUpdateResTime() / totalSize;
	}

}
