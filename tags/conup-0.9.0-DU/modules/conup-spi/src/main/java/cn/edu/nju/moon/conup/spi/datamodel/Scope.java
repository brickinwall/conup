package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Scope {
	
	private Map<String, Set<String>> parentComponents;
	private Map<String, Set<String>> subComponents;
	
	/** String "compA<compB" means that compB is a parent component of compA */
	public static final String PARENT_SEPERATOR = "<";
	/** String "compA>compC" means that compc is a sub component of compA */
	public static final String SUB_SEPERATOR = ">";
	/** String "compA+compB|compA-compC" means that compA has a parent component CompB 
	 * 	and sub component compC*/
	public static final String SCOPE_ENTRY_SEPERATOR = "#";
	public static final String TARGET_IDENTIFIER = "TARGET_COMP";
	/** */
	public static final String TARGET_SEPERATOR = "@";
	
	//all components in current scope
	private Set<String> components;
	//target components that need to be dynamically updated
	private Set<String> target;
	
	public Scope(){
		components = new HashSet<String>();
		target = new HashSet<String>();
		parentComponents = new HashMap<String, Set<String>>();
		subComponents = new HashMap<String, Set<String>>();
	}
	
	
	public Set<String> getParentComponents(String current){
		return parentComponents.get(current);
	}
	
	public Set<String> getSubComponents(String current){
		return subComponents.get(current);
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

	public void addComponent(String componentName, Set<String> parentComps, Set<String> subComps){
		this.components.add(componentName);
		if(parentComponents.get(componentName) == null){
			parentComponents.put(componentName, new HashSet<String>());
		}
		this.parentComponents.get(componentName).addAll(parentComps);
		
		if(subComponents.get(componentName) == null){
			subComponents.put(componentName, new HashSet<String>());
		}
		this.subComponents.get(componentName).addAll(subComps);
	}
	
	public void setComponent(String componentName, Set<String> parentComps, Set<String> subComps){
		this.components.add(componentName);
		this.parentComponents.put(componentName, parentComps);
		this.parentComponents.put(componentName, subComps);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for(Entry<String, Set<String>> entry : parentComponents.entrySet()){
			for(String parent : entry.getValue()){
				buffer.append(entry.getKey() + PARENT_SEPERATOR + parent + SCOPE_ENTRY_SEPERATOR);
			}
		}
		
		for (Entry<String, Set<String>> entry : subComponents.entrySet()) {
			for(String sub : entry.getValue()){
				buffer.append(entry.getKey() + SUB_SEPERATOR + sub + SCOPE_ENTRY_SEPERATOR);
			}
		}
		for(String t : target){
			buffer.append(TARGET_IDENTIFIER + TARGET_SEPERATOR + t + SCOPE_ENTRY_SEPERATOR);
		}
		buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
	}
	
	public static Scope inverse(String scopeString){
		if(scopeString == null)
			return null;
		
		Scope scope = new Scope();
		String [] split = scopeString.split(SCOPE_ENTRY_SEPERATOR);
		
		for(String entry : split){
			if(entry.contains(PARENT_SEPERATOR)){
				String curComp = entry.split(PARENT_SEPERATOR)[0];
				Set<String> parentComps = new HashSet<String>();
				parentComps.add(entry.split(PARENT_SEPERATOR)[1]);
				scope.addComponent(curComp, parentComps, new HashSet<String>());
			} else if(entry.contains(SUB_SEPERATOR)){
				String curComp = entry.split(SUB_SEPERATOR)[0];
				Set<String> subComps = new HashSet<String>();
				subComps.add(entry.split(SUB_SEPERATOR)[1]);
				scope.addComponent(curComp, new HashSet<String>(), subComps);
			} else if(entry.contains(TARGET_SEPERATOR)){
				String targetComp = entry.split(TARGET_SEPERATOR)[1];
				scope.getTargetComponents().add(targetComp);
			} else{
				return null;
			}
		}
		
		if(scope.getTargetComponents().size() == 0){
			return null;
		}
		
		return scope;
	}


	public boolean isTarget(String compName) {
		return target.contains(compName);
	}


	public boolean isRootComp(String hostComp) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
