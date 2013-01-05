package cn.edu.nju.moon.conup.core.manager.impl;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

public class DynamicDepManagerImplTest {

	DynamicDepManagerImpl ddm = null;
	NodeManager nodeManager = null;
	@Before
	public void setUp() throws Exception {
		nodeManager = NodeManager.getInstance();
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		ComponentObject compObj = new ComponentObject("AuthComponent", "1.1", 
				VersionConsistencyImpl.ALGORITHM_TYPE, CONCURRENT_VERSION,
				null,null, "JAVA_POJO");
		nodeManager.addComponentObject("AuthComponent", compObj);
		
	}

	@Test
	public void testManageTx() {
		
	}
}
