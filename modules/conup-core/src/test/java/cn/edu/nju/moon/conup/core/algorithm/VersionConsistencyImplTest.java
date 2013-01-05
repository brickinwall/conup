package cn.edu.nju.moon.conup.core.algorithm;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

public class VersionConsistencyImplTest {

	private static final String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
	
	public final static String FUTURE_DEP = "FUTURE_DEP";
	/** dependence type is "past" */
	public final static String PAST_DEP = "PAST_DEP";
	VersionConsistencyImpl vc = null;
	DynamicDepManagerImpl authDDM = null;
	DynamicDepManagerImpl portalDDM = null;
	NodeManager nodeManager = null;

	@Before
	public void setUp() throws Exception {
		vc = new VersionConsistencyImpl();
//
//		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
//
//		nodeManager = NodeManager.getInstance();
//		ComponentObject authCompObj = new ComponentObject("AuthComponent", "1.1",
//				VersionConsistencyImpl.ALGORITHM_TYPE, CONCURRENT_VERSION,
//				new HashSet<String>(),new HashSet<String>(), JAVA_POJO_IMPL_TYPE);
//		
//		authCompObj.getStaticInDeps().add("PortalComponent");
//		
//		authCompObj.setStaticDeps(new HashSet<String>());
//		nodeManager.addComponentObject("AuthComponent", authCompObj);
//		authDDM = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager("AuthComponent");
//		authDDM.setAlgorithm(new AlgorithmFactory().createAlgorithm(authCompObj.getAlgorithmConf()));
//		
//		ComponentObject portalCompObj = new ComponentObject("PortalComponent", "1.1",
//				VersionConsistencyImpl.ALGORITHM_TYPE, CONCURRENT_VERSION,
//				new HashSet<String>(), new HashSet<String>(),JAVA_POJO_IMPL_TYPE);
//		
//		portalCompObj.getStaticDeps().add("AuthComponent");
//		
//		nodeManager.addComponentObject("PortalComponent", portalCompObj);
//		portalDDM  = (DynamicDepManagerImpl)nodeManager.getDynamicDepManager("PortalComponent");
//		portalDDM.setAlgorithm(new AlgorithmFactory().createAlgorithm(portalCompObj.getAlgorithmConf()));
//		
//		CommServerManager.getInstance().start("PortalComponent");
//		CommServerManager.getInstance().start("AuthComponent");
	}

	@Test
	public void testManageDependenceTransactionContext() {
		/*
		 * test CompStatus.NORMAL
//		 */
//		authDDM.setCompStatus(CompStatus.NORMAL);
//		TransactionContext tc1 = new TransactionContext();
//		tc1.setCurrentTx(UUID.randomUUID().toString());
//		tc1.setEventType(TxEventType.TransactionStart);
//		tc1.setFutureComponents(new HashSet<String>());
//		tc1.setHostComponent("AuthComponent");
//		tc1.setParentComponent("PortalComponent");
//		String rootTx1 = UUID.randomUUID().toString();
//		tc1.setParentTx(rootTx1);
//		tc1.setPastComponents(new HashSet<String>());
//		tc1.setRootComponent("PortalComponent");
//		tc1.setRootTx(rootTx1);
//
//		vc.manageDependence(tc1);
//
//		/*
//		 * test CompStatus.VALID
//		 * test when sub-tx start, then parent tx should ask whether need to remove future dep?
//		 */
//		String rootTx2 = UUID.randomUUID().toString();
//		authDDM.setCompStatus(CompStatus.VALID);
//		// AuthComponent's InDepRegistry add future indep
//		Dependence dep1 = new Dependence(FUTURE_DEP, rootTx2, "PortalComponent", "AuthComponent", null, null);
//		authDDM.getInDepRegistry().addDependence(dep1);
//		
//		// PortalComponent's OutDepRegistry add future outdep
//		Dependence dep2 = new Dependence(FUTURE_DEP, rootTx2, "PortalComponent", "AuthComponent", null, null);
//		portalDDM.getOutDepRegistry().addDependence(dep2 );
//		
//		// create a new sub tx
//		TransactionContext tc2 = new TransactionContext();
//		tc2.setCurrentTx(UUID.randomUUID().toString());
//		tc2.setEventType(TxEventType.TransactionStart);
//		tc2.setFutureComponents(new HashSet<String>());
//		tc2.setHostComponent("AuthComponent");
//		tc2.setParentComponent("PortalComponent");
//		tc2.setParentTx(rootTx2);
//		tc2.setPastComponents(new HashSet<String>());
//		tc2.setRootComponent("PortalComponent");
//		tc2.setRootTx(rootTx2);
//		vc.manageDependence(tc2);
//		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		assertFalse(authDDM.getInDepRegistry().contain(dep1));
//		assertFalse(portalDDM.getOutDepRegistry().contain(dep2));
		
	}

	/*
	 * test TxEventType.FirstRequestService
	 */
	@Test
	public void testFirstRequestService() throws InterruptedException{
		
//		String rootTx = UUID.randomUUID().toString();
//		TransactionContext tc = new TransactionContext();
//		tc.setCurrentTx(rootTx);
//		tc.setEventType(TxEventType.TransactionStart);
//		Set<String> futureC = new HashSet<String>();
//		futureC.add("AuthComponent");
//		tc.setFutureComponents(futureC);
//		tc.setHostComponent("PortalComponent");
//		tc.setParentComponent(null);
//		tc.setParentTx(rootTx);
//		tc.setPastComponents(new HashSet<String>());
//		tc.setRootComponent("PortalComponent");
//		tc.setRootTx(rootTx);
//		portalDDM.setCompStatus(CompStatus.VALID);
//		tc.setEventType(TxEventType.TransactionStart);
//		vc.manageDependence(tc);
//		
//		tc.setEventType(TxEventType.FirstRequestService);
//		vc.manageDependence(tc);
//		
//		assertTrue(portalDDM.getOutDepRegistry().size() == 2);
//		assertTrue(authDDM.getInDepRegistry().size() == 1);
	}
	
	@Test
	public void testManageDependenceStringStringString() {
//		String payload = createPayload("PortalComponent", "AuthComponent", UUID
//				.randomUUID().toString(), "NOTIFY_FUTURE_CREATE");// "EventType.ACK_FUTURE_CREATE";
//		vc.manageDependence(payload);
	}

	@Test
	public void testGetAlgorithmType() {
		assertEquals("CONSISTENCY_ALGORITHM", vc.getAlgorithmType());
	}

	@After
	public void tearDown(){
//		CommServerManager.getInstance().stop("PortalComponent");
//		CommServerManager.getInstance().stop("AuthComponent");
	}
	
	
	private String createPayload(String srcComp, String targetComp,
			String rootTx, String operationType) {
		String payload = "ConsistencyPayload.SRC_COMPONENT:" + srcComp
				+ ",ConsistencyPayload.TARGET_COMPONENT:" + targetComp
				+ ",ConsistencyPayload.ROOT_TX:" + rootTx
				+ ",ConsistencyPayload.OPERATION_TYPE:" + operationType;
		return payload;
	}
}
