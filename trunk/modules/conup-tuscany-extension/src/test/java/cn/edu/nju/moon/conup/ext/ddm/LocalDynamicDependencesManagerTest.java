package cn.edu.nju.moon.conup.ext.ddm;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.test.BufferTestConvention;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;


public class LocalDynamicDependencesManagerTest {
	TxDepMonitorImpl txDepMonitor = null;
	Node node = null;
	String transactionID;	
	
	@Before
	public void setUp() throws Exception {
//		txDepMonitor = new TxDepMonitorImpl();
//		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
//		node = runtime.createNode();
//		String contributionURL = "src/test/resources/conup-sample-hello-auth.jar";
//		node.installContribution(contributionURL);
////		node.startComposite(contributionURI, compositeURI);
//		node.startDeployables("conup-sample-hello-auth");
//		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
//		transactionID = UUID.randomUUID().toString();
//		TransactionContext tc = new TransactionContext();
//		tc.setCurrentTx(transactionID);
//		tc.setHostComponent("AuthComponent");
//		String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
//		ComponentObject compObject = new ComponentObject("AuthComponent", "1.1", 
//				"CONSISTENCY_ALGORITHM", CONCURRENT_VERSION, 
//				null, null, JAVA_POJO_IMPL_TYPE);
//		NodeManager nodeMgr = NodeManager.getInstance();
//		nodeMgr.addComponentObject("AuthComponent", compObject);
//		DynamicDepManager ddm = nodeMgr.getDynamicDepManager("AuthComponent");
//		
//		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance("AuthComponent");
//		compLifecycleMgr.setNode(node);
//		ddm.setCompStatus(CompStatus.NORMAL);
//		Map<String, TransactionContext> TX_IDS = TxLifecycleManager.TX_IDS;
//		
//		TX_IDS.put(transactionID, tc);
//		LocalDynamicDependencesManager.getInstance(transactionID,"cn/edu/nju/moon/conup/sample/portal/services/TokenService,cn/edu/nju/moon/conup/sample/portal/services/ProcService;cn/edu/nju/moon/conup/sample/portal/services/ProcService;_E", "COM.cn/edu/nju/moon/conup/sample/portal/services/TokenService.23-1;COM.cn/edu/nju/moon/conup/sample/portal/services/ProcService.35-2;_E");
		
	}
	@After
	public void teardown() throws Exception {
//		String contributionURL = "src/test/resources/conup-sample-hello-auth.jar";
//		node.uninstallContribution(contributionURL);
//		node.stop();
	}
	@Test
	public void TestGetFuture() {
//		LocalDynamicDependencesManager lddm = LocalDynamicDependencesManager.getInstance(transactionID);
//		lddm.trigger("Start");
//		Set<String> future = new ConcurrentSkipListSet<String>();
//		future.add("cn.edu.nju.moon.conup.sample.portal.services.TokenService");
//		future.add("cn.edu.nju.moon.conup.sample.portal.services.ProcService");
//		assertTrue(lddm.getFuture().equals(future));
//		future.remove("cn.edu.nju.moon.conup.sample.portal.services.TokenService");
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/TokenService.23");
//		assertTrue(lddm.getFuture().equals(future));
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/ProcService.35");
//		future.remove("cn.edu.nju.moon.conup.sample.portal.services.ProcService");
//		assertTrue(lddm.getFuture().equals(future));
//		lddm.trigger("");
//		assertTrue(lddm.getFuture().equals(future));
	}
	@Test
	public void TestGetPast() {
//		LocalDynamicDependencesManager lddm = LocalDynamicDependencesManager.getInstance(transactionID);
//		lddm.trigger("Start");
//		Set<String> past = new ConcurrentSkipListSet<String>();
//		assertTrue(lddm.getPast().equals(past));
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/TokenService.23");
//		past.add("cn.edu.nju.moon.conup.sample.portal.services.TokenService");
//		assertTrue(lddm.getPast().equals(past));
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/ProcService.35");
//		past.add("cn.edu.nju.moon.conup.sample.portal.services.ProcService");
//		assertTrue(lddm.getPast().equals(past));
//		lddm.trigger("");
//		assertTrue(lddm.getPast().equals(past));
	}
	@Test
	public void TestTrigger() {
//		LocalDynamicDependencesManager lddm = LocalDynamicDependencesManager.getInstance(transactionID);
//		lddm.trigger("Start");		
//		assertEquals(0,lddm.getCurrentState());
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/TokenService.23");
//		assertEquals(1,lddm.getCurrentState());
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/ProcService.35");
//		assertEquals(2,lddm.getCurrentState());
//		lddm.trigger("");
//		assertEquals(2,lddm.getCurrentState());
	}
	@Test
	public void TestIsThisLastUsed() {
//		LocalDynamicDependencesManager lddm = LocalDynamicDependencesManager.getInstance(transactionID);
//		lddm.trigger("Start");		
//		assertTrue(lddm.whetherUseInFuture("cn.edu.nju.moon.conup.sample.portal.services.ProcService"));
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/TokenService.23");		
//		assertFalse(lddm.whetherUseInFuture("cn.edu.nju.moon.conup.sample.portal.services.ProcService"));
//		lddm.trigger("COM.cn/edu/nju/moon/conup/sample/portal/services/ProcService.35");
//		assertFalse(lddm.whetherUseInFuture("cn.edu.nju.moon.conup.sample.portal.services.ProcService"));
//		lddm.trigger("");
//		assertFalse(lddm.whetherUseInFuture("cn.edu.nju.moon.conup.sample.portal.services.ProcService"));
	}
	@Test
	public void TestReverse() {

//		LocalDynamicDependencesManager lddm = LocalDynamicDependencesManager.getInstance(transactionID);
//		Set<String> newset = new ConcurrentSkipListSet<String>();
//		newset.add("cn/edu/nju/moon/conup/sample/portal/services/TokenService");
//		newset.add("cn/edu/nju/moon/conup/sample/portal/services/PortalService");
//		lddm.reverse(newset);
//		Set<String> aset = new ConcurrentSkipListSet<String>();
//		aset.add("cn.edu.nju.moon.conup.sample.portal.services.PortalService");
//		aset.add("cn.edu.nju.moon.conup.sample.portal.services.TokenService");
//		assertEquals(aset,lddm.reverse(newset));
	}

}

