package cn.edu.nju.moon.conup.ext.utils.experiments.utils;

import java.io.IOException;
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

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;

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

	@SuppressWarnings("rawtypes")
	public ExpSetting getExpSetting() {
		ExpSetting expSetting = new ExpSetting();
		// Element experiment = root.getChild("experiment");
		List settings = expRoot.getChildren("setting");
		Iterator settingIter = settings.iterator();
		Element setting = null;
		while (settingIter.hasNext()) {
			Element tmp = (Element) settingIter.next();
			if (tmp.getAttributeValue("enable").equals("yes")) {
				setting = tmp;
				break;
			}
		}

		assert setting != null;

		Element indepRun = setting.getChild("indepRun");
		Element targetComp = setting.getChild("targetComp");
		Element ipAddress = setting.getChild("ipAddress");
		Element rqstInterval = setting.getChild("rqstInterval");
		Element baseDir = setting.getChild("baseDir");
		Element scopeElement = setting.getChild("scope");

		expSetting.setIndepRun(Integer.parseInt(indepRun.getValue().trim()));
		expSetting.setTargetComp(targetComp.getValue().trim());
		expSetting.setIpAddress(ipAddress.getValue().trim());
		expSetting.setRqstInterval(Integer.parseInt(rqstInterval.getValue()
				.trim()));
		expSetting.setBaseDir(baseDir.getValue().trim());

		if (scopeElement == null) {
			expSetting.setScope(null);
		} else {
			Scope scope = new Scope();
			List compsInScope = scopeElement.getChildren("component");
			Iterator compsInScopeIter = compsInScope.iterator();
			while (compsInScopeIter.hasNext()) {
				Element comp = (Element) compsInScopeIter.next();
				String compName = comp.getAttributeValue("name");
				Set<String> parentComps = new HashSet<String>();
				Set<String> subComps = new HashSet<String>();
				
				List subCompsList = comp.getChildren("child");
				Iterator iter = subCompsList.iterator();
				while(iter.hasNext()){
					Element tmp = (Element)iter.next();
					subComps.add(tmp.getValue().trim());
				}
				
				List parentCompsList = comp.getChildren("parent");
				iter = parentCompsList.iterator();
				while(iter.hasNext()){
					Element tmp = (Element)iter.next();
					parentComps.add(tmp.getValue().trim());
				}
				scope.addComponent(compName, parentComps, subComps);
			}
			Set<String> targetComps = new HashSet<String>();
			List targetCompsList = scopeElement.getChildren("targetComps");
			Element targets = null;
			while(targetCompsList.iterator().hasNext()){
				targets = (Element)targetCompsList.iterator().next();
				break;
			}
			if(targets != null){
				List targetsInScope = targets.getChildren("target");
				Iterator iter = targetsInScope.iterator();
				while(iter.hasNext()){
					Element tmp = (Element)iter.next();
					targetComps.add(tmp.getValue().trim());
				}
			}
			scope.setTarget(targetComps);
			scope.setSpecifiedScope(true);
			expSetting.setScope(scope);
		}

		return expSetting;
	}

	@SuppressWarnings("rawtypes")
	public String getAlgorithmConf() {
		Element configuration = conupRoot.getChild("configuration");
		Element algorithms = configuration.getChild("algorithms");
		List allALgs = algorithms.getChildren();
		Iterator algsIter = allALgs.iterator();
		String algConf = null;
		while (algsIter.hasNext()) {
			Element alg = (Element) algsIter.next();
			if (alg.getAttributeValue("enable").equals("yes")) {
				algConf = alg.getValue();
				break;
			}
		}

		assert algConf != null;
		return algConf;
	}

	@SuppressWarnings("rawtypes")
	public String getFreenessStrategy() {
		Element configuration = conupRoot.getChild("configuration");
		Element freenessStrategies = configuration
				.getChild("freenessStrategies");
		List allStrategies = freenessStrategies.getChildren();
		Iterator strategiesIter = allStrategies.iterator();
		String strategyConf = null;
		while (strategiesIter.hasNext()) {
			Element strategy = (Element) strategiesIter.next();
			if (strategy.getAttributeValue("enable").equals("yes")) {
				strategyConf = strategy.getValue();
				break;
			}
		}

		assert strategyConf != null;
		return strategyConf;
	}

	/**
	 * GET TUSCANY_HOME'S PHYSICAL LOCATION
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public String getDistributionEnvPath() {
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
				tuscanyHome = path;
				disPath = path + "/bin";
				break;
			}
		}
		return disPath;
	}

}
