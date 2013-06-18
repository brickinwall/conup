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
	}

	@Test
	public void testGetAlgorithmType() {
		assertEquals("CONSISTENCY_ALGORITHM", vc.getAlgorithmType());
	}
	
}
