package cn.edu.nju.moon.conup.spi.manager;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class NodeManagerTest {
	NodeManager nodeMgr = null;
	private ComponentObject compObj;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		nodeMgr = NodeManager.getInstance();
		
		String compIdentifier = null;
		String compVer = null;
		String algorithmConf = null;
		String freenessConf = null;
		compIdentifier = "AuthComponent";
		compVer = "oldVersion";
		algorithmConf = "";
		freenessConf = FreenessStrategy.CONCURRENT_VERSION;
		compObj = new ComponentObject(compIdentifier, compVer, algorithmConf, freenessConf);
	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDynamicDepManager() {
		DynamicDepManager depMgr;
		
		depMgr = nodeMgr.getDynamicDepManager(compObj.getIdentifier());
		assertNull(depMgr);
		
		nodeMgr.setComponentObject(compObj.getIdentifier(), compObj);
		depMgr = nodeMgr.getDynamicDepManager(compObj.getIdentifier());
		assertNotNull(depMgr);
		assertNotNull(depMgr.getCompObject());
		assertEquals(compObj, depMgr.getCompObject());
	}

	@Test
	public void testGetOndemandSetupHelper() {
		OndemandSetupHelper helper;
		
		nodeMgr.removeCompObject(compObj);
		helper = nodeMgr.getOndemandSetupHelper(compObj.getIdentifier());
		assertNull(helper);
		
		nodeMgr.setComponentObject(compObj.getIdentifier(), compObj);
		helper = nodeMgr.getOndemandSetupHelper(compObj.getIdentifier());
		assertNotNull(helper);
		assertNotNull(helper.getCompObject());
		assertEquals(compObj, helper.getCompObject());
	}

}
