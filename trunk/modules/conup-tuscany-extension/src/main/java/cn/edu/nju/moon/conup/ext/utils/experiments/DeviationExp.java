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

public class DeviationExp {
	private Logger LOGGER = Logger.getLogger(DeviationExp.class.getName());
	private static String absolutePath = null;
	private static String fileName = null;
	private static DeviationExp experiment = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private int nThreads;
	private int threadId;
	
	private DeviationExp() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		String algorithm = xmlUtil.getAlgorithmConf();
		algorithm = algorithm.substring(0, algorithm.indexOf("_ALGORITHM"));
		String freenessStrategy = xmlUtil.getFreenessStrategy();
		freenessStrategy = freenessStrategy.substring(0, freenessStrategy.indexOf("_FOR_FREENESS"));
		expSetting = xmlUtil.getExpSetting();
		nThreads = expSetting.getnThreads();
		threadId = expSetting.getThreadId();
		String expType = expSetting.getType();
		String targetComp = expSetting.getTargetComp();
		int rqstInterval = expSetting.getRqstInterval();
	
		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/deviation/";
		fileName = algorithm + "_" + freenessStrategy + "_" + "deviation" + "_{" + nThreads + "_" + threadId + "}_" + rqstInterval + "_"
				+ targetComp + ".csv";
		LOGGER.fine("result file:" + fileName);
		try {
			File file = new File(absolutePath + fileName);
			out = new PrintWriter(new FileWriter(file), true);
			out.write("#" + this + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	public static DeviationExp getInstance() {
		if (experiment == null) {
			synchronized (DeviationExp.class) {
				experiment = new DeviationExp();
			}
		}
		return experiment;
	}

	public void close() {
		synchronized (experiment) {
			out.close();
		}
	}
	
	@Override
	public String toString() {
		return "Round, ThreadId, RequestArrivalTime, NormalResponse, UpdateResponse, TimelinessTime";
	}

	public void writeToFile(int round, Map<Integer, Long> normalRes, Map<Integer, Long> updateRes) {
		synchronized (experiment) {
			String data = null;
			for(int i = 1; i <= normalRes.size(); i++){
				data = round + "," + i + "," + normalRes.get(new Integer(i)) * 1e-6 + "," + updateRes.get(new Integer(i)) * 1e-6 + "\n";
				out.write(data);
				out.flush();
			}
		}
	}

	public void writeToFile(int round, Set<RqstInfo> updateResInfos) {
		synchronized (experiment) {
			String data = null;
			for(RqstInfo rqstInfo : updateResInfos){
				data = round + ","  + rqstInfo.getThreadId() + "," + rqstInfo.getAbsoluteTime() + "," + (rqstInfo.getEndTime() - rqstInfo.getStartTime()) * 1e-6 + "\n";
				out.write(data);
				out.flush();
			}
		}
	}

	public void writeToFile(int round, Set<RqstInfo> updateResInfos, Map<Integer, Long> normalRes, double timelinessTime) {
		synchronized (experiment) {
			String data = null;
			int i = 0;
			for(RqstInfo rqstInfo : updateResInfos){
				if(i == 0){
					data = round + ","  + rqstInfo.getThreadId() + "," + rqstInfo.getAbsoluteTime() 
							+ "," + normalRes.get(rqstInfo.getThreadId()) * 1e-6
							+ "," + (rqstInfo.getEndTime() - rqstInfo.getStartTime()) * 1e-6 
							+ "," + timelinessTime + "\n";
				} else {
					data = round + ","  + rqstInfo.getThreadId() + "," + rqstInfo.getAbsoluteTime() 
							+ "," + normalRes.get(rqstInfo.getThreadId()) * 1e-6
							+ "," + (rqstInfo.getEndTime() - rqstInfo.getStartTime()) * 1e-6 
							+ "\n";
				}
				i ++;
				out.write(data);
				out.flush();
			}
		}
	}
}
