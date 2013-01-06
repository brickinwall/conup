package cn.edu.nju.moon.conup.spi.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author rgc
 * @version Nov 29, 2012 7:25:53 PM
 */
public class XMLUtil {
//	private String conupXmlPath = "/home/rgc/Documents/conup/tags/conup-2.1-DU/distribution/tuscany-sca-2.1-DU/bin/Conup.xml";
//	private String conupXmlPath = "src/main/resources/Conup.xml";
	Element root = null;

	public XMLUtil() {
		//development version
		String xmlUtilLocation = XMLUtil.class.getResource("").toString();
//		System.out.println("xmlUtilLocation:" + xmlUtilLocation);
		int beginIndex = xmlUtilLocation.indexOf(":") + 1;
		int endIndex = xmlUtilLocation.indexOf("cn/edu/nju/moon/conup/spi/utils/");
		String conupXmlPath = xmlUtilLocation.substring(beginIndex, endIndex) + "Conup.xml";
//		System.out.println("conupXmlPath:" + conupXmlPath);
		
		//distribution version
//		String disPath = getDistributionEnvPath();
//		if(disPath != null){
//			conupXmlPath = disPath + "/Conup.xml";
//		}
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = sb.build(conupXmlPath);
			root = doc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		Set<String> childrenComps = new HashSet<String>();
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
			if(key.equals("PATH") && OS.contains("Linux")){
				String path = map.get(key);
				String[] paths = path.split(":");
				for(String str : paths){
					if(str.contains("tuscany")){
						disPath = str;
						break;
					}
				}
				break;
			}
			if(key.equals("Path") && OS.contains("Windows")){
				String path = map.get(key);
				String[] paths = path.split(";");
				for(String str : paths){
					if(str.contains("tuscany")){
						disPath = str;
						break;
					}
				}
				break;
			}
		}
		return disPath;
	}

}
