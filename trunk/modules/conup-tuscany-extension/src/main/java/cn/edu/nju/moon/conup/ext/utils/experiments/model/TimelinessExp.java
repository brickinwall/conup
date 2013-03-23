package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class TimelinessExp {
	private static final Logger LOGGER = Logger.getLogger(TimelinessExp.class.getName());
	
	private static String absolutePath = null;
	private static String fileName = null;
	private static TimelinessExp timeliness = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private int nThreads;
	private int threadId;

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	private TimelinessExp() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		expSetting = xmlUtil.getExpSetting();
		nThreads = expSetting.getnThreads();
		threadId = expSetting.getThreadId();
		String expType = expSetting.getType();
		String targetComp = expSetting.getTargetComp();

		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/";
		fileName = expType + "_{" + nThreads + "}_" + threadId + "_"
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

	public static TimelinessExp getInstance() {
		if (timeliness == null) {
			synchronized (TimelinessExp.class) {
				timeliness = new TimelinessExp();
			}
		}
		return timeliness;
	}

//	public void writeResponseTimeToFile(int roundId, int curThreadId,
//			String statusWhenStart, String statusWhenEnd, double responseTime) {
//		synchronized (timeliness) {
//			LOGGER.info("I'm writing to disruption.. ");
//			String data = roundId + "," + nThreads + "," + curThreadId + ","
//					+ statusWhenStart + "," + statusWhenEnd + ","
//					+ responseTime + "\n";
//			out.write(data);
//			out.flush();
//		}
//	}

	public void writeToFile(int roundId, double updateTime) {
		synchronized (timeliness) {
			LOGGER.info("I'm writing: " + roundId + "," + updateTime);
			String data = roundId + "," + updateTime + "\n"; 
			out.write(data);
			out.flush();
		}
	}

	public void close() {
		synchronized (timeliness) {
			out.close();
		}
	}

	@Override
	public String toString() {
		return "Run Identifier, Quiescence, Tranquillty, Consistency(WF), Consistency(BF), Consistency(CV)";
	}
}
