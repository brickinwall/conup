package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;

public class ResponseTimeRecorder {
	private static Logger LOGGER = Logger.getLogger(ResponseTimeRecorder.class.getName());
	private Map<Integer, Long> normalRes = new ConcurrentHashMap<Integer, Long>();
	private Map<Integer, Long> updateRes = new ConcurrentHashMap<Integer, Long>();
	private Set<RqstInfo> updateResInfos = new ConcurrentSkipListSet<RqstInfo>();

	public Map<Integer, Long> getNormalRes() {
		return normalRes;
	}

	public Map<Integer, Long> getUpdateRes() {
		return updateRes;
	}

	public void addUpdateResInfo(RqstInfo rqstInfo) {
		assert updateResInfos.contains(rqstInfo) != true;
		updateResInfos.add(rqstInfo);
	}
	
	/**
	 * @return get all the response time of disrupted txs,
	 * key:threadId, value:response time
	 */
	public Map<Integer, Double> getDisruptedTxResTime(){
		return DisruptionExp.getInstance().getDisruptedTxsResTime(updateResInfos);
	}

	public void addNormalResponse(int threadId, long time) {
		assert normalRes.get(threadId) == null;
		normalRes.put(threadId, time);
	}

	public void addUpdateResponse(int threadId, long time) {
		assert updateRes.get(threadId) == null;
		updateRes.put(threadId, time);
	}

	public Set<RqstInfo> getUpdateResInfos() {
		return updateResInfos;
	}
	
	public double getTotalNormalResTime() {
		long totalTime = 0;
		Iterator<Entry<Integer, Long>> iterator;

		iterator = normalRes.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Long> entry = iterator.next();
			totalTime += entry.getValue();
		}
		return totalTime * 1e-6;
	}

	public double getTotalUpdateResTime() {
		long totalTime = 0;
		Iterator<Entry<Integer, Long>> iterator;

		iterator = updateRes.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Long> entry = iterator.next();
			totalTime += entry.getValue();
		}
		return totalTime * 1e-6;

	}

	public double getAverageNormalResTime() {
		int totalSize = normalRes.size();
		return getTotalNormalResTime() / totalSize;
	}

	public double getAverageUpdateResTime() {
		int totalSize = updateRes.size();
		return getTotalUpdateResTime() / totalSize;
	}

}
