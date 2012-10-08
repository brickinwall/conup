package cn.edu.nju.moon.conup.domain.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class CompositeAnalyzerImpl implements CompositeAnalyzer {
	private NodesGraphGeneratorImpl graphGenerator = NodesGraphGeneratorImpl.getInstance();
	private static CompositeAnalyzerImpl compositeAnalyser = new CompositeAnalyzerImpl();
	
	@Override
	public void analyze(String filePath) {
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(filePath);
			Element root = doc.getRootElement();
			Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
			Element component = root.getChild("component", ns);
			String currentComponentName = component.getAttributeValue("name");
			if(!graphGenerator.isExist())
				graphGenerator.createGraph();
		
			
			//get component service binding ip and port
			List serviceList = component.getChildren("service", ns);
			Iterator serviceIterator = serviceList.iterator();
			String ipAndPort = null;
			while(serviceIterator.hasNext()){
				Element service = (Element) serviceIterator.next();
				List binding = service.getChildren();
				Iterator bindingIterator = binding.iterator();
				while(bindingIterator.hasNext()){
					Element specificBinding = (Element) bindingIterator.next();
					String bindingUri = specificBinding.getAttributeValue("uri");
					int protocolIndex = bindingUri.indexOf("http://");
					String subStr = bindingUri.substring(protocolIndex + 7);
					int slashIndex = subStr.indexOf("/");
					ipAndPort = subStr.substring(0, slashIndex);
					break;
				}
				if(ipAndPort != null)
					break;
			}
			
			graphGenerator.addNode(currentComponentName, ipAndPort);
			
			
			List referenceList = component.getChildren("reference",ns);
			Iterator iterator = referenceList.iterator();
			while (iterator.hasNext()) {
				Element reference = (Element) iterator.next();
				List binding = reference.getChildren();
				Iterator bindingIterator = binding.iterator();
				while (bindingIterator.hasNext()) {
					Element specificBinding = (Element) bindingIterator.next();
					String bindingUri = specificBinding.getAttributeValue("uri");
					int lastIndex = bindingUri.lastIndexOf("/");
					String subStr = bindingUri.substring(0, lastIndex);
					lastIndex = subStr.lastIndexOf("/");
					String targetComponentName = subStr.substring(lastIndex + 1, subStr.length());
					graphGenerator.addEdge(currentComponentName,targetComponentName);
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static CompositeAnalyzerImpl getInstance(){
		return compositeAnalyser;
	}

}
