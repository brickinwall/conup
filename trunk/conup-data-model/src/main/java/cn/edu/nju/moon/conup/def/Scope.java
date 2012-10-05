package cn.edu.nju.moon.conup.def;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class Scope {
	
	private Element scope = null;
	private Set<String> parentComponents;
	private Set<String> subComponents;
	
//	public Element getScope() {
//		return scope;
//	}
//
//	public void setScope(Element scope) {
//		this.scope = scope;
//	}
//	
//	public Set<String> getParentComponents(String componentName){
//		Set<String> parentComponents = new HashSet<String>();
//		if(scope != null){
//			Element edges = scope.getChild("edges");
//			List edgeList = edges.getChildren("edge");
//			Iterator edgeListIterator = edgeList.iterator();
//			while(edgeListIterator.hasNext()){
//				Element element = (Element) edgeListIterator.next();
//				String destComponent = element.getAttributeValue("dest");
//				if(destComponent.equals(componentName)){
//					String srcComponent = element.getAttributeValue("src");
//					parentComponents.add(srcComponent);
//				}
//			}
//			return parentComponents;
//		}else{
//			System.out.println("scope is null...");
//			return null;
//		}
//	}
//	
//	public Set<String> getSubComponents(String componentName){
//		Set<String> subComponents = new HashSet<String>();
//		if(scope != null){
//			Element edges = scope.getChild("edges");
//			List edgeList = edges.getChildren("edge");
//			Iterator edgeListIterator = edgeList.iterator();
//			while(edgeListIterator.hasNext()){
//				Element element = (Element) edgeListIterator.next();
//				String srcComponent = element.getAttributeValue("src");
//				if(srcComponent.equals(componentName)){
//					String destComponent = element.getAttributeValue("dest");
//					subComponents.add(destComponent);
//				}
//			}
//			return subComponents;
//		}else{
//			System.out.println("scope is null...");
//			return null;
//		}
//	}
//	
//	public boolean contains(String componentName){
//		if(scope != null){
//			Element nodes = scope.getChild("nodes");
//			List nodesList = nodes.getChildren("node");
//			Iterator nodeListIterator = nodesList.iterator();
//			while(nodeListIterator.hasNext()){
//				Element element = (Element) nodeListIterator.next();
//				String elementName = element.getAttributeValue("id");
//				if(componentName.equals(elementName)){
//					return true;
//				}
//			}
//			return false;
//		}else{
//			System.out.println("scope is null...");
//			return false;
//		}
//	}
//	
//	public Set<String> getAllComponents() {
//		Set<String> components = new HashSet<String>();
//		if(scope != null){
//			Element nodes = scope.getChild("nodes");
//			List nodesList = nodes.getChildren("node");
//			Iterator nodeListIterator = nodesList.iterator();
//			while(nodeListIterator.hasNext()){
//				Element element = (Element) nodeListIterator.next();
//				String elementName = element.getAttributeValue("id");
//				components.add(elementName);
//			}
//			return components;
//		}else{
//			System.out.println("scope is null...");
//			return null;
//		}
//	}
	
	
	
	
	//all components in current scope
	private Set<String> components;
	//target components that need to be dynamically updated
	private Set<String> target;
	
	public Scope(){
		components = new HashSet<String>();
		target = new HashSet<String>();
		//FOR TEST
		components.add("AuthComponent");
		components.add("ProcComponent");
		components.add("PortalComponent");
		
//		components.add("Portal2Component");
//		components.add("Proc2Component");
		target.add("AuthComponent");
	}
	
	public Set<String> getParentComponents(String current){
		Set<String> result = new HashSet<String>();
		
		//TODO
		
		//FOR TEST
		if(current.equals("AuthComponent")){
			result.add("ProcComponent");
			result.add("PortalComponent");
//			result.add("Portal2Component");
//			result.add("Proc2Component");
		} else if(current.equals("ProcComponent")){
			result.add("PortalComponent");
		} else if(current.equals("PortalComponent")){
			
		} 
//		else if(current.equals("Proc2Component")) {
//			result.add("Portal2Component");
//		}
		else if(!components.contains(current)){
			System.out.println("Invalid component name, because " + current + "is not in scope");
		}
		
		return result;
	}
	
	public Set<String> getSubComponents(String current){
		Set<String> result = new HashSet<String>();
		
		//TODO
		
		//FOR TEST
		if (current.equals("AuthComponent")) {
			
		} else if (current.equals("ProcComponent")) {
			result.add("AuthComponent");
		} else if (current.equals("PortalComponent")) {
			result.add("AuthComponent");
			result.add("ProcComponent");
		}
//		else if(current.equals("Portal2Component")) {
//			result.add("Proc2Component");
//			result.add("AuthComponent");
//		} else if(current.equals("Proc2Component")){
//			result.add("AuthComponent");
//		}
		else if (!components.contains(current)) {
			System.out.println("Invalid component name, because " + current
					+ "is not in scope");
		}

		return result;
	}
	
	public boolean contains(String componentName){
		return components.contains(componentName);
	}

	/** return all the components in current scope */
	public Set<String> getAllComponents() {
		return components;
	}

	/** return the components that need to be dynamically updated */
	public Set<String> getTargetComponents() {
		return target;
	}

	public void setTarget(Set<String> target) {
		this.target = target;
	}

	public void setComponents(Set<String> components) {
		this.components = components;
	}
	
	public void addComponent(String componentName){
		components.add(componentName);
	}
	
	

}
