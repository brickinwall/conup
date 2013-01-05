package cn.edu.nju.moon.conup.ext.tx.manager;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.UUID;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.datamodel.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
/**
 * @author rgc
 */
public class TxDepMonitorTest {
	TxDepMonitorImpl txDepMonitor = null;
	Node node = null;
	@Before
	public void setUp() throws Exception {
		txDepMonitor = new TxDepMonitorImpl();
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		node = runtime.createNode();
		if(node.getStartedCompositeURIs().get("conup-sample-hello-auth") != null){
			node.uninstallContribution("conup-sample-hello-auth");
		}
		String contributionURL = "src/test/resources/conup-sample-hello-auth.jar";
//		String contributionURI = "conup-sample-hello-auth";
//		String compositeURI = "auth.composite";
		
		node.installContribution(contributionURL);
//		node.startComposite(contributionURI, compositeURI);
		node.startDeployables("conup-sample-hello-auth");
		
		
//		String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
//		String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
//		NodeManager nodeMgr = NodeManager.getInstance();
//		Set<String> staticDeps = new HashSet<String>();
//		String implType = BufferTestConvention.JAVA_POJO_IMPL_TYPE;
//		String compIdentifier = "HelloworldComponent";
//		ComponentObject comObj = new ComponentObject(compIdentifier, "1.1", ALGORITHM_TYPE, CONCURRENT_VERSION, staticDeps,null , implType);
//		nodeMgr.addComponentObject(compIdentifier, comObj);
//		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance("AuthComponent");
//		compLifecycleMgr.setNode(node);
//		clm = CompLifecycleManager.getInstance(compIdentifier);
	}
	@After
	public void tearDown(){
		node.stop();
	}

	@Test
	public void testNotifyTxEventTypeString() throws ContributionReadException, ValidationException, ActivationException {
		
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		String curTxID = UUID.randomUUID().toString();
		TransactionContext tc = new TransactionContext();
		tc.setCurrentTx(curTxID);
		tc.setHostComponent("AuthComponent");
		String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
		ComponentObject compObject = new ComponentObject("AuthComponent", "1.1", 
				"CONSISTENCY_ALGORITHM", CONCURRENT_VERSION, 
				null, null, JAVA_POJO_IMPL_TYPE);
		NodeManager nodeMgr = NodeManager.getInstance();
		nodeMgr.addComponentObject("AuthComponent", compObject);
//		DynamicDepManager ddm = nodeMgr.getDynamicDepManager("AuthComponent");
		
		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance("AuthComponent");
		compLifecycleMgr.setNode(node);
//		ddm.setCompStatus(CompStatus.NORMAL);
		Map<String, TransactionContext> TX_IDS = TxLifecycleManager.TX_IDS;
		
		TX_IDS.put(curTxID, tc);
		
		String states = "_E";
		String nexts = "_E";
		LocalDynamicDependencesManager localDDM = LocalDynamicDependencesManager.getInstance(curTxID, states, nexts);
//		txDepMonitor.notify(TxEventType.FirstRequestService, curTxID);
	}
}
