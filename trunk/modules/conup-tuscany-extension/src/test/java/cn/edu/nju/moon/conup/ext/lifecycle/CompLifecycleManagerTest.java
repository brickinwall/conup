package cn.edu.nju.moon.conup.ext.lifecycle;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.test.BufferTestConvention;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 */
public class CompLifecycleManagerTest {
	private static Logger LOGGER = Logger.getLogger(CompLifecycleManagerTest.class.getName());
	CompLifecycleManagerImpl clm = null;
	Node node = null;

	@Before
	public void setUp() throws Exception {
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		node = runtime.createNode();
		String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
		String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		NodeManager nodeMgr = NodeManager.getInstance();
		Set<String> staticDeps = new HashSet<String>();
		String implType = BufferTestConvention.JAVA_POJO_IMPL_TYPE;
		String compIdentifier = "HelloworldComponent";
		ComponentObject comObj = new ComponentObject(compIdentifier, "1.1", ALGORITHM_TYPE, CONCURRENT_VERSION, staticDeps,null , implType);
		nodeMgr.addComponentObject(compIdentifier, comObj);
		CompLifecycleManagerImpl compLifecycleMgr = new CompLifecycleManagerImpl(comObj);
		nodeMgr.setCompLifecycleManager(compIdentifier, compLifecycleMgr);
//		CompLifecycleManagerImpl compLifecycleMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifier);
		compLifecycleMgr.setNode(node);
		clm = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(compIdentifier);
		
	}
	
	@After
	public void tearDown() throws Exception {
		node.stop();
	}

	@Test
	public void testUninstall() throws ContributionReadException, ValidationException, ActivationException {
//		String domainURI = null;
//		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
//		Node node = runtime.createNode(domainURI);
//		node.installContribution("src/test/resources/sample-helloworld.jar");
//		node.startComposite("sample-helloworld", "helloworld.composite");
		clm.install("sample-helloworld", "src/test/resources/sample-helloworld.jar");
		assertTrue(clm.uninstall("sample-helloworld"));
//		Contribution contribution = node.getContribution("sample-helloworld");
		
//		TuscanyRuntime runtime2 = TuscanyRuntime.newInstance();
//		Node node2 = runtime.createNode(domainURI);
//		contribution = node2.getContribution("sample-helloworld");
//		LOGGER.fine(node2.getStartedCompositeURIs());
//		assertNotNull(contribution);
		
//		clm.uninstall(null, "helloworld");
//		try{
//			contribution = node.getContribution("helloworld");
//		} catch (IllegalArgumentException e){
//			
//		}
//		assertNull(contribution);
	}

	@Test
	public void testInstall() throws ContributionReadException, ValidationException, ActivationException {
		assertTrue(clm.install("sample-helloworld", "src/test/resources/sample-helloworld.jar"));
		assertTrue(clm.install("conup-sample-hello-auth", "src/test/resources/conup-sample-hello-auth.jar"));
		
		Node node = clm.getNode();
		for(Endpoint ep : ((NodeImpl)node).getDomainRegistry().getEndpoints()){
			LOGGER.fine("\t" + ep + ":" );
			LOGGER.fine("\t\t" + "component=" + ep.getComponent());
			LOGGER.fine("\t\t" + "service=" + ep.getService());
			LOGGER.fine("\t\t" + "isRemote=" + ep.isRemote());
			LOGGER.fine("\t\t" + "isAsyncInvocation=" + ep.isAsyncInvocation());
			LOGGER.fine("\t\t" + "requiredIntents=" + ep.getRequiredIntents());
			LOGGER.fine("\t\t" + "policySets=" + ep.getPolicySets());
			LOGGER.fine("\t\t" + "binding=" + ep.getBinding());
			LOGGER.fine("\t\t" + "deployedUri=" + ep.getDeployedURI());
		}
		LOGGER.fine("<-----stop print endpoint in install...");
		clm.uninstall("sample-helloworld");
		clm.uninstall("conup-sample-hello-auth");
		node.stop();
	}

	@Test
	public void testStop() {
		String contributionURI = "sample-helloworld";
		clm.install(contributionURI, "src/test/resources/sample-helloworld.jar");
		
		Node node = clm.getNode();
		for(Endpoint ep : ((NodeImpl)node).getDomainRegistry().getEndpoints()){
			LOGGER.fine("\t" + ep + ":" );
			LOGGER.fine("\t\t" + "component=" + ep.getComponent());
			LOGGER.fine("\t\t" + "service=" + ep.getService());
			LOGGER.fine("\t\t" + "isRemote=" + ep.isRemote());
			LOGGER.fine("\t\t" + "isAsyncInvocation=" + ep.isAsyncInvocation());
			LOGGER.fine("\t\t" + "requiredIntents=" + ep.getRequiredIntents());
			LOGGER.fine("\t\t" + "policySets=" + ep.getPolicySets());
			LOGGER.fine("\t\t" + "binding=" + ep.getBinding());
			LOGGER.fine("\t\t" + "deployedUri=" + ep.getDeployedURI());
		}
		assertTrue(clm.stop(contributionURI));
		node.stop();
	}

	@Test
	public void testUpdate() {
//		NodeManager nodeMgr;
//		Node node;
//		TokenService tokenService = null;
//		CompLifecycleManager lcMgr;
//		DynamicDepManager depMgr;
//		String compIdentifier = "AuthComponent";
//		
//		nodeMgr = NodeManager.getInstance();
//		node = TuscanyRuntime.newInstance().createNode("default");
//		try {
//			//install and start contribution
//			node.installContribution("conup-sample-hello-auth", 
//					"src/test/resources/conup-sample-hello-auth.jar", null, null);
//			node.startComposite("conup-sample-hello-auth", "auth.composite");
//			
//			//add components to NodeManager
//			Map<String, DeployedComposite> startedComposites = ((NodeImpl)node).getStartedComposites();
//			DeployedComposite dc = startedComposites.get("conup-sample-hello-auth" + "/" + "auth.composite");
//			Composite composite = dc.getBuiltComposite();
//			List<Component> compsInNode = composite.getComponents();
//			for(Component comp : compsInNode){
//				String compId = comp.getName();
//				String compVer = BufferTestConvention.OLD_VERSION;
//				String algorithmType = BufferTestConvention.CONSISTENCY_ALGORITHM;
//				String freeness = BufferTestConvention.CONCURRENT_VERSION;
//				Set<String> staticDeps = null;
//				String implType = BufferTestConvention.JAVA_POJO_IMPL_TYPE;
//				ComponentObject compObj;
//				compObj = new ComponentObject(compId, compVer, algorithmType, 
//						freeness, staticDeps, null, implType);
//				if(nodeMgr.getComponentObject(compId) == null){
//					nodeMgr.addComponentObject(compId, compObj);
//				}
//			}//END FOR
//			
//			//add node to CompLifecycleManager
//			lcMgr = CompLifecycleManager.getInstance(compIdentifier);
//			lcMgr.setNode(node);
//			
//			//access TokenService before dynamic update
//			tokenService = node.getService(TokenService.class, 
//					"AuthComponent#service-binding(TokenService/TokenService)");
//			String tokenResult = tokenService.getToken("nju,cs");
//			assertEquals(tokenResult, 
//					BufferTestConvention.OLD_VERSION_HELLO_AUTH_TOKEN_RESULT);
//			
//			//update
//			String baseDir = "src/test/resources/helloAuthNewVersionCompImp";
//			String classPath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
//			String contributionURI = "conup-sample-hello-auth";
//			String compositeURI = "auth.composite";
//			Scope scope = null;
//			DynamicUpdateContext updateCtx;
//			lcMgr = CompLifecycleManager.getInstance(compIdentifier);
////			depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
//			depMgr = new DynamicDepManagerImpl();
//			depMgr.setAlgorithm(new VersionConsistencyImpl());
////			nodeMgr.setDynamicDependencyMgr(nodeMgr.getComponentObject(compIdentifier), depMgr);
//			
//			depMgr.ondemandSetupIsDone();
//			String payload = TuscanyPayloadCreator.createPayload(TuscanyOperationType.UPDATE, compIdentifier, baseDir, classPath, contributionURI, compositeURI);
////			lcMgr.update(baseDir, classPath, contributionURI, compositeURI, compIdentifier);
//////			lcMgr.update(payload);
////			
////			//to test whether the new version impl is loaded correctly
////			updateCtx = lcMgr.getUpdateCtx();
////			TokenService authInstance;
////			authInstance = (TokenService)(updateCtx.getOldVerClass().newInstance());
////			assertEquals(BufferTestConvention.OLD_VERSION_HELLO_AUTH_TOKEN_RESULT, authInstance.getToken("nju,cs"));
////			authInstance = (TokenService)(updateCtx.getNewVerClass().newInstance());
////			LOGGER.fine(authInstance.getToken("nju,cs"));
////			try {
////				Thread.sleep(2000);
////			} catch (InterruptedException e) {
////				e.printStackTrace();
////			}
////			//access service after dynamic update
////			assertEquals(BufferTestConvention.NEW_VERSION_HELLO_AUTH_TOKEN_RESULT, authInstance.getToken("nju,cs"));
////			assertEquals(updateCtx.isLoaded(), true);
//			
//		} catch (ContributionReadException | ValidationException e) {
//			e.printStackTrace();
//		} catch (ActivationException e) {
//			e.printStackTrace();
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
//		} 
//		node.uninstallContribution("conup-sample-hello-auth");
//		node.stop();
	}

}
