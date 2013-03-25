package com.tuscanyscatours.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.ext.utils.experiments.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.TimelinessRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.OverheadExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.TimelinessExp;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class CoordinationLauncher {
	private static Logger LOGGER = Logger.getLogger(CoordinationLauncher.class.getName());
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting coordination node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(CoordinationLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-coordination",
				"fullapp-coordination.composite");
		LOGGER.fine("fullapp-coordination.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("TravelCatalog", "oldVersion");
//		nodeMgr.getDynamicDepManager("TravelCatalog").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("TravelCatalog").setNode(node);
		CommServerManager.getInstance().start("TravelCatalog");

		nodeMgr.loadConupConf("TripBooking", "oldVersion");
//		nodeMgr.getDynamicDepManager("TripBooking").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("TripBooking").setNode(node);
		CommServerManager.getInstance().start("TripBooking");
		
		nodeMgr.loadConupConf("Coordination", "oldVersion");
//		nodeMgr.getDynamicDepManager("Coordination").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("Coordination").setNode(node);
		CommServerManager.getInstance().start("Coordination");

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
		 accessServices(node);
	}
	
	public static void accessServices(Node node) throws Exception{
		int accessTimes = 40;		//total request
		int rqstInterval = 200;
		String targetComp = "CurrencyConverter";	//target component for update
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
				CountDownLatch accessCountDown = new CountDownLatch(accessTimes);
				for (int i = 0; i < accessTimes; i++) {
					new CoordinationVisitorThread(node, accessCountDown).start();
					Thread.sleep(rqstInterval);
				}
				accessCountDown.await();
				break;
			case update:
				String toVer = null;
				if( input.length == 3){
					targetComp = input[1].trim();
					toVer = input[2].trim();
//					System.out.println("update " + targetComp + " " + toVer);
					TravelCompUpdate.update(targetComp, toVer);
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
					new CoordinationVisitorThread(node, 0 , i + 1).start();
					Thread.sleep(rqstInterval);
					if(updatePoints.get(i) != null){
						TravelCompUpdate.update(targetComp, updatePoints.get(i));
					}
				}
				break;
			case disruption:
				targetComp = input[1].trim();
				rqstInterval = new Integer(input[2].trim());
				accessTimes = new Integer(input[3].trim());

				for(int i=4; i<input.length; i+=2){
					int point = new Integer(input[i].trim());
					if(point < accessTimes)
						updatePoints.put(point, input[i+1]);
				}
				
				int warmUpTimes = 100;
				CountDownLatch warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					Thread.sleep(rqstInterval);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				DisruptionExp disExp = DisruptionExp.getInstance();
//				double normalBaseLine = 0.0;
//				int n = 4;
//				for(int round = 0; round < n; round++){
//					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
//
//					CountDownLatch normalCountDown = new CountDownLatch(accessTimes);
//					for (int i = 0; i < accessTimes; i++) {
//						new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
//						Thread.sleep(rqstInterval);
//					}
//					normalCountDown.await();
//					
//					normalBaseLine += resTimeRec.getTotalNormalResTime();
//					Thread.sleep(2000);
//				}
//				normalBaseLine = normalBaseLine / n;
//				LOGGER.info("normalBaseLine:" + normalBaseLine);
//				
//				for(int round = 0; round < 50; round++){
//					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
//					CountDownLatch updateCountDown = new CountDownLatch(accessTimes);
//					for (int i = 0; i < accessTimes; i++) {
//						new CoordinationVisitorThread(node, updateCountDown, i + 1, resTimeRec, "update").start();
//						if(updatePoints.get(i) != null){
//							TravelCompUpdate.update(targetComp, updatePoints.get(i));
//						}
//						Thread.sleep(rqstInterval);
//					}
//					updateCountDown.await();
//					
//					String data = normalBaseLine + "," + resTimeRec.getTotalUpdateResTime() + "," + (long)(resTimeRec.getTotalUpdateResTime() - normalBaseLine) + "\n";
//					disExp.writeToFile(data);
//					
//					Thread.sleep(2000);
//				}
				
				for(int round = 0; round < 50; round++){
					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
					CountDownLatch updateCountDown = new CountDownLatch(accessTimes);
					for (int i = 0; i < accessTimes; i++) {
						new CoordinationVisitorThread(node, updateCountDown, i + 1, resTimeRec, "update").start();
						if(updatePoints.get(i) != null){
							TravelCompUpdate.update(targetComp, updatePoints.get(i));
						}
						Thread.sleep(rqstInterval);
					}
					updateCountDown.await();
					
					
					Thread.sleep(3000);

					CountDownLatch normalCountDown = new CountDownLatch(accessTimes);
					for (int i = 0; i < accessTimes; i++) {
						new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
						Thread.sleep(rqstInterval);
					}
					normalCountDown.await();
					
					String data = resTimeRec.getTotalNormalResTime() + "," + resTimeRec.getTotalUpdateResTime() + "," + (long)(resTimeRec.getTotalUpdateResTime() - resTimeRec.getTotalNormalResTime()) + "\n";
					disExp.writeToFile(data);
					
					Thread.sleep(3000);
				}
				
				disExp.close();
				break;
			case timeliness:
				targetComp = input[1].trim();
				rqstInterval = new Integer(input[2].trim());
				accessTimes = new Integer(input[3].trim());

				for(int i=4; i<input.length; i+=2){
					int point = new Integer(input[i].trim());
					if(point < accessTimes)
						updatePoints.put(point, input[i+1]);
				}
				
				warmUpTimes = 100;
				warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					Thread.sleep(rqstInterval);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				TimelinessRecorder timelinessRec = new TimelinessRecorder();
				for(int round = 0; round < 50; round++){
					CountDownLatch updateCountDown = new CountDownLatch(accessTimes);
					for (int i = 0; i < accessTimes; i++) {
						new CoordinationVisitorThread(node, updateCountDown, i + 1, timelinessRec).start();
						if(updatePoints.get(i) != null){
							TravelCompUpdate.update(targetComp, updatePoints.get(i));
						}
						Thread.sleep(rqstInterval);
					}
					updateCountDown.await();
					
					Thread.sleep(2000);
				}
				List<Double> allUpdateTime = new TimelinessRecorder().getAllUpdateCostTime();
				TimelinessExp.getInstance().writeToFile(allUpdateTime);
				break;
			case overhead:
				rqstInterval = new Integer(input[1].trim());
				accessTimes = new Integer(input[2].trim());
				
				warmUpTimes = 100;
				warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					Thread.sleep(rqstInterval);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				OverheadExp overExp = OverheadExp.getInstance();
				List<Double> tuscanyBaseLine = new ArrayList<Double>();
				for(int round = 0; round < 50; round++){
					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();

					CountDownLatch normalCountDown = new CountDownLatch(accessTimes);
					for (int i = 0; i < accessTimes; i++) {
						new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
						Thread.sleep(rqstInterval);
					}
					normalCountDown.await();
					
					tuscanyBaseLine.add(resTimeRec.getTotalNormalResTime());
					Thread.sleep(2000);
				}
				String data = "";
				for(int m = 0; m < tuscanyBaseLine.size(); m++){
					data += tuscanyBaseLine.get(m) + "\n";
				}
						
				overExp.writeToFile(data);
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
		
		
//		int num = 0;
//		for(int i = 0; i < num; i++){
//			new CoordinationVisitorThread(node).start();
//			Thread.sleep(400);
//			if(i == 15){
//				TravelCompUpdate.updateCurrencyToVerOne();
//				Thread.sleep(1000);
//			}
			
//			if(i == 35){
//				TravelCompUpdate.updateCurrencyToVerTwo();
//				Thread.sleep(1000);
//			}
//			Experiment.getInstance().close();
//		}
	}
	
	private static void printHelp(){
		System.out.println();
		System.out.println("experiment of disruption ");
		System.out.println("	[usage] disruption CurrencyConverter 500 50 25 VER_ONE\n");
		
		System.out.println("experiment of timeliness ");
		System.out.println("	[usage] timeliness CurrencyConverter 500 50 25 VER_ONE\n");
		
		System.out.println("experiment of overhead ");
		System.out.println("	[usage] overhead 500 50 \n");
		
		System.out.println("access specified times without executing update, e.g., ");
		System.out.println("	[usage] access 500 50");
		System.out.println("	[behavior] access the component 50 times, and the thread sleep 500ms before sending each request");
		System.out.println("update specified component without accessing it. e.g., ");
		System.out.println("	[usage] update CurrencyConverter VER_ONE");
		System.out.println("	[behavior] update component 'CurrencyConverter' to VER_ONE");
		System.out.println("update a component while requests ongoing, e.g., ");
		System.out.println("	[usage] updateAt CurrencyConverter 500 50 25 VER_ONE");
		System.out.println("	[behavior] access 50 times, and the thread sleep 500ms before sending each request. " +
				" Meanwhile, update component 'CurrencyConverter' to VER_ONE at 25th request");
		
		System.out.println("	[usage] updateAt CurrencyConverter 200 50 15 VER_ONE 35 VER_TWO");
		System.out.println("	[behavior] access 50 times, and the thread sleep 200ms before sending each request. " +
				" Meanwhile, update component 'CurrencyConverter' to VER_ONE at 15th request and to VER_TWO at 35th request");
		System.out.println("'help' shows supported commands.");
		System.out.println();
	}
	
}
