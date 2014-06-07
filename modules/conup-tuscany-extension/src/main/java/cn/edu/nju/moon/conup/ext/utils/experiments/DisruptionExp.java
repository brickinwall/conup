package cn.edu.nju.moon.conup.ext.utils.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.RqstInfo;
import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class DisruptionExp {
	private Logger LOGGER = Logger.getLogger(DisruptionExp.class.getName());
	private static String absolutePath = null;
	private static String fileName = null;
	private static DisruptionExp experiment = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	
	// update startTime, endTime
	private long updateStartTime = 0L;
	private long updateEndTime = 0L;
	private long updateCostTime = 0L;
	private boolean updateFinished = false;
	private CallBack callBack = null;

	private DisruptionExp() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		String algorithm = xmlUtil.getAlgorithmConf();
		algorithm = algorithm.substring(0, algorithm.indexOf("_ALGORITHM"));
		String freenessStrategy = xmlUtil.getFreenessStrategy();
		freenessStrategy = freenessStrategy.substring(0, freenessStrategy.indexOf("_FOR_FREENESS"));
		expSetting = xmlUtil.getExpSetting();
		String targetComp = expSetting.getTargetComp();
		int rqstInterval = expSetting.getRqstInterval();
	
		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/disruption/";
		fileName = algorithm + "_" + freenessStrategy + "_DisruptionExp_{"
				+ targetComp + "_" + rqstInterval + "}" + ".csv";
		LOGGER.fine("result file:" + fileName);
		try {
			File file = new File(absolutePath + fileName);
			out = new PrintWriter(new FileWriter(file), true);
			out.write("#" + this + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isUpdateFinished() {
		return updateFinished;
	}

	public void setUpdateFinished(boolean updateFinished) {
		this.updateFinished = updateFinished;
	}
	
	public long getUpdateStartTime() {
		return updateStartTime;
	}

	public void setUpdateStartTime(long updateStartTime) {
		this.updateStartTime = updateStartTime;
	}

	public long getUpdateEndTime() {
		return updateEndTime;
	}

	public void setUpdateEndTime(long updateEndTime) {
		this.updateEndTime = updateEndTime;
		this.updateFinished = true;
		if(callBack != null)
			this.callBack.callback();
	}

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	public static DisruptionExp getInstance() {
		if (experiment == null) {
			synchronized (DisruptionExp.class) {
				experiment = new DisruptionExp();
			}
		}
		return experiment;
	}
	/**
	 * @param updateRes
	 * @return get all the response time of disrupted txs
	 */
	public Map<Integer, Double> getDisruptedTxsResTime(Set<RqstInfo> updateRes){
		Map<Integer, Double> disruptedResTime = new ConcurrentHashMap<Integer, Double>();
		Iterator<RqstInfo> iter = updateRes.iterator();
		while(iter.hasNext()){
			RqstInfo rqstInfo = iter.next();
			if(rqstInfo.getEndTime() < updateStartTime)
				continue;
			else if(rqstInfo.getStartTime() > updateEndTime)
				continue;
			else{
				assert disruptedResTime.get(rqstInfo.getThreadId()) == null;
				disruptedResTime.put(rqstInfo.getThreadId(), (rqstInfo.getEndTime() - rqstInfo.getStartTime()) * 1e-6);
			}
		}
		
		// reset updateStartTime and updateEndTime
		updateCostTime = updateEndTime - updateStartTime;
		updateStartTime = 0;
		updateEndTime = 0;
		
		return disruptedResTime;
	}
	
	public void writeToFile(String data) {
		synchronized (experiment) {
			LOGGER.fine("I'm writing: " + data);
			out.write(data);
			out.flush();
		}
	}

	public void close() {
		synchronized (experiment) {
			out.close();
		}
	}
	
	public double getTimelinessTime(){
		return updateCostTime * 1e-6; 
	}

	@Override
	public String toString() {
		return "Round, ThreadId, NormalResponse, UpdateResponse, timelinessTime";
	}

	public void setUpdateIsDoneCallBack(CallBack callBack) {
		LOGGER.fine("setUpdateIsDoneCallBack(...) " + callBack);
		this.callBack = callBack;
		LOGGER.fine("setUpdateIsDoneCallBack(...) " + this.callBack);
	}
}
