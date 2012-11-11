package cn.edu.nju.moon.conup.spi.factory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ManagerFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateDynamicDepManager() {
		ManagerFactory mgrFactory = new ManagerFactory();
		assertNotNull(mgrFactory.createDynamicDepManager());
	}

	@Test
	public void testCreateOndemandSetupHelper() {
		fail("Not yet implemented");
	}
	
	public static void main(String[] args) {
		ManagerFactory mgrFactory = new ManagerFactory();
		mgrFactory.createDynamicDepManager();
	}

}
