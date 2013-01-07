package cn.edu.nju.moon.conup.core.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ConsistencyOndemandPayloadResolverTest {
	private ConsistencyOndemandPayloadResolver ondemandPlResolver;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		String payload;
		payload = ConsistencyPayload.OPERATION_TYPE + ":" + ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND	+ "," +
				ConsistencyPayload.SRC_COMPONENT + ":" + "PortalComponent" + "," +
				ConsistencyPayload.TARGET_COMPONENT + ":" + "AuthComponent" + "," + 
				ConsistencyPayload.ROOT_TX + ":" + "rootTx_1";
		ondemandPlResolver = new ConsistencyOndemandPayloadResolver(payload);
				
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetOperation() {
		assertEquals(ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND, ondemandPlResolver.getOperation());
	}

	@Test
	public void testGetParameter() {
		assertEquals("PortalComponent", ondemandPlResolver.getParameter(ConsistencyPayload.SRC_COMPONENT));
		assertEquals("AuthComponent", ondemandPlResolver.getParameter(ConsistencyPayload.TARGET_COMPONENT));
		assertEquals("rootTx_1", ondemandPlResolver.getParameter(ConsistencyPayload.ROOT_TX));
	}

}
