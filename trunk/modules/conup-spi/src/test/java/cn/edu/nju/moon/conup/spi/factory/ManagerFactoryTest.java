package cn.edu.nju.moon.conup.spi.factory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.test.SpiTestConvention;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ManagerFactoryTest {
	private ComponentObject compObj;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		String compIdentifier = null;
		String compVer = null;
		String algorithmConf = null;
		String freenessConf = null;
		compIdentifier = "AuthComponent";
		compVer = "oldVersion";
		algorithmConf = "";
		freenessConf = CONCURRENT_VERSION;
		compObj = new ComponentObject(compIdentifier, compVer, algorithmConf, 
				freenessConf, null, null, SpiTestConvention.JAVA_POJO_IMPL_TYPE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateDynamicDepManager() {
		ManagerFactory mgrFactory = new ManagerFactory();
		DynamicDepManager depMgr = mgrFactory.createDynamicDepManager();
		assertNotNull(depMgr);
	}

	@Test
	public void testCreateOndemandSetupHelper() {
		ManagerFactory mgrFactory = new ManagerFactory();
		OndemandSetupHelper helper = mgrFactory.createOndemandSetupHelper();
		assertNotNull(helper);
	}
	
//	public static void main(String[] args) {
//		String compIdentifier = null;
//		String compVer = null;
//		String algorithmConf = null;
//		String freenessConf = null;
//		ComponentObject compObj;
//		compIdentifier = "AuthComponent";
//		compVer = "oldVersion";
//		algorithmConf = "";
//		freenessConf = FreenessStrategy.CONCURRENT_VERSION;
//		compObj = new ComponentObject(compIdentifier, compVer, algorithmConf, freenessConf);
//		
//		ManagerFactory mgrFactory = new ManagerFactory();
//		DynamicDepManager depMgr = mgrFactory.createDynamicDepManager();
//		LOGGER.fine("depMgr:" + depMgr);
//		LOGGER.fine("depMgr.getCompObject():" + depMgr.getCompObject());
//	}

}
