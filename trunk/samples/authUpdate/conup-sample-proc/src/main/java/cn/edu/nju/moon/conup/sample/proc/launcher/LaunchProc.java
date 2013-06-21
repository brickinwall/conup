package cn.edu.nju.moon.conup.sample.proc.launcher;


import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.sample.proc.services.ProcService;
import cn.edu.nju.moon.conup.spi.complifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;

public class LaunchProc {
	private static Logger LOGGER = Logger.getLogger(LaunchProc.class.getName());
	/**
	 * distributed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting conup-sample-proc node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchProc.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-proc", "proc.composite");
        
        //add current business node to container
        
        LOGGER.fine("proc.composite ready for big business !!!");

        //initiate NodeManager
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("ProcComponent", "oldVersion");
        ComponentObject compObj = nodeMgr.getComponentObject("ProcComponent");
//        nodeMgr.getDynamicDepManager("ProcComponent").ondemandSetupIsDone();

//        CompLifecycleManagerImpl.getInstance("ProcComponent").setNode(node);
        
        CompLifecycleManagerImpl compLifecycleManager = new CompLifecycleManagerImpl(compObj);
		compLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("ProcComponent", compLifecycleManager);
		TxDepMonitor txDepMonitor = new TxDepMonitorImpl(compObj);
		nodeMgr.setTxDepMonitor("ProcComponent", txDepMonitor);
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObj);
		nodeMgr.setTxLifecycleManager("ProcComponent", txLifecycleMgr);
		
		DynamicDepManager depMgr = NodeManager.getInstance().getDynamicDepManager(compObj.getIdentifier());
		depMgr.setTxLifecycleMgr(txLifecycleMgr);
		compLifecycleManager.setDepMgr(depMgr);
        
        CommServerManager.getInstance().start("ProcComponent");
        
        //send ondemand request
//        sendOndemandRqst();
        
        //access
//        accessServices(node);
        
        System.in.read();
        LOGGER.fine("Stopping ...");
        node.stop();
        
    }
	
	private static void accessServices(Node node) throws InterruptedException {
		try {
//				LOGGER.fine("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
//				ProcService pi = node.getService(ProcService.class, "ProcComponent#service-binding(ProcService/ProcService)");
//				LOGGER.fine("\t" + "" + pi.process("nju,cs,pass", ""));
			
		while(true){
			LOGGER.fine("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
			ProcService pi = node.getService(ProcService.class, "ProcComponent/ProcService");
			LOGGER.fine("\t" + "" + pi.process("emptyExeProc", "nju,cs,pass", ""));
			Thread.sleep(50);
		}
		
//			int threadNum = 100;
//			for(int i=0; i<threadNum; i++){
//				LOGGER.fine("Try to access ProcComponent#service-binding(ProcService/ProcService)");
//				new ProcVisitorThread(node).start();
////				Thread.sleep(2000);
//			}
			
//			
//			LOGGER.fine("\nTry to access TokenComponent#service-binding(TokenService/TokenService):");
//			TokenService ts = node.getService(TokenService.class, "TokenComponent");
//			String token = ts.getToken("nju,cs");
//			LOGGER.fine("\t" + "" + token);
//			
//			LOGGER.fine("\nTry to access VerificationComponent#service-binding(VerificationService/VerificationService):");
//			VerificationService vs = node.getService(VerificationService.class, "VerificationComponent");
//			LOGGER.fine("\t" + "" + vs.verify(token));
			
//			LOGGER.fine("\nTry to access DBComponent#service-binding(DBService/DBService):");
//			DBService db = node.getService(DBService.class, "DBComponent");
//			LOGGER.fine("\t" + "" + db.dbOperation());
			
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
		compLcMgr = CompLifecycleManagerImpl.getInstance(compIdentifier);
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		ondemandHelper.ondemandSetup();
	}

}
