package cn.edu.nju.moon.conup.sample.configuration.util;

import java.io.IOException;
import java.rmi.ConnectException;
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

import cn.edu.nju.moon.conup.sample.configuration.model.TargetComp;
import cn.edu.nju.moon.conup.spi.exception.ConupEnvException;

/**
 * 
 * @author rgc
 * 
 */
public class UpdateXmlUtil {
	private String conupXmlPath = "src/main/resources/TargetComps.xml";
	Element root = null;

	public UpdateXmlUtil() {

		String disPath = getDistributionEnvPath();
		if (disPath != null) {
			conupXmlPath = disPath + "/TargetComps.xml";
		}
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = sb.build(conupXmlPath);
			root = doc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (disPath == "" || disPath == null) {
				throw new ConupEnvException(
						"TUSCANY_HOME environment is not set!");
			} else {
				e.printStackTrace();
			}
		}
	}

	public UpdateXmlUtil(String xmlPath) {
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

	public TargetComp getTargetComp() throws ConnectException {
		TargetComp result = null;
		Set<TargetComp> allComps = getAllComponents();
		Iterator<TargetComp> iter = allComps.iterator();
		while(iter.hasNext()){
			TargetComp targetComp = iter.next();
			if(targetComp.isTarget()){
				result = targetComp;
				break;
			}
		}
		
		if(result == null){
			throw new ConnectException("Error in Update.xml, please check target component configuration.");
		}
		return result;
	}

	public Set<TargetComp> getAllComponents() {
		Set<TargetComp> allComps = new HashSet<TargetComp>();

		try {
			List compList = root.getChildren("comp");
			Iterator iterator = compList.iterator();
			while (iterator.hasNext()) {
				Element comp = (Element) iterator.next();
				String isTargetComp = comp.getAttributeValue("enabled").trim();
				boolean targetCompFlag = false;
				if(isTargetComp.equals("yes"))
					targetCompFlag = true;
				String ipAddress = comp.getChild("ipAddress").getValue().trim();
				int port = Integer.parseInt(comp.getChild("port").getValue().trim());
				String targetCompIdentifier = comp.getChild("targetComp").getValue().trim();
				String contributionUri = comp.getChild("contributionUri").getValue().trim();
				String compsiteUri = comp.getChild("compsiteUri").getValue().trim();
				String compImpl = comp.getChild("compImpl").getValue().trim();
				String baseDir = comp.getChild("baseDir").getValue().trim();
				TargetComp targetComp = new TargetComp(targetCompFlag, ipAddress, port,
						targetCompIdentifier, contributionUri, compsiteUri,
						compImpl, baseDir);

				allComps.add(targetComp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allComps;
	}

	private String getDistributionEnvPath() {
		Properties prpt = System.getProperties();
		Enumeration<?> enm = prpt.propertyNames();
		String OS = null;
		while (enm.hasMoreElements()) {
			String key = (String) enm.nextElement();
			if (key.equals("os.name")) {
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
			if (key.equals("TUSCANY_HOME")) {
				String path = map.get(key);
				disPath = path + "/bin";
				break;
			}
		}
		return disPath;
	}
}
