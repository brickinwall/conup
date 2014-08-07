package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.UUID;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
/**
 * @author rgc
 */
public class TxDepMonitorTest {
	TxDepMonitorImpl txDepMonitor = null;
	Node node = null;
	@Before
	public void setUp() throws Exception {
		NodeManager nodeMgr = NodeManager.getInstance();
		String componentName = "AuthComponent";
		nodeMgr.loadConupConf(componentName , "Version");
		ComponentObject compObj = nodeMgr.getComponentObject(componentName);
		
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObj);
        nodeMgr.setTxLifecycleManager(componentName, txLifecycleMgr);
        
		txDepMonitor = new TxDepMonitorImpl(compObj);
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

	@SuppressWarnings("unused")
	@Test
	public void testNotifyTxEventTypeString() throws ContributionReadException, ValidationException, ActivationException {
		String hostCompName = "AuthComponent";
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		String curTxID = UUID.randomUUID().toString();
		TransactionContext tc = new TransactionContext();
		tc.setCurrentTx(curTxID);
		tc.setHostComponent(hostCompName);
		String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
		ComponentObject compObject = new ComponentObject("AuthComponent", "1.1", 
				"CONSISTENCY_ALGORITHM", CONCURRENT_VERSION, 
				null, null, JAVA_POJO_IMPL_TYPE);
		NodeManager nodeMgr = NodeManager.getInstance();
		nodeMgr.addComponentObject("AuthComponent", compObject);
		CompLifecycleManagerImpl compLifecycleMgr = new CompLifecycleManagerImpl(compObject);
		nodeMgr.addComponentObject(hostCompName, compObject);
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObject);
		nodeMgr.setTxLifecycleManager("AuthComponent", txLifecycleMgr);
//		DynamicDepManager ddm = nodeMgr.getDynamicDepManager("AuthComponent");
		
//		CompLifecycleManagerImpl compLifecycleMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance("AuthComponent");
//		compLifecycleMgr.setNode(node);
		nodeMgr.setTuscanyNode(node);
//		ddm.setCompStatus(CompStatus.NORMAL);
		
		
//		Map<String, TransactionContext> TX_IDS = TxLifecycleManagerImpl.TX_IDS;
//		
//		TX_IDS.put(curTxID, tc);
		TransactionRegistry txRegistry = txLifecycleMgr.getTxRegistry();
		txRegistry.addTransactionContext(curTxID, tc);
		
		String states = "_E";
		String nexts = "_E";
		LocalDynamicDependencesManager localDDM = LocalDynamicDependencesManager.getInstance(curTxID, states, nexts);
//		txDepMonitor.notify(TxEventType.FirstRequestService, curTxID);
	}
}
