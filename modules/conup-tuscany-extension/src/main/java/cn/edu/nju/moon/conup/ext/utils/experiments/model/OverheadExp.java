package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class OverheadExp {
	private Logger LOGGER = Logger.getLogger(OverheadExp.class.getName());
	private static String absolutePath = null;
	private static String fileName = null;
	private static OverheadExp experiment = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private int nThreads;
	private int threadId;
	private String algorithm = null;

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	private OverheadExp() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		String algorithm = xmlUtil.getAlgorithmConf();
		algorithm = algorithm.substring(0, algorithm.indexOf("_ALGORITHM"));
		expSetting = xmlUtil.getExpSetting();
		nThreads = expSetting.getnThreads();
		threadId = expSetting.getThreadId();
		String expType = expSetting.getType();
		String targetComp = expSetting.getTargetComp();

		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/";
		fileName = algorithm + "_" + expType + "_{" + nThreads + "}_" + ".csv";
		LOGGER.fine("result file:" + fileName);
		try {
			File file = new File(absolutePath + fileName);
			out = new PrintWriter(new FileWriter(file), true);
			out.write("#" + this + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static OverheadExp getInstance() {
		if (experiment == null) {
			synchronized (OverheadExp.class) {
				experiment = new OverheadExp();
			}
		}
		return experiment;
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
		return "NormalTotalExecTime(" + algorithm + ")";
	}
}
