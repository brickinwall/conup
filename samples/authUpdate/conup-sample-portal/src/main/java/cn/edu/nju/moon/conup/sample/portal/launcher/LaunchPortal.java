package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;


import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.sample.portal.services.PortalService;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class LaunchPortal {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting conup-sample-portal node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchPortal.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-portal", "portal.composite");
        
        //add current business node to container
        
        System.out.println("portal.composite ready for big business !!!");
        
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("PortalComponent", "oldVersion");
//        nodeMgr.getDynamicDepManager("PortalComponent").ondemandSetupIsDone();
        
        CompLifecycleManager.getInstance("PortalComponent").setNode(node);
        CommServerManager.getInstance().start("PortalComponent");
        
        //launch DepRecorder
        DepRecorder depRecorder;
        depRecorder = DepRecorder.getInstance();
        
//        testUpdate();
        
        //access
        accessServices(node);
        
//        Thread.sleep(60 * 1000);
        
        //print DepRecorder
//        depRecorder.printRecorder();
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
	}
	
	private static void accessServices(Node node) throws InterruptedException {
			int threadNum = 100;
			
			Random random;
			random = new Random(System.currentTimeMillis());
			for(int i=0; i<threadNum; i++){
				System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
				new PortalVisitorThread(node).start();
	//			testUpdate();
				if(i == 25){
					testUpdate();
				}
//				if(i == 75){
//					Thread.sleep(2000);
//					testUpdateToOldVersion();
//				}
				Thread.sleep(Math.abs(random.nextInt())%10 * 60);
	//			Thread.sleep(200);
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
	
	private static void testUpdateToOldVersion() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update/old";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("114.212.85.6", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
}
