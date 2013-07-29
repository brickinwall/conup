package cn.edu.nju.moon.conup.comm.api.utils;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 * @version Nov 22, 2012 4:09:43 PM
 */
public class CommInfosTest {
	@Before
	public void setUp() throws Exception {
		Node node = null;
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		node = runtime.createNode("default");
		// install and start contribution
		node.installContribution("conup-sample-hello-auth",
				"src/test/resources/conup-sample-hello-auth.jar", null, null);
		node.startComposite("conup-sample-hello-auth", "auth.composite");
//		String implType = BufferTestConvention.JAVA_POJO_IMPL_TYPE;
		String compIdentifier = "AuthComponent";
//		ComponentObject comObj = new ComponentObject(compIdentifier, "1.1", ALGORITHM_TYPE, CONCURRENT_VERSION, staticDeps,null , implType);
//		nodeMgr.addComponentObject(compIdentifier, comObj);
//		CompLifecycleManager compLifecycleMgr = CompLifecycleManager.getInstance(compIdentifier);
//		compLifecycleMgr.setNode(node);

		String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
		String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		NodeManager nodeMgr = NodeManager.getInstance();
		Set<String> staticDeps = new HashSet<String>();
		String implType = "JAVA_POJO";
//		String compIdentifier = "AuthComponent";
		ComponentObject comObj = new ComponentObject(compIdentifier, "1.1",
				ALGORITHM_TYPE, CONCURRENT_VERSION, staticDeps, null ,implType);
//		comObj.setCompCommInfo(new CompCommInfo("AuthComponent", "10.0.2.15", 18080));
		nodeMgr.addComponentObject(compIdentifier, comObj);
		CompLifecycleManagerImpl compLifecycleMgr = new CompLifecycleManagerImpl(comObj);
		nodeMgr.setCompLifecycleManager(compIdentifier, compLifecycleMgr);
//		CompLifecycleManagerImpl compLifecycleMgr = CompLifecycleManagerImpl.getInstance(compIdentifier);
//		compLifecycleMgr.setNode(node);
		nodeMgr.setTuscanyNode(node);
	}

	@Test
	public void testGetInfos() throws ContributionReadException,
			ValidationException, ActivationException {
//		assertEquals("127.0.0.1", CommServerManager.getInstance().getInfos("AuthComponent").getIp());
		assertEquals(18080, CommServerManager.getInstance().getInfos("AuthComponent").getPort());
	}

}
