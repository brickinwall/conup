package cn.edu.nju.moon.conup.spi.manager;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.test.SpiTestConvention;

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
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		String compIdentifier = null;
		String compVer = null;
		String algorithmConf = null;
		String freenessConf = null;
		compIdentifier = "AuthComponent";
		compVer = "oldVersion";
		algorithmConf = "CONSISTENCY_ALGORITHM";
		freenessConf = CONCURRENT_VERSION;
		compObj = new ComponentObject(compIdentifier, compVer, algorithmConf, 
				freenessConf, null, null, SpiTestConvention.JAVA_POJO_IMPL_TYPE);
	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDynamicDepManager() {
		DynamicDepManager depMgr;
		if(nodeMgr.getComponentObject("AuthComponent") != null)
			nodeMgr.removeCompObject(compObj);
		depMgr = nodeMgr.getDynamicDepManager(compObj.getIdentifier());
		assertNull(depMgr);
		
		nodeMgr.addComponentObject(compObj.getIdentifier(), compObj);
		depMgr = nodeMgr.getDynamicDepManager(compObj.getIdentifier());
		assertNotNull(depMgr);
		assertNotNull(depMgr.getCompObject());
		assertEquals(compObj, depMgr.getCompObject());
	}

	@Test
	public void testGetOndemandSetupHelper() {
		OndemandSetupHelper helper;
		if(nodeMgr.getComponentObject("AuthComponent") != null)
			nodeMgr.removeCompObject(compObj);
		helper = nodeMgr.getOndemandSetupHelper(compObj.getIdentifier());
		assertNull(helper);
		
		nodeMgr.addComponentObject(compObj.getIdentifier(), compObj);
		helper = nodeMgr.getOndemandSetupHelper(compObj.getIdentifier());
		assertNotNull(helper);
		assertNotNull(helper.getCompObject());
		assertEquals(compObj, helper.getCompObject());
	}

}
