package cn.edu.nju.moon.conup.domain.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cn.edu.nju.moon.conup.domain.def.StaticEdge;

public class DomainConfigImpl implements DomainConfig {
	private static String graphPath = NodesGraphGeneratorImpl.getInstance().getGraphUri();
	@Override
	public List<String> getAllNodes() {
		List<String> nodesList = new ArrayList<String>();
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(graphPath);
			Element root = doc.getRootElement();
			Element nodes = root.getChild("nodes");
			List nodeList = nodes.getChildren("node");
			Iterator iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				String nodeName = element.getAttributeValue("id");
				nodesList.add(nodeName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodesList;
	}

	@Override
	public List<StaticEdge> getAllEdges() {
		List<StaticEdge> staticEdges = new ArrayList<StaticEdge>();
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(graphPath);
			Element root = doc.getRootElement();
			Element edges = root.getChild("edges");
			List nodeList = edges.getChildren("edge");
			Iterator iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				String srcComponent = element.getAttributeValue("src");
				String targetComponent = element.getAttributeValue("dest");
				StaticEdge newEdge = new StaticEdge(srcComponent, targetComponent);
				staticEdges.add(newEdge);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return staticEdges;
	}

	@Override
	public List<String> getDestNodes(String srcNode) {
		List<String> destNodesList = new ArrayList<String>();
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(graphPath);
			Element root = doc.getRootElement();
			Element edges = root.getChild("edges");
			List nodeList = edges.getChildren("edge");
			Iterator iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				String srcComponent = element.getAttributeValue("src");
				if(srcComponent.equals(srcNode)){
					destNodesList.add(element.getAttributeValue("dest"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return destNodesList;
	}

	@Override
	public List<String> getSrcNodes(String destNode) {
		List<String> srcNodesList = new ArrayList<String>();
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(graphPath);
			Element root = doc.getRootElement();
			Element edges = root.getChild("edges");
			List nodeList = edges.getChildren("edge");
			Iterator iterator = nodeList.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				String destComponent = element.getAttributeValue("dest");
				if(destComponent.equals(destNode)){
					srcNodesList.add(element.getAttributeValue("src"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return srcNodesList;
	}

}
