package cn.edu.nju.moon.conup.domain.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuscany.sca.Node;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.communication.services.OndemandService;
import cn.edu.nju.moon.conup.def.Scope;
import cn.edu.nju.moon.conup.domain.generator.NodesGraphGeneratorImpl;
import cn.edu.nju.moon.conup.domain.generator.ScopeGeneratorImpl;
import cn.edu.nju.moon.conup.domain.launcher.LaunchDomainManager;



@Service({FreenessService.class, TransactionIDService.class, StaticConfigService.class, DomainComponentUpdateService.class })
public class DomainManagerServiceImpl implements FreenessService, TransactionIDService, StaticConfigService, DomainComponentUpdateService{
	private static String graphPath = NodesGraphGeneratorImpl.getInstance().getGraphUri();
	public String createID() {
		String result = null;
		//take current time in milliseconds as transaction id
		result = Long.toString(System.currentTimeMillis());
		//add transaction id to the LaunchDomainManager.createdTransactions
		LaunchDomainManager.createdTransactions.add(result);
		
		return result;
	}

	public boolean removeID(String id) {
		if(LaunchDomainManager.createdTransactions.contains(id))
			return LaunchDomainManager.createdTransactions.remove(id);
		return false;
	}

	@Override
	public boolean isFreeness(String componentName) {
		String endPoint = componentName + "Comm#service-binding(FreenessService/FreenessService)";
		Node communicationNode = LaunchDomainManager.node;
		FreenessService freenessService;
		try {
			freenessService = communicationNode.getService(FreenessService.class, endPoint);
			return freenessService.isFreeness(componentName);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
		return false;
	}

//	@Override
	public boolean isFreeness(String[] componentNames) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getParentComponents(String target) {
		Set<String> result = new HashSet<String>();
		
		
//		//FOR TEST
//		if(target.equals("AuthComponent")){
//			result.add("ProcComponent");
//			result.add("PortalComponent");
//		} else if(target.equals("ProcComponent")){
//			result.add("PortalComponent");
//		} else if(target.equals("PortalComponent")){
//			
//		}		
//		return result;
		
		Set<String> parentComponents = new HashSet<String>();
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
				if(destComponent.equals(target)){
					parentComponents.add(element.getAttributeValue("src"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return parentComponents;
	}

	@Override
	public Set<String> getSubComponents(String target) {
		Set<String> result = new HashSet<String>();
		
		
//		//FOR TEST
//		if (target.equals("AuthComponent")) {
//			
//		} else if (target.equals("ProcComponent")) {
//			result.add("AuthComponent");
//		} else if (target.equals("PortalComponent")) {
//			result.add("AuthComponent");
//			result.add("ProcComponent");
//		}
//		
//		return result;
		
		Set<String> subComponents = new HashSet<String>();
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
				if(srcComponent.equals(target)){
					subComponents.add(element.getAttributeValue("dest"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return subComponents;
	}

	@Override
	public Scope getScope(String target) {

		//FOR TEST
		Scope scope = new Scope();
		scope.getTargetComponents().add(target);
		return scope;
		
//		Scope scope = new Scope();
//		ScopeGeneratorImpl sg = new ScopeGeneratorImpl();
//		scope.setScope(sg.generate(target));
//		
//		return scope;
	}

	@Override
	public boolean onDemandRequest(String targetComponent, String freenessSetup) {
		System.out.println("Update required: target=" + targetComponent + 
				", freenessSetup=" + freenessSetup);
		String endPoint = targetComponent + "Comm#service-binding(OndemandService/OndemandService)";
		Node communicationNode = LaunchDomainManager.node;
		OndemandService ondemandService;
		try {
			ondemandService = communicationNode.getService(OndemandService.class, endPoint);
			return ondemandService.reqOndemandSetup(targetComponent, 
					targetComponent, getScope(targetComponent), freenessSetup);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
		
//		String endPoint = targetComponent + "Comm#service-binding(ComponentUpdateService/ComponentUpdateService)";
//		System.out.println("In domain manager, invoke " + endPoint);
//		Node communicationNode = LaunchDomainManager.node;
//		ComponentUpdateService updateService;
//		try {
//			updateService = communicationNode.getService(ComponentUpdateService.class, endPoint);
//			return updateService.update();
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
//		}
		return true;
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createGraph() {
		NodesGraphGeneratorImpl nodesGraphGenerator = NodesGraphGeneratorImpl.getInstance();
		if(!nodesGraphGenerator.isExist())
			nodesGraphGenerator.createGraph();
	}

	@Override
	public boolean addNodes(Map<String, String> nodesInfo) {
		NodesGraphGeneratorImpl nodesGraphGenerator = NodesGraphGeneratorImpl.getInstance();
		Iterator iterator = nodesInfo.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry entry = (Map.Entry) iterator.next(); 
			String componentName = (String) entry.getKey();
			String ipAndPortInfo = (String) entry.getValue();
			nodesGraphGenerator.addNode(componentName, ipAndPortInfo);
		}
		
		return true;
	}

	@Override
	public boolean addEdges(Map<String, Set<String>> edgesInfo) {
		NodesGraphGeneratorImpl nodesGraphGenerator = NodesGraphGeneratorImpl.getInstance();
		Iterator iterator = edgesInfo.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry entry = (Map.Entry) iterator.next(); 
			String srcComponent = (String) entry.getKey();
			Set<String> destComponents = (Set<String>) entry.getValue();
			Iterator destIterator = destComponents.iterator();
			while(destIterator.hasNext()){
				String destComponent = (String) destIterator.next();
				nodesGraphGenerator.addEdge(srcComponent, destComponent);
			}
		}
		return true;
	}

//	@Override
//	public String status(String targetComponent) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
