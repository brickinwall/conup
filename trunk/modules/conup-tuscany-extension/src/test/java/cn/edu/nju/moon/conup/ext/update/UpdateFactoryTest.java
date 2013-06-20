package cn.edu.nju.moon.conup.ext.update;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.test.BufferTestConvention;
import cn.edu.nju.moon.conup.spi.complifecycle.ComponentUpdator;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class UpdateFactoryTest {

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
	public void testCreateCompUpdator() {
		ComponentUpdator updtor = UpdateFactory.createCompUpdator(BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		assertNotNull(updtor);
	}

	@Test
	public void testCreateTransformer() {
	}

//	@Test
//	public void testCreateFreenessStrategy(){
//		
//		String freenessConf = ConcurrentVersionStrategy.CONCURRENT_VERSION;
//		FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
//		assertNotNull(freeness);
//	}
	
	@Test
	public void testCreateFreenessCallback() {
		ComponentUpdator callback = UpdateFactory.createCompUpdator(BufferTestConvention.JAVA_POJO_IMPL_TYPE);
		assertNotNull(callback);
	}

}
