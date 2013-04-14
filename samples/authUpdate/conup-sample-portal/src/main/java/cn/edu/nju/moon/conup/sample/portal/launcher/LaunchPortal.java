package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class LaunchPortal {
	private static Logger LOGGER = Logger.getLogger(LaunchPortal.class.getName());
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting conup-sample-portal node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchPortal.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-portal", "portal.composite");
        
        //add current business node to container
        
        LOGGER.fine("portal.composite ready for big business !!!");
        
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
        LOGGER.fine("Stopping ...");
        node.stop();
        
	}
	
	
	public static void accessServices(Node node) throws Exception{
		int accessTimes = 40;		//total request
		int rqstInterval = 1000;
		String targetComp = "AuthComponent";	//target component for update
		Map<Integer, String> updatePoints = new TreeMap<Integer, String>();
		
//		System.out.println("Pls input the command, or input 'help' for help");
		printHelp();
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNextLine()){
			String [] input = scanner.nextLine().split(" ");
			COMMANDS command = null;
			try{
				command = Enum.valueOf(COMMANDS.class, input[0].trim());
			} catch(Exception e){
				System.out.println("Unsupported command. input 'help' for help.");
				continue;
			}
			
			switch (command) {
			case access:
				if( input.length == 3 ){
					rqstInterval = new Integer(input[1].trim());
					accessTimes = new Integer(input[2].trim());
				} else{
					System.out.println("Illegal parameters for 'access'");
					break;
				}
				
//				System.out.println("accessTimes: " + rqstInterval + " " + accessTimes);
				for (int i = 0; i < accessTimes; i++) {
					new PortalVisitorThread(node, 0 ,i + 1).start();
					Thread.sleep(rqstInterval);
				}
				break;
			case update:
				String toVer = null;
				if( input.length == 3){
					targetComp = input[1].trim();
					toVer = input[2].trim();
//					System.out.println("update " + targetComp + " " + toVer);
					AuthCompUpdate.update(targetComp, toVer);
				} else{
					System.out.println("Illegal parameters for 'update'");
					break;
				}
				break;
			case updateAt:
				if(input.length<=4 || input.length%2==1){
					System.out.println("Illegal parameters for 'updateAt'");
					break;
				}
				
				targetComp = input[1].trim();
				rqstInterval = new Integer(input[2].trim());
				accessTimes = new Integer(input[3].trim());
				
				for(int i=4; i<input.length; i+=2){
					int point = new Integer(input[i].trim());
					if(point < accessTimes)
						updatePoints.put(point, input[i+1]);
				}
				
				for (int i = 0; i < accessTimes; i++) {
					new PortalVisitorThread(node, 0 , i + 1).start();
					Thread.sleep(rqstInterval);
					if(updatePoints.get(i) != null){
//						System.out.println("update " + targetComp + " at " + i);
						AuthCompUpdate.update(targetComp, updatePoints.get(i));
					}
				}
				break;
			case help:
				printHelp();
				break;
			case exit:
				return;
			default:
				System.out.println("Unsupported command. input 'help' for help.");
				break;
			}
			
		}//WHILE
		
	}
	
	private static void printHelp(){
		System.out.println();
		System.out.println("access specified times without executing update, e.g., ");
		System.out.println("	[usage] access 200 50");
		System.out.println("	[behavior] access the component 50 times, and the thread sleep 200ms before sending each request");
		System.out.println("update specified component without accessing it. e.g., ");
		System.out.println("	[usage] update AuthComponent VER_ONE");
		System.out.println("	[behavior] update component 'AuthComponent' to VER_ONE");
		System.out.println("update a component while requests ongoing, e.g., ");
		System.out.println("	[usage] updateAt AuthComponent 200 50 35 VER_ONE");
		System.out.println("	[behavior] access 50 times, and the thread sleep 200ms before sending each request. " +
				" Meanwhile, update component 'AuthComponent' to VER_ONE at 35th request");
		
		System.out.println("	[usage] updateAt AuthComponent 200 70 25 VER_ONE 65 VER_TWO");
		System.out.println("	[behavior] access 50 times, and the thread sleep 200ms before sending each request. " +
				" Meanwhile, update component 'AuthComponent' to VER_ONE at 15th request and to VER_TWO at 35th request");
		System.out.println("'help' shows supported commands.");
		System.out.println();
	}
	
//	private static void accessServices(Node node) throws InterruptedException {
//			int threadNum = 70;
//			
//			Random random;
//			random = new Random(System.currentTimeMillis());
//			for(int i=0; i<threadNum; i++){
//				LOGGER.fine("Try to access PortalComponent#service-binding(PortalService/PortalService)");
//				new PortalVisitorThread(node).start();
//	//			testUpdate();
//				if(i == 30){
//					testUpdateVersion2();
//				}
//				if(i == 60){
//					testUpdateVersion3();
//				}
//				Thread.sleep(Math.abs(random.nextInt())%10 * 60);
//	//			Thread.sleep(200);
//			}
//		}

	
}
