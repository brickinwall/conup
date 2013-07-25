package cn.edu.nju.moon.conup.spi.utils;

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

import cn.edu.nju.moon.conup.spi.exception.ConupException;

/**
 * @author rgc
 * @version Nov 29, 2012 7:25:53 PM
 */
public class XMLUtil {
	private String conupXmlPath = "src/main/resources/Conup.xml";
	Element root = null;

	public XMLUtil() {
		
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
				throw new ConupException("TUSCANY_HOME environment is not set!");
			} else{
				e.printStackTrace();
			}
		}
	}
	
	public XMLUtil(String xmlPath) {
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

	public String getAlgorithmConf() {
		Element configuration = root.getChild("configuration");
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
		Element configuration = root.getChild("configuration");
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

	public Set<String> getParents(String compIdentifier) {
		Set<String> parentComp = new HashSet<String>();
		try {
			Element staticDeps = root.getChild("staticDeps");
			List compList = staticDeps.getChildren("component");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String compName = comp.getAttributeValue("name").trim();
				if (compName.equals(compIdentifier)) {
					List parentList = comp.getChildren("parent");
					Iterator parentListIter = parentList.iterator();
					while (parentListIter.hasNext()) {
						parentComp.add(((Element) parentListIter.next()).getValue().trim());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parentComp;
	}

	public Set<String> getChildren(String compIdentifier) {
		Set<String> childrenComps = new ConcurrentSkipListSet<String>();
		try {
			Element staticDeps = root.getChild("staticDeps");
			List compList = staticDeps.getChildren("component");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String compName = comp.getAttributeValue("name").trim();
				if (compName.equals(compIdentifier)) {
					List childrenList = comp.getChildren("child");
					Iterator childListIter = childrenList.iterator();
					while (childListIter.hasNext()) {
						childrenComps.add(((Element) childListIter.next()).getValue().trim());
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
				String compName = comp.getAttributeValue("name").trim();
				allComps.add(compName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allComps;
	}
	
	private String getDistributionEnvPath(){
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
				disPath = path + "/bin";
				break;
			}
//			if(key.equals("PATH") && OS.contains("Linux")){
//				String path = map.get(key);
//				String[] paths = path.split(":");
//				for(String str : paths){
//					if(str.contains("conup")){
//						disPath = str;
//						break;
//					}
//				}
//				break;
//			}
//			if(key.equals("Path") && OS.contains("Windows")){
//				String path = map.get(key);
//				String[] paths = path.split(";");
//				for(String str : paths){
//					if(str.contains("conup")){
//						disPath = str;
//						break;
//					}
//				}
//				break;
//			}
		}
		return disPath;
	}

}
