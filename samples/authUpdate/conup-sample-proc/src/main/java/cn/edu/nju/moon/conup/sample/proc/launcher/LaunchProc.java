package cn.edu.nju.moon.conup.sample.proc.launcher;


import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.sample.proc.services.ProcService;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

public class LaunchProc {
	/**
	 * distributed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting conup-sample-proc node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchProc.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-proc", "proc.composite");
        
        //add current business node to container
        
        System.out.println("proc.composite ready for big business !!!");

        //initiate NodeManager
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("ProcComponent", "oldVersion");
//        nodeMgr.getDynamicDepManager("ProcComponent").ondemandSetupIsDone();

        CompLifecycleManager.getInstance("ProcComponent").setNode(node);
        
        CommServerManager.getInstance().start("ProcComponent");
        
        //send ondemand request
//        sendOndemandRqst();
        
        //access
//        accessServices(node);
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
	private static void accessServices(Node node) throws InterruptedException {
		try {
//				System.out.println("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
//				ProcService pi = node.getService(ProcService.class, "ProcComponent#service-binding(ProcService/ProcService)");
//				System.out.println("\t" + "" + pi.process("nju,cs,pass", ""));
			
		while(true){
			System.out.println("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
			ProcService pi = node.getService(ProcService.class, "ProcComponent#service-binding(ProcService/ProcService)");
			System.out.println("\t" + "" + pi.process("emptyExeProc", "nju,cs,pass", ""));
			Thread.sleep(50);
		}
		
//			int threadNum = 100;
//			for(int i=0; i<threadNum; i++){
//				System.out.println("Try to access ProcComponent#service-binding(ProcService/ProcService)");
//				new ProcVisitorThread(node).start();
////				Thread.sleep(2000);
//			}
			
//			
//			System.out.println("\nTry to access TokenComponent#service-binding(TokenService/TokenService):");
//			TokenService ts = node.getService(TokenService.class, "TokenComponent#service-binding(TokenService/TokenService)");
//			String token = ts.getToken("nju,cs");
//			System.out.println("\t" + "" + token);
//			
//			System.out.println("\nTry to access VerificationComponent#service-binding(VerificationService/VerificationService):");
//			VerificationService vs = node.getService(VerificationService.class, "VerificationComponent#service-binding(VerificationService/VerificationService)");
//			System.out.println("\t" + "" + vs.verify(token));
			
//			System.out.println("\nTry to access DBComponent#service-binding(DBService/DBService):");
//			DBService db = node.getService(DBService.class, "DBComponent#service-binding(DBService/DBService)");
//			System.out.println("\t" + "" + db.dbOperation());
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendOndemandRqst(){
		CompLifecycleManager compLcMgr;
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		OndemandSetupHelper ondemandHelper;
		String compIdentifier = "ProcComponent";
		compLcMgr = CompLifecycleManager.getInstance(compIdentifier);
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		ondemandHelper.ondemandSetup();
	}

}
