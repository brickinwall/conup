package cn.edu.nju.moon.conup.domain.generator;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.domain.generator.NodesGraphGeneratorImpl;

public class NodesGraphGeneratorImplTest {

	static NodesGraphGeneratorImpl nodesGraphGenerator;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		nodesGraphGenerator = NodesGraphGeneratorImpl.getInstance();
	}
	
	@Test
	public void testAddNode() {
		nodesGraphGenerator.addNode("auth", "10.0.2.15:8082");
	}

	@Test
	public void testAddEdge() {
		nodesGraphGenerator.addEdge("portal", "auth");
	}

	@Test
	public void testCreateGraph() {
		nodesGraphGenerator.createGraph();
	}

}
