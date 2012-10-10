package cn.edu.nju.moon.conup.domain.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class NodesGraphGeneratorImpl implements NodesGraphGenerator {
	private static NodesGraphGeneratorImpl nodesGraphGenerator = new NodesGraphGeneratorImpl();
	private String graphUri;
	
	public String getGraphUri() {
		return graphUri;
	}

	public void setGraphUri(String graphUri) {
		this.graphUri = graphUri;
	}

	private static String XML_Declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private NodesGraphGeneratorImpl() {
		String graphName = "graph.xml";
		String baseUri = new File("").getAbsolutePath();
		baseUri = baseUri.substring(0, baseUri.lastIndexOf(File.separator))
				+ File.separator + "conup-domain-manager";
		String graphLocation = baseUri + File.separator + "src"
				+ File.separator + "main" + File.separator + "resources"
				+ File.separator;
		graphUri = graphLocation + graphName;
	}
	
	@Override
	public boolean addNode(String componentName, String bindingIpAndPort) {
		if(!isExist())
			return false;
		
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(graphUri);
			Element root = doc.getRootElement();
			/**
			 * test whether if component is already in graph, if not add to graph.
			 * else not add to it
			 */
			Element nodes = root.getChild("nodes");
			List nodeList = nodes.getChildren("node");
			Iterator iterator = nodeList.iterator();
			while (iterator.hasNext()) {
			Element element = (Element) iterator.next();
			String nodeName = element.getAttributeValue("id");
			if (nodeName.equals(componentName))
				return false;
			}
			Element newNode = new Element("node");
			newNode.setAttribute(new Attribute("id", componentName));
			newNode.setAttribute("bindingInfo", bindingIpAndPort);
			nodes.addContent(newNode);
			Format format = Format.getPrettyFormat();
			// format.setLineSeparator("\r\n");
			XMLOutputter out = new XMLOutputter(format);
			out.output(doc, new FileOutputStream(new File(graphUri), false));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean addEdge(String srcComponent, String destComponent) {
		if(!isExist())
			return false;
		
		SAXBuilder sb = new SAXBuilder();
		Document doc;
		try {
			doc = sb.build(graphUri);
			Element root = doc.getRootElement();
			/**
			 * check whether there is a arc from src to dst
			 * if not, add a new edge.
			 * else do nothing
			 */
			Element edges = root.getChild("edges");
			List edgeList = edges.getChildren("edge");
			Iterator iterator = edgeList.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				String src = element.getAttributeValue("src");
				String dest = element.getAttributeValue("dest");
				if (src.equals(srcComponent) && dest.equals(destComponent))
					return false;
			}
			Element newArc = new Element("edge");
			newArc.setAttribute(new Attribute("src", srcComponent));
			newArc.setAttribute(new Attribute("dest", destComponent));
			edges.addContent(newArc);
			Format format = Format.getPrettyFormat();
			// format.setLineSeparator("\r\n");
			XMLOutputter out = new XMLOutputter(format);
			out.output(doc, new FileOutputStream(new File(graphUri), false));
			
//			List list = root.getChildren("edge");
//			Iterator iterator = list.iterator();
//			while (iterator.hasNext()) {
//				Element element = (Element) iterator.next();
//				String src = element.getAttributeValue("src");
//				String dest = element.getAttributeValue("dest");
//				if (src.equals(srcComponent) && dest.equals(destComponent))
//					return false;
//			}
//			Element newArc = new Element("edge");
//			newArc.setAttribute(new Attribute("src", srcComponent));
//			newArc.setAttribute(new Attribute("dest", destComponent));
//			root.addContent(newArc);
//			Format format = Format.getPrettyFormat();
//			// format.setLineSeparator("\r\n");
//			XMLOutputter out = new XMLOutputter(format);
//			out.output(doc, new FileOutputStream(new File(graphUri), false));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public void createGraph() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(XML_Declaration + "\n");
		buffer.append("<graph edgeDefault=\"directed\">" + "\n");
		buffer.append("<nodes />" + "\n");
		buffer.append("<edges />" + "\n");
		buffer.append("</graph>");

		// deleteCompositeFiles(compositeLocation);
		File file;
		try {
			file = new File(graphUri);
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
	
	public boolean isExist(){
		File graphFile = new File(graphUri);
		if(graphFile.exists())
			return true;
		else
			return false;
	}

	public static NodesGraphGeneratorImpl getInstance() {
		return nodesGraphGenerator;
	}
}
