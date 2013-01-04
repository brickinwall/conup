package cn.edu.nju.moon.conup.communication.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuscany.sca.Node;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cn.edu.nju.moon.conup.communication.services.VcService;
import cn.edu.nju.moon.conup.communication.services.VcServiceImpl;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.domain.services.StaticConfigService;


public class CompositeAnalyzerImpl implements CompositeAnalyzer {
//	private NodesGraphGeneratorImpl graphGenerator = NodesGraphGeneratorImpl.getInstance();
	private static CompositeAnalyzerImpl compositeAnalyser = new CompositeAnalyzerImpl();
	
	@Override
	public void analyze(String filePath) {
		Node node = VcContainerImpl.getInstance().getCommunicationNode();
		StaticConfigService staticConfig =null;
		Map<String, String> nodesInfo = new HashMap<String, String>();
		Map<String, Set<String>> edgesInfo = new HashMap<String, Set<String>>();
		
		try {
			staticConfig = node.getService(StaticConfigService.class, "DomainManagerComponent#service-binding(StaticConfigService/StaticConfigService)");
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(filePath);
			Element root = doc.getRootElement();
			Namespace ns = Namespace.getNamespace("http://docs.oasis-open.org/ns/opencsa/sca/200912");
			Element component = root.getChild("component", ns);
			String currentComponentName = component.getAttributeValue("name");
//			if(!graphGenerator.isExist())
//				graphGenerator.createGraph();
			staticConfig.createGraph();
			
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
			nodesInfo.put(currentComponentName, ipAndPort);
//			graphGenerator.addNode(currentComponentName, ipAndPort);
			
			
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
//					graphGenerator.addEdge(currentComponentName,targetComponentName);
					if(edgesInfo.containsKey(currentComponentName)){
						edgesInfo.get(currentComponentName).add(targetComponentName);
					}else{
						Set<String> refComp = new HashSet<String>();
						refComp.add(targetComponentName);
						edgesInfo.put(currentComponentName, refComp);
					}
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		staticConfig.addNodes(nodesInfo);
		staticConfig.addEdges(edgesInfo);
	}
	
	public static CompositeAnalyzerImpl getInstance(){
		return compositeAnalyser;
	}

}
