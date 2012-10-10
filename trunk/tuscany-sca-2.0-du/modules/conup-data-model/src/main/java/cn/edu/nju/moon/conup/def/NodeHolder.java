package cn.edu.nju.moon.conup.def;

import org.apache.tuscany.sca.Node;

public class NodeHolder {
	private static NodeHolder nodeHolder = new NodeHolder();
	private Node node = null;
	
	private NodeHolder(){
	}
	
	public static NodeHolder getInstance(){
		return nodeHolder;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	
	
}
