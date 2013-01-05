package cn.edu.nju.moon.conup.txpre;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.txpre.ControlFlow;
import cn.edu.nju.moon.conup.txpre.NodeAndOutarcs;

public class ControlFlowTest {
	
	ControlFlow controlFlow = null;	

	@Before
	public void setUp() throws Exception {
		List<NodeAndOutarcs> con = new LinkedList<NodeAndOutarcs>();
		con.add(new NodeAndOutarcs(0,1));
		con.add(new NodeAndOutarcs(1,2));
		con.add(new NodeAndOutarcs(2,3));
		controlFlow = new ControlFlow(con);	
	}
	
	@Test
	public void testAddFlow() {
		NodeAndOutarcs arc = new NodeAndOutarcs(1,3);
		assertFalse(controlFlow.getFlow().contains(arc));
		controlFlow.addFlow(1, 3);
		assertTrue(controlFlow.getFlow().contains(controlFlow.getFlow(1)));		
		
	}
	public void testGetDst(){
		List<Integer> dst = new LinkedList<Integer>();
		dst.add(2);
		assertTrue(controlFlow.getDst(1).equals(dst));
		controlFlow.addFlow(1, 3);
		dst.add(3);
		assertTrue(controlFlow.getDst(1).equals(dst));	
	}

}
