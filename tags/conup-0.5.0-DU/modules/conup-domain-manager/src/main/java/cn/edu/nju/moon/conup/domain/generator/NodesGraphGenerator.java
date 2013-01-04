package cn.edu.nju.moon.conup.domain.generator;

public interface NodesGraphGenerator {
	public void createGraph();
	public boolean addNode(String componentName, String bindingIpAndPort);
	public boolean addEdge(String srcComponent, String destComponent);
}
