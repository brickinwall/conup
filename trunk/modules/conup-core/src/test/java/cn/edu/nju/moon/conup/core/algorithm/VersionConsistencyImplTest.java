package cn.edu.nju.moon.conup.core.algorithm;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

public class VersionConsistencyImplTest {
	
	VersionConsistencyImpl vc = null;
	DynamicDepManagerImpl ddm = null;
	NodeManager nodeManager = null;
	
	@Before
	public void setUp() throws Exception {
		vc = new VersionConsistencyImpl();
		
		nodeManager = NodeManager.getInstance();
		ComponentObject compObj = new ComponentObject("AuthComponent", "1.1", VersionConsistencyImpl.ALGORITHM_TYPE, FreenessStrategy.CONCURRENT_VERSION);
		nodeManager.setComponentObject("AuthComponent", compObj);
		
		ddm = (DynamicDepManagerImpl)nodeManager.getDynamicDepManager("AuthComponent");
		ddm.setCompStatus(CompStatus.NORMAL);
		ddm.setAlgorithm(compObj.getAlgorithmConf());
	}

	@Test
	public void testManageDependenceTransactionContext() {
		TransactionContext tc = new TransactionContext();
		tc.setCurrentTx(UUID.randomUUID().toString());
		tc.setEventType(TxEventType.TransactionStart);
		tc.setFutureComponents(new HashSet<String>());
		tc.setHostComponent("AuthComponent");
		tc.setParentComponent("PortalComponent");
		String rootTx = UUID.randomUUID().toString();
		tc.setParentTx(rootTx);
		tc.setPastComponents(new HashSet<String>());
		tc.setRootComponent("PortalComponent");
		tc.setRootTx(rootTx);
		
		vc.manageDependence(tc);
	}

	@Test
	public void testManageDependenceStringStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAlgorithmType() {
		assertEquals("CONSISTENCY_ALGORITHM", vc.getAlgorithmType());
	}

}
