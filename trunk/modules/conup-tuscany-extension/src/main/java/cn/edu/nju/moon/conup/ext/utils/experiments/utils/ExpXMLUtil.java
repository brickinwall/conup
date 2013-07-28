package cn.edu.nju.moon.conup.ext.utils.experiments.utils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.spi.exception.ConupException;

/**
 * @author rgc
 * @version Nov 29, 2012 7:25:53 PM
 */
public class ExpXMLUtil {
	private String tuscanyHome;
	private String conupXmlPath;
	private String expXmlPath = null;
	
	Element conupRoot = null;
	Document conupDoc = null;
	Element expRoot = null;
	Document expDoc = null;

	public ExpXMLUtil() {
		
		String disPath = getDistributionEnvPath();
		if (disPath != null) {
			conupXmlPath = disPath + "/Conup.xml";
			expXmlPath = disPath + "/ExpSetting.xml";
		}
		SAXBuilder sb = new SAXBuilder();
		try {
			conupDoc = sb.build(conupXmlPath);
			conupRoot = conupDoc.getRootElement();
			expDoc = sb.build(expXmlPath);
			expRoot = expDoc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (disPath == "" || disPath == null) {
				throw new RuntimeException(
						"TUSCANY_HOME environment is not set!");
			} else {
				e.printStackTrace();
			}
		}
	}
	
	public ExpXMLUtil(String xmlPath) {
		SAXBuilder sb = new SAXBuilder();
		try {
			conupDoc = sb.build(xmlPath + "Conup.xml");
			conupRoot = conupDoc.getRootElement();
			expDoc = sb.build(xmlPath + "ExpSetting.xml");
			expRoot = expDoc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getTuscanyHome() {
		return tuscanyHome;
	}

	public ExpSetting getExpSetting(){
		ExpSetting expSetting = new ExpSetting();
//		Element experiment = root.getChild("experiment");
		List settings = expRoot.getChildren("setting");
		Iterator settingIter = settings.iterator();
		Element setting = null;
		while(settingIter.hasNext()){
			Element tmp = (Element) settingIter.next();
			if(tmp.getAttributeValue("enable").equals("yes")){
				setting = tmp;
				break;
			}
		}
		
		assert setting != null;
		
		Element indepRun = setting.getChild("indepRun");
		Element nThreads = setting.getChild("nThreads");
		Element threadId = setting.getChild("threadId");
		Element targetComp = setting.getChild("targetComp");
		Element ipAddress = setting.getChild("ipAddress");
		Element rqstInterval = setting.getChild("rqstInterval");
		Element baseDir = setting.getChild("baseDir");
		Element type = setting.getChild("type");
		
		expSetting.setIndepRun(Integer.parseInt(indepRun.getValue().trim()));
		expSetting.setnThreads(Integer.parseInt(nThreads.getValue().trim()));
		expSetting.setThreadId(Integer.parseInt(threadId.getValue().trim()));
		expSetting.setTargetComp(targetComp.getValue().trim());
		expSetting.setIpAddress(ipAddress.getValue().trim());
		expSetting.setRqstInterval(Integer.parseInt(rqstInterval.getValue().trim()));
		expSetting.setBaseDir(baseDir.getValue().trim());
		expSetting.setType(type.getValue().trim());
		
		return expSetting;
	}
	
	public String getAlgorithmConf() {
		Element configuration = conupRoot.getChild("configuration");
		Element algorithms = configuration.getChild("algorithms");
		List allALgs = algorithms.getChildren();
		Iterator algsIter = allALgs.iterator();
		String algConf = null;
		while(algsIter.hasNext()){
			Element alg = (Element) algsIter.next();
			if(alg.getAttributeValue("enable").equals("yes")){
				algConf =  alg.getValue();
				break;
			}
		}
		
		assert algConf != null;
		return algConf;
	}

	public String getFreenessStrategy() {
		Element configuration = conupRoot.getChild("configuration");
		Element freenessStrategies = configuration.getChild("freenessStrategies");
		List allStrategies = freenessStrategies.getChildren();
		Iterator strategiesIter = allStrategies.iterator();
		String strategyConf = null;
		while(strategiesIter.hasNext()){
			Element strategy = (Element) strategiesIter.next();
			if(strategy.getAttributeValue("enable").equals("yes")){
				strategyConf =  strategy.getValue();
				break;
			}
		}
		
		assert strategyConf != null;
		return strategyConf;
	}

	/**
	 * GET TUSCANY_HOME'S PHYSICAL LOCATION
	 * @return
	 */
	public String getDistributionEnvPath(){
		Properties prpt = System.getProperties();
		Enumeration<?> enm = prpt.propertyNames(); 
		String OS = null;
		while (enm.hasMoreElements()) {
			String key = (String) enm.nextElement();
			if(key.equals("os.name")){
				OS = System.getProperty(key, "undefined");
				break;
			}
		}
		
		String disPath = "";
		Map<String, String> map = System.getenv(); 
		Set<String> set = map.keySet(); 
		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if(key.equals("TUSCANY_HOME")){
				String path = map.get(key);
				tuscanyHome = path;
				disPath = path + "/bin";
				break;
			}
		}
		return disPath;
	}

}
