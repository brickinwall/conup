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
	private int nThreads;
	private int threadId;
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
		nThreads = expSetting.getnThreads();
		threadId = expSetting.getThreadId();
		String expType = expSetting.getType();
		String targetComp = expSetting.getTargetComp();
		int rqstInterval = expSetting.getRqstInterval();

		absolutePath = tuscanyHomeLocation + "/samples/experiments-result/timeliness/";
		fileName = algorithm + "_" + strategy + "_" + expType + "_{" + nThreads + "_" + threadId + "}_" + rqstInterval + "_"
				+ targetComp + ".csv";
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
//			out.close();
		}
	}
	
	/*public void writeToFile(double updateTime) throws IOException {
		synchronized (timeliness) {
			LOGGER.info("updateTime:" + updateTime);
			String data = updateTime + "\n";
			out.write(data);
			out.flush();
		}
	}*/
	
	public void writeToFile(String data) throws IOException {
		synchronized (timeliness) {
			data = data + "\n";
			out.write(data);
			out.flush();
		}
	}
	

	/*public void addColumn(List<Double> updateTime) throws IOException {
		synchronized (timeliness) {
//			LOGGER.info("I'm writing: " + roundId + "," + updateTime);
			assert algorithm != null;
			
			BufferedReader bufReader = new BufferedReader(new FileReader(absolutePath + fileName));
			String lineStr = "";
			int rowNum = 0;
			StringBuffer nContent = new StringBuffer();
//			nContent.append(bufReader.readLine()).append("\r\n");
			
			while((lineStr = bufReader.readLine()) != null){
				String addValue = "";
				if(rowNum < updateTime.size()){
					addValue += updateTime.get(rowNum);
				}
				if(lineStr.endsWith(",")){
					nContent.append(lineStr).append("\"" + addValue + "\"");
				} else{
					nContent.append(lineStr).append(",\"" + addValue + "\"");
				}
				rowNum ++;
				nContent.append("\r\n");
			}
			bufReader.close();
			
			FileOutputStream fileOutStream = new FileOutputStream(new File(absolutePath + fileName), false);
			fileOutStream.write(nContent.toString().getBytes());
			fileOutStream.close();
			
//			String data = roundId + "," + updateTime + "\n"; 
//			out.write(data);
//			out.flush();
		}
	}*/

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
