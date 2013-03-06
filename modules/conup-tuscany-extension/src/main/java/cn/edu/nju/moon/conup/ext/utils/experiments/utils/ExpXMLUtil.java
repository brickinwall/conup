package cn.edu.nju.moon.conup.ext.utils.experiments.utils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.spi.exception.ConupEnvException;

/**
 * @author rgc
 * @version Nov 29, 2012 7:25:53 PM
 */
public class ExpXMLUtil {
	private String tuscanyHome;
	private String conupXmlPath;
	Element root = null;

	public ExpXMLUtil() {
		
		String disPath = getDistributionEnvPath();
		if(disPath != null){
			conupXmlPath = disPath + "/Conup.xml";
		}
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = sb.build(conupXmlPath);
			root = doc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(disPath == "" || disPath == null){
				throw new ConupEnvException("TUSCANY_HOME environment is not set!");
			} else{
				e.printStackTrace();
			}
		}
	}
	
	public ExpXMLUtil(String xmlPath) {
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = sb.build(xmlPath);
			root = doc.getRootElement();
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
		Element experiment = root.getChild("experiment");
		Element setting = experiment.getChild("setting");
		
		Element indepRun = setting.getChild("indepRun");
		Element nThreads = setting.getChild("nThreads");
		Element threadId = setting.getChild("threadId");
		Element targetComp = setting.getChild("targetComp");
		Element ipAddress = setting.getChild("ipAddress");
		Element rqstInterval = setting.getChild("rqstInterval");
		Element baseDir = setting.getChild("baseDir");
		Element type = setting.getChild("type");
		
		expSetting.setIndepRun(Integer.parseInt(indepRun.getValue()));
		expSetting.setnThreads(Integer.parseInt(nThreads.getValue()));
		expSetting.setThreadId(Integer.parseInt(threadId.getValue()));
		expSetting.setTargetComp(targetComp.getValue());
		expSetting.setIpAddress(ipAddress.getValue());
		expSetting.setRqstInterval(Integer.parseInt(rqstInterval.getValue()));
		expSetting.setBaseDir(baseDir.getValue());
		expSetting.setType(type.getValue());
		
		return expSetting;
	}
	
	public String getAlgorithmConf() {
		Element configuration = root.getChild("configuration");
		Element algorithm = configuration.getChild("algorithm");
		return algorithm.getValue();
	}

	public String getFreenessStrategy() {
		Element configuration = root.getChild("configuration");
		Element freenessStrategy = configuration.getChild("freenessStrategy");
		return freenessStrategy.getValue();
	}

	public Set<String> getParents(String compIdentifier) {
		Set<String> parentComp = new HashSet<String>();
		try {
			Element staticDeps = root.getChild("staticDeps");
			List compList = staticDeps.getChildren("component");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String compName = comp.getAttributeValue("name");
				if (compName.equals(compIdentifier)) {
					List parentList = comp.getChildren("parent");
					Iterator parentListIter = parentList.iterator();
					while (parentListIter.hasNext()) {
						parentComp.add(((Element) parentListIter.next())
								.getValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parentComp;
	}

	public Set<String> getChildren(String compIdentifier) {
//		Set<String> childrenComps = new HashSet<String>();
		Set<String> childrenComps = new ConcurrentSkipListSet<String>();
		try {
			Element staticDeps = root.getChild("staticDeps");
			List compList = staticDeps.getChildren("component");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String compName = comp.getAttributeValue("name");
				if (compName.equals(compIdentifier)) {
					List childrenList = comp.getChildren("child");
					Iterator childListIter = childrenList.iterator();
					while (childListIter.hasNext()) {
						childrenComps.add(((Element) childListIter.next())
								.getValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return childrenComps;
	}
	
	public Set<String> getAllComponents(){
		Set<String> allComps = new HashSet<String>();
		
		try {
			Element staticDeps = root.getChild("staticDeps");
			List compList = staticDeps.getChildren("component");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String compName = comp.getAttributeValue("name");
				allComps.add(compName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allComps;
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
