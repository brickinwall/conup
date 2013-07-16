package cn.edu.nju.moon.conup.comm.api.server;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
/**
 * @author rgc
 */
public class CompCommServerTest {
	
	CommServer ccs = null;
	@Before
	public void setUp() throws Exception {
		ccs = new CommServer("TestComp");
	}

	@Test
	public void testStart() {
		ccs.start("localhost", 19999);
		ccs.stop();
	}

	@Test
	public void testStop() {
		ccs.start("localhost", 19999);
		assertTrue(ccs.stop());
		
	}

}
