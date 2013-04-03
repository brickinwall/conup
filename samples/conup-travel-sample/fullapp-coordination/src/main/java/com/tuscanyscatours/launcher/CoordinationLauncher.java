package com.tuscanyscatours.launcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.OverheadExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;
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
		int accessTimes = 0;		//total request
		int rqstInterval = 0;
		Map<Integer, String> updatePoints = new TreeMap<Integer, String>();
		
		ExpXMLUtil xmlUtil = new ExpXMLUtil();
		String algorithm = xmlUtil.getAlgorithmConf();
		algorithm = algorithm.substring(0, algorithm.indexOf("_ALGORITHM"));
		ExpSetting expSetting = xmlUtil.getExpSetting();
		rqstInterval = expSetting.getRqstInterval();
		int nThreads = expSetting.getnThreads();
		int threadId = expSetting.getThreadId();
		int indepRun = expSetting.getIndepRun();
		String targetComp = expSetting.getTargetComp();	//target component for update
		
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
				
				CountDownLatch updateAtCountDown = new CountDownLatch(accessTimes);
				for (int i = 0; i < accessTimes; i++) {
					new CoordinationVisitorThread(node, 0 , i + 1, updateAtCountDown).start();
					Thread.sleep(rqstInterval);
					if(updatePoints.get(i) != null){
						TravelCompUpdate.update(targetComp, updatePoints.get(i));
					}
				}
				break;
			case disruption:
				
				int warmUpTimes = 400;
				CountDownLatch warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					if(i == 300){
						TravelCompUpdate.update();
					}
					Thread.sleep(200);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				DisruptionExp disExp = DisruptionExp.getInstance();
				
				for(int round = 0; round < indepRun; round++){
					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
					System.out.println("-------------round " + round + "--------------");
					
					CountDownLatch normalCountDown = new CountDownLatch(nThreads);
					for (int i = 0; i < nThreads; i++) {
						new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
						Thread.sleep(rqstInterval);
					}
					normalCountDown.await();
					
					Thread.sleep(3000);

					CountDownLatch updateCountDown = new CountDownLatch(nThreads);
					for (int i = 0; i < nThreads; i++) {
						new CoordinationVisitorThread(node, updateCountDown, i + 1, resTimeRec, "update").start();
						if(i == threadId){
							disExp.setUpdateStartTime(System.nanoTime());
							TravelCompUpdate.update();
						}
						Thread.sleep(rqstInterval);
					}
					updateCountDown.await();
					
					Thread.sleep(1000);
					
					Map<Integer, Long> normalRes = resTimeRec.getNormalRes();
					Map<Integer, Long> updateRes = resTimeRec.getUpdateRes();
//					System.out.println("normalRes.size() ==" + normalRes.size());
//					System.out.println("updateRes.size() ==" + updateRes.size());
					assert normalRes.size() == nThreads;
					assert updateRes.size() == nThreads;
					
//					for(int i = 0; i < nThreads; i++){
//						String data = round + "," + (i+1) + "," + normalRes.get(i + 1) * 1e-6 + "," + updateRes.get(i+1) * 1e-6 + "\n";
//						disExp.writeToFile(data);
//					}
					double averageNormalResTime = resTimeRec.getAverageNormalResTime();
					Map<Integer, Double> disruptedTxsResTime = resTimeRec.getDisruptedTxResTime();
					Iterator<Entry<Integer, Double>> iter = disruptedTxsResTime.entrySet().iterator();
					int count = 0;
					String data = null;
					while(iter.hasNext()){
						Entry<Integer, Double> entry = iter.next();
						int curThreadId = entry.getKey();
						Double resTime = entry.getValue();
						if(count == 0)
							data = round + "," + curThreadId + "," + averageNormalResTime + "," + resTime + "," + disExp.getTimelinessTime() + "\n";
						else
							data = round + "," + curThreadId + "," + averageNormalResTime + "," + resTime + "\n";
						LOGGER.fine(data);
						disExp.writeToFile(data);
						count++;
					}
//					String data = resTimeRec.getTotalNormalResTime() + "," + resTimeRec.getTotalUpdateResTime() + "," + (long)(resTimeRec.getTotalUpdateResTime() - resTimeRec.getTotalNormalResTime()) + "\n";
//					disExp.writeToFile(data);
					
					Thread.sleep(3500);
				}
				
//				disExp.close();
				break;
			case timeliness:

				warmUpTimes = 400;
				warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					if(i == 300){
						TravelCompUpdate.update();
					}
					Thread.sleep(rqstInterval);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				TimelinessRecorder timelinessRec = new TimelinessRecorder();
				for(int round = 0; round < indepRun; round++){
					CountDownLatch updateCountDown = new CountDownLatch(nThreads);
					for (int i = 0; i < nThreads; i++) {
						new CoordinationVisitorThread(node, updateCountDown, i + 1, timelinessRec).start();
						if(i == threadId){
							TravelCompUpdate.update();
						}
						Thread.sleep(rqstInterval);
					}
					updateCountDown.await();
					
					Thread.sleep(2000);
				}
//				List<Double> allUpdateTime = timelinessRec.getAllUpdateCostTime();
//				System.out.println(allUpdateTime);
//				TimelinessExp.getInstance().writeToFile(allUpdateTime);
				
				break;
			case overhead:
				warmUpTimes = 400;
				warmCountDown = new CountDownLatch(warmUpTimes);
				for (int i = 0; i < warmUpTimes; i++) {
					new CoordinationVisitorThread(node, warmCountDown).start();
					Thread.sleep(rqstInterval);
				}
				warmCountDown.await();
				
				Thread.sleep(3000);
				
				OverheadExp overExp = OverheadExp.getInstance();
				List<Double> tuscanyBaseLine = new ArrayList<Double>();
				for(int round = 0; round < indepRun; round++){
					ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();

					CountDownLatch normalCountDown = new CountDownLatch(nThreads);
					for (int i = 0; i < nThreads; i++) {
						new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
						Thread.sleep(rqstInterval);
					}
					normalCountDown.await();
					//wait for all data store into collection
					Thread.sleep(1000);
					
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
		System.out.println("	[usage] disruption\n");
		
		System.out.println("experiment of timeliness ");
		System.out.println("	[usage] timeliness\n");
		
		System.out.println("experiment of overhead ");
		System.out.println("	[usage] overhead\n");
		
		System.out.println("access specified times without executing update, e.g., ");
		System.out.println("	[usage] access 500 50");
		System.out.println("	[behavior] access the component 50 times, and the thread sleep 500ms before sending each request");
		System.out.println("update specified component without accessing it. e.g., ");
		System.out.println("	[usage] update CurrencyConverter VER_ONE");
		System.out.println("	[behavior] update component 'CurrencyConverter' to VER_ONE");
		System.out.println("	[usage] update ShoppingCart VER_ONE");
		System.out.println("	[usage] update HotelPartner VER_ONE");
		System.out.println("	[usage] update TripPartner VER_ONE");
		System.out.println("update a component while requests ongoing, e.g., ");
		System.out.println("	[usage] updateAt CurrencyConverter 500 50 25 VER_ONE");
		System.out.println("	[behavior] access 50 times, and the thread sleep 500ms before sending each request. " +
				" Meanwhile, update component 'CurrencyConverter' to VER_ONE at 25th request");
		System.out.println("	[usage] updateAt ShoppingCart 500 50 25 VER_ONE");
		System.out.println("	[usage] updateAt CurrencyConverter 200 50 15 VER_ONE 35 VER_TWO");
		System.out.println("	[behavior] access 50 times, and the thread sleep 200ms before sending each request. " +
				" Meanwhile, update component 'CurrencyConverter' to VER_ONE at 15th request and to VER_TWO at 35th request");
		System.out.println("'help' shows supported commands.");
		System.out.println();
	}
	
}
