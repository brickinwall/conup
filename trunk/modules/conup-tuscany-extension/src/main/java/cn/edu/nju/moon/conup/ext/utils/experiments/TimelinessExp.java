package cn.edu.nju.moon.conup.ext.utils.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;

public class TimelinessExp {
	private static final Logger LOGGER = Logger.getLogger(TimelinessExp.class.getName());
	
	private static String absolutePath = null;
	private static String fileName = null;
	private static TimelinessExp timeliness = null;
	private static PrintWriter out = null;
	private ExpSetting expSetting;
	private String algorithm = null;
	private String strategy = null;

	public ExpSetting getExpSetting() {
		return expSetting;
	}

	private TimelinessExp() {
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String tuscanyHomeLocation = xmlUtil.getTuscanyHome();
		algorithm = xmlUtil.getAlgorithmConf();
		algorithm = algorithm.substring(0, algorithm.indexOf("_ALGORITHM"));
		strategy = xmlUtil.getFreenessStrategy();
		strategy = strategy.substring(0, strategy.indexOf("_FOR_FREENESS"));
		expSetting = xmlUtil.getExpSetting();
		String targetComp = expSetting.getTargetComp();
		int rqstInterval = expSetting.getRqstInterval();

		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/timeliness/";
		fileName = algorithm + "_" + strategy + "_TimelinessExp_{" + targetComp
				+ "_" + rqstInterval + "}" + ".csv";
		LOGGER.fine("result file:" + fileName);
		try {
			File file = new File(absolutePath + fileName);
			out = new PrintWriter(new FileWriter(file), true);
			out.write("#" + this + "\n");
			out.flush();
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
	
	public void writeToFile(List<Double> updateTime) throws IOException {
		synchronized (timeliness) {
			for(int i = 0; i < updateTime.size(); i++){
				String data = updateTime.get(i) + "\n"; 
				out.write(data);
				out.flush();
			}
		}
	}
	
	public void writeToFile(String data) throws IOException {
		synchronized (timeliness) {
			data = data + "\n";
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
		return "OndemandTime, UpdateTime";
	}
}
