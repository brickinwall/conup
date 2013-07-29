package cn.edu.nju.moon.conup.sample.auth.launcher;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.comp.manager.UpdateManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;


public class LaunchAuth {
	private static Logger LOGGER = Logger.getLogger(LaunchAuth.class.getName());
	public static void main(String[] args) throws Exception {
		
		LOGGER.fine("Starting conup-sample-auth node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchAuth.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-auth", "auth.composite");
        
        //add current business node to container
        
        LOGGER.fine("auth.composite ready for big business !!!");
        
        //initiate NodeManager
        String compIdentifier = "AuthComponent";
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf(compIdentifier, "oldVersion");
        ComponentObject compObj = nodeMgr.getComponentObject(compIdentifier);
//        nodeMgr.getDynamicDepManager("AuthComponent").ondemandSetupIsDone();
//        CompLifecycleManagerImpl.getInstance("AuthComponent").setNode(node);
        
        CompLifecycleManagerImpl compLifecycleManager = new CompLifecycleManagerImpl(compObj);
//        compLifecycleManager.setNode(node);
        nodeMgr.setTuscanyNode(node);
        nodeMgr.setCompLifecycleManager(compIdentifier, compLifecycleManager);

        TxDepMonitor txDepMonitor = new TxDepMonitorImpl(compObj);
		nodeMgr.setTxDepMonitor(compIdentifier, txDepMonitor);
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObj);
		nodeMgr.setTxLifecycleManager(compIdentifier, txLifecycleMgr);
		
		
		DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(compObj.getIdentifier());
		depMgr.setTxLifecycleMgr(txLifecycleMgr);
		depMgr.setCompLifeCycleMgr(compLifecycleManager);
//		compLifecycleManager.setDepMgr(depMgr);
		
		OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compObj.getIdentifier());
        
		CommServerManager.getInstance().start(compIdentifier);
		UpdateManager updateMgr = new UpdateManagerImpl(compObj);
		nodeMgr.setUpdateManager(compIdentifier, updateMgr);
		ServerIoHandler serverIoHandler = CommServerManager.getInstance().getCommServer(compIdentifier).getServerIOHandler();
		serverIoHandler.registerUpdateManager(updateMgr);
        //access
        accessServices(node);
//        sendOndemandRqst();
        
        System.in.read();
        LOGGER.fine("Stopping ...");
        node.stop();
        
    }
	
	private static void accessServices(Node node) throws InterruptedException {
		
		int threadNum = 1;
		Random random = new Random(System.currentTimeMillis());
		for(int i=0; i<threadNum; i++){
			LOGGER.fine("Try to access AuthComponent#service-binding(TokenService/TokenService)");
			new AuthVisitorThread(node).start();
			
			if(i == 25 || i == 75){
				testUpdate();
				Thread.sleep(2000);
			}
			
//			Thread.sleep(Math.abs(random.nextInt()) % 10 * 80);
			Thread.sleep(200);
		}
		
	
	}
	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	public static void sendOndemandRqst() {
		CompLifeCycleManager compLcMgr;
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		OndemandSetupHelper ondemandHelper;
		String compIdentifier = "AuthComponent";
		nodeMgr = NodeManager.getInstance();
		compLcMgr = nodeMgr.getCompLifecycleManager(compIdentifier);
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		ondemandHelper.ondemandSetup();
		Set<Dependence> outDeps = depMgr.getRuntimeDeps();
		LOGGER.fine("OutDepRegistry:");
		for (Iterator iterator = outDeps.iterator(); iterator.hasNext();) {
			Dependence dependence = (Dependence) iterator.next();
			LOGGER.fine(dependence.toString());
		}

		LOGGER.fine("InDepRegistry:");
		Set<Dependence> inDeps = depMgr.getRuntimeInDeps();
		for (Iterator iterator = inDeps.iterator(); iterator.hasNext();) {
			Dependence dependence = (Dependence) iterator.next();
			LOGGER.fine(dependence.toString());
		}
	}
}
