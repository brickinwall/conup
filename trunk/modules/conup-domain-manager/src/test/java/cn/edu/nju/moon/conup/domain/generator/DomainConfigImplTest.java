package cn.edu.nju.moon.conup.domain.generator;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.domain.def.StaticEdge;
import cn.edu.nju.moon.conup.domain.generator.DomainConfigImpl;

public class DomainConfigImplTest {
	static DomainConfigImpl dc = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dc = new DomainConfigImpl();
	}

	@Test
	public void testGetAllNodes() {
		List<String> nodes = dc.getAllNodes();
		Iterator it = nodes.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}

	@Test
	public void testGetAllEdges() {
		List<StaticEdge> edges = dc.getAllEdges();
		Iterator it = edges.iterator();
		while(it.hasNext()){
			StaticEdge se = (StaticEdge) it.next();
			System.out.println(se.getSrcComponent() + " -> " + se.getTargetComponent());
		}
	}

	@Test
	public void testGetTargetNode() {
		List<String> nodes = dc.getDestNodes("PortalComponent");
		Iterator it = nodes.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		
	}

}
