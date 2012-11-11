package cn.edu.nju.moon.conup.spi.manager;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class NodeManagerTest {
	NodeManager nodeMgr = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		nodeMgr = NodeManager.getInstance();
		nodeMgr.setComponentObject("AuthComponent", 
				new ComponentObject("AuthComponent", null, Algorithm.CONSISTENCY_ALGORITHM, null));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDynamicDepManager() {
		System.out.println("mgr" + nodeMgr.getDynamicDepManager("AuthComponent"));
		System.out.println("compObj" + nodeMgr.getComponentObject("AuthComponent"));
//		assertTrue(nodeMgr.getDynamicDepManager("AuthComponent") != null);
	}

	@Test
	public void testGetOndemandSetupHelper() {
		assertTrue(nodeMgr.getOndemandSetupHelper("AuthComponent") != null);
	}

}
