package cn.edu.nju.moon.conup.domain.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ScopeGeneratorImpl implements ScopeGenerator {
	private String scopeGraphUri = null;
	
	public ScopeGeneratorImpl(){
		String graphName = "scopeGraph.xml";
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "conup-domain-manager";
		String graphLocation = baseUri + File.separator + "src"
				+ File.separator + "main" + File.separator + "resources"
				+ File.separator;
		scopeGraphUri = graphLocation + graphName;
		
		createGraph();
	}
	
	private void createGraph() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n");
		buffer.append("<graph edgeDefault=\"directed\">" + "\n");
		buffer.append("<nodes />" + "\n");
		buffer.append("<edges />" + "\n");
		buffer.append("</graph>");

		// deleteCompositeFiles(compositeLocation);
		File file;
		try {
			file = new File(scopeGraphUri);
			FileWriter writer = new FileWriter(file);
			writer.write(buffer.toString());

			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Element generate(String componentName) {
		Queue<String>  queue= new LinkedBlockingQueue<String>();
		DomainConfigImpl domainConfig = new DomainConfigImpl();
		List<String> parentNodes = domainConfig.getSrcNodes(componentName);
		SAXBuilder sb = new SAXBuilder();
		Document doc = null;
		Element root = null;
		Element nodes = null;
		Element edges = null;
		try {
			doc = sb.build(scopeGraphUri);
			root = doc.getRootElement();
			nodes = root.getChild("nodes");
			edges = root.getChild("edges");
			Element newNode = new Element("node");
			newNode.setAttribute(new Attribute("id", componentName));
			nodes.addContent(newNode);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Iterator iterator = parentNodes.iterator();
		while(iterator.hasNext()){
			String parentCompName = (String) iterator.next();
			if(!nodeExist(nodes, parentCompName)){
				addNode(nodes, parentCompName);
				addEdge(edges, parentCompName, componentName);
				queue.add(parentCompName);
			}else if(nodeExist(nodes, parentCompName) && !edgeExist(edges, parentCompName, componentName)){
				addEdge(edges, parentCompName, componentName);
			}else{
//				do nothing
			}
		}
		
		while(!queue.isEmpty()){
			String compInQueue = queue.poll();
			List<String> parents = domainConfig.getSrcNodes(compInQueue);
			for(String com : parents){
				if(!nodeExist(nodes, com)){
					addNode(nodes, com);
					addEdge(edges, com, compInQueue);
					queue.add(com);
				}else if(nodeExist(nodes, com) && ! edgeExist(edges, com, compInQueue)){
					addEdge(edges, com, compInQueue);
				}else{
//					do nothing
				}
			}
		}
		
		Format format = Format.getPrettyFormat();
		XMLOutputter out = new XMLOutputter(format);
		try {
			out.output(doc, new FileOutputStream(new File(scopeGraphUri), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return root;
	}
	
	private void addNode(Element nodesElement, String nodeName){
		Element newNode = new Element("node");
		newNode.setAttribute(new Attribute("id", nodeName));
		nodesElement.addContent(newNode);
	}
	
	private void addEdge(Element edgesElement, String srcNode, String destName){
		Element newArc = new Element("edge");
		newArc.setAttribute(new Attribute("src", srcNode));
		newArc.setAttribute(new Attribute("dest", destName));
		edgesElement.addContent(newArc);
	}
	
	private boolean nodeExist(Element nodesElement, String nodeName){
		List nodeList = nodesElement.getChildren("node");
		Iterator iterator = nodeList.iterator();
		while(iterator.hasNext()){
			Element node = (Element) iterator.next();
			String name = node.getAttributeValue("id");
			if(name.equals(nodeName))
				return true;
		}
		return false;
	}
	
	private boolean edgeExist(Element edgesElement, String srcNode, String destNode){
		List edgeList = edgesElement.getChildren("edge");
		Iterator iterator = edgeList.iterator();
		while(iterator.hasNext()){
			Element edge = (Element) iterator.next();
			String src = edge.getAttributeValue("src");
			String dest = edge.getAttributeValue("dest");
			if(src.equals(srcNode) && dest.equals(destNode)){
				return true;
			}
		}
		return false;
	}

}
