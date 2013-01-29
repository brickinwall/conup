package cn.edu.nju.moon.conup.sample.auth.launcher;

import java.util.Random;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;


public class LaunchAuth {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting conup-sample-auth node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchAuth.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-auth", "auth.composite");
        
        //add current business node to container
        
        System.out.println("auth.composite ready for big business !!!");
        
        //initiate NodeManager
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("AuthComponent", "oldVersion");
//        nodeMgr.getDynamicDepManager("AuthComponent").ondemandSetupIsDone();

        CompLifecycleManager.getInstance("AuthComponent").setNode(node);
        
        CommServerManager.getInstance().start("AuthComponent");
        
        //access
//        accessServices(node);
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
	private static void accessServices(Node node) throws InterruptedException {
		
		int threadNum = 100;
		Random random = new Random(System.currentTimeMillis());
		for(int i=0; i<threadNum; i++){
			System.out.println("Try to access AuthComponent#service-binding(TokenService/TokenService)");
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
				rcs.update("114.212.83.140", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
}
