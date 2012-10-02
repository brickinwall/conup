package cn.edu.nju.moon.conup.domain.services;

import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Remotable;

import cn.edu.nju.moon.conup.def.Scope;

@Remotable
public interface StaticConfigService {
	public Set<String> getParentComponents(String target);
	public Set<String> getSubComponents(String target);
	public Scope getScope(String target);
	
	public void createGraph();
	public boolean addNodes(Map<String,String> nodesInfo);
	public boolean addEdges(Map<String, Set<String>> edgesInfo);
	
}
