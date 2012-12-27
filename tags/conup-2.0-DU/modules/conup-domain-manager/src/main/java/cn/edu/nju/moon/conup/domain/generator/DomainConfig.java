package cn.edu.nju.moon.conup.domain.generator;

import java.util.List;
import java.util.Map;

import cn.edu.nju.moon.conup.domain.def.StaticEdge;

public interface DomainConfig {
	public List<String> getAllNodes();
	
	public List<StaticEdge> getAllEdges();
	
	public List<String> getDestNodes(String srcNode);
	
	public List<String> getSrcNodes(String destNode);
}
