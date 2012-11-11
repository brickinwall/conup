package cn.edu.nju.moon.conup.spi.factory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

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
//		System.out.println("depMgr:" + depMgr);
//		System.out.println("depMgr.getCompObject():" + depMgr.getCompObject());
//	}

}
