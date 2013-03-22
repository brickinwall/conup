package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class Experiment {
	private Logger LOGGER = Logger.getLogger(Experiment.class.getName());
	private static String absolutePath= "/home/valerio/workspace/conUp/tuscany-sca/experiments/results/";
	private static String fileName ="timeliness_experiment_results.csv";
	private static Experiment experiment = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private int nThreads;
	
	public ExpSetting getExpSetting() {
		return expSetting;
	}

	private Experiment() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		expSetting = xmlUtil.getExpSetting();
		int nThreads = expSetting.getnThreads();
		int threadId = expSetting.getThreadId();
		String expType = expSetting.getType();
		String targetComp = expSetting.getTargetComp();
		
		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/";
		fileName = expType + "_{" + nThreads + "}_" + threadId + "_" + targetComp + ".csv";
		LOGGER.fine("result file:" + fileName);
		try {
			File file = new File(absolutePath+fileName);
			out = new PrintWriter(new FileWriter(file,true),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Experiment getInstance() {
		if(experiment == null) {
			synchronized (Experiment.class) {
				experiment = new Experiment();
			}
		}
		return experiment;
	}
	
	public void writeDisruptionToFile(int roundId, int curThreadId, String statusWhenStart, String statusWhenEnd, double responseTime){
		synchronized(experiment){
			out.write(roundId + "," + nThreads + "," + curThreadId + "," + statusWhenStart + "," + statusWhenEnd + "," + responseTime + "\n");
			out.flush();
//			out.println(nThreads + "," + curThreadId + "," + statusWhenStart + "," + statusWhenEnd + "," + responseTime);
		}
	}
	
	public void writeToFile(String data) {
		synchronized (experiment) {
			LOGGER.fine("I'm writing: " + data);
//			out.println(data);			
			out.write(data);
			out.flush();
		}
	}
	
	public void close() {
		synchronized (experiment) {
			out.close();
		}
	}

}
