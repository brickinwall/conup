package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class DisruptionExp {
	private Logger LOGGER = Logger.getLogger(DisruptionExp.class.getName());
	private static String absolutePath = null;
	private static String fileName = null;
	private static DisruptionExp experiment = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private int nThreads;
	private int threadId;

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	private DisruptionExp() {
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

		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/";
		fileName = algorithm + "_" + freenessStrategy + "_" + expType + "_{" + nThreads + "}_" + threadId + "_"
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

	public static DisruptionExp getInstance() {
		if (experiment == null) {
			synchronized (DisruptionExp.class) {
				experiment = new DisruptionExp();
			}
		}
		return experiment;
	}

	public void writeResponseTimeToFile(int roundId, int curThreadId,
			String statusWhenStart, String statusWhenEnd, double responseTime) {
		synchronized (experiment) {
			LOGGER.info("I'm writing to disruption.. ");
			String data = roundId + "," + nThreads + "," + curThreadId + ","
					+ statusWhenStart + "," + statusWhenEnd + ","
					+ responseTime + "\n";
			out.write(data);
			out.flush();
		}
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

	@Override
	public String toString() {
		return "Round, ThreadId, NormalResponse, UpdateResponse";
	}
}
