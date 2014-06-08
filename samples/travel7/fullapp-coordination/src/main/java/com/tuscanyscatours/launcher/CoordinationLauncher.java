package com.tuscanyscatours.launcher;

import it.unipr.ce.dsg.deus.core.Event;
import it.unipr.ce.dsg.deus.core.InvalidParamsException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ResponseTimeRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.TimelinessRecorder;
import cn.edu.nju.moon.conup.ext.utils.experiments.CallBack;
import cn.edu.nju.moon.conup.ext.utils.experiments.CorrectnessExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.DeviationExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExperimentOperation;
import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class CoordinationLauncher {
	private static Logger LOGGER = Logger.getLogger(CoordinationLauncher.class.getName());
	private static boolean stopExp = false;
	
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

		nodeMgr.loadConupConf("Coordination", "oldVersion");
		ComponentObject coordinationCompObj = nodeMgr.getComponentObject("Coordination");
		CompLifecycleManagerImpl coordinationCompLifecycleManager = new CompLifecycleManagerImpl(coordinationCompObj);
		nodeMgr.setCompLifecycleManager("Coordination", coordinationCompLifecycleManager);
		TxLifecycleManager coordinationTxLifecycleMgr = new TxLifecycleManagerImpl(coordinationCompObj);
		nodeMgr.setTxLifecycleManager("Coordination", coordinationTxLifecycleMgr);
		TxDepMonitor coordinationTxDepMonitor = new TxDepMonitorImpl(coordinationCompObj);
		nodeMgr.setTxDepMonitor("Coordination", coordinationTxDepMonitor);
		
		DynamicDepManager coordinationDepMgr = NodeManager.getInstance().getDynamicDepManager(coordinationCompObj.getIdentifier());
		coordinationDepMgr.setTxLifecycleMgr(coordinationTxLifecycleMgr);
		coordinationDepMgr.setCompLifeCycleMgr(coordinationCompLifecycleManager);

		nodeMgr.getOndemandSetupHelper("Coordination");
		UpdateManager coordinationUpdateMgr = nodeMgr.getUpdateManageer("Coordination");
		CommServerManager.getInstance().start("Coordination");
		ServerIoHandler coordinationServerIoHandler = CommServerManager.getInstance().getCommServer("Coordination").getServerIOHandler();
		coordinationServerIoHandler.registerUpdateManager(coordinationUpdateMgr);

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
		final Scope scope = expSetting.getScope();
		final String targetComp = expSetting.getTargetComp();	//target component for update
		String ip = expSetting.getIpAddress();
		
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
					String targetComp1 = input[1].trim();
					toVer = input[2].trim();
					TravelCompUpdate.update(targetComp1, toVer, scope);
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
				
				String targetComp1 = input[1].trim();
				rqstInterval = new Integer(input[2].trim());
				accessTimes = new Integer(input[3].trim());
				
				for(int i=4; i<input.length; i+=2){
					int point = new Integer(input[i].trim());
					if(point < accessTimes)
						updatePoints.put(point, input[i+1]);
				}
				
				CountDownLatch updateAtCountDown = new CountDownLatch(accessTimes);
				for (int i = 0; i < accessTimes; i++) {
					new CoordinationVisitorThread(node, updateAtCountDown, i + 1).start();
					Thread.sleep(rqstInterval);
					if(updatePoints.get(i) != null){
						TravelCompUpdate.update(targetComp1, updatePoints.get(i), scope);
					}
				}
				break;
			case DTO:
				doDTOExp(node);
				break;
			case correctness:
				doCorrectnessExp(node, expSetting);
				break;
			case ger:
				String gerResult =TravelExpResultQuery.queryExpResult(targetComp, ExperimentOperation.GET_EXECUTION_RECORDER);
				System.out.println(gerResult);
				ExecutionRecorderAnalyzer analyzer = new ExecutionRecorderAnalyzer(gerResult);
				System.out.println("inconsistent/total: " + 
						analyzer.getInconsistentRecords() + "/" + analyzer.getTotalRecords());
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
	
	private static void doDTOExp(Node node) throws InterruptedException, InvalidParamsException {
    	ExpXMLUtil xmlUtil = new ExpXMLUtil();
    	ExpSetting expSetting = xmlUtil.getExpSetting();
    	
    	// the interval between each request thread
    	int rqstInterval = expSetting.getRqstInterval();
    	int indepRun = expSetting.getIndepRun();
    	final String targetComp = expSetting.getTargetComp();
    	final String ipAddress = expSetting.getIpAddress();
    	final Scope scope = expSetting.getScope();
    	
    	System.out.println("---------------------------------------------");
    	System.out.println("targetComp:" + targetComp);
    	System.out.println("ipAddress:" + ipAddress);
    	System.out.println("scope:" + scope);
    	System.out.println("rqstInterval:" + rqstInterval);
    	System.out.println("---------------------------------------------");
    	
    	// make request arrival as poission process
    	Event event = null;
    	int seed = 123456789;
    	Properties params = new Properties();
    	float MeanArrival = rqstInterval;
    	params.setProperty("meanArrival", Float.toString(MeanArrival));
    	ArrayList<Event> refEvents = new ArrayList<Event>();
    	MyPoissonProcess mpp = new MyPoissonProcess("myPoissonProcess", params, null, refEvents);
    	Random random = new Random(seed);
    	mpp.setRandom(random);
    	
		int warmUpTimes = 400;
		CountDownLatch warmCountDown = new CountDownLatch(warmUpTimes);
		for (int i = 0; i < warmUpTimes; i++) {
			new CoordinationVisitorThread(node, warmCountDown).start();
			if(i == 300){
				TravelCompUpdate.update(targetComp, scope);
			}
			Thread.sleep(200);
		}
		warmCountDown.await();
		
		warmUpTimes = 200;
		warmCountDown = new CountDownLatch(warmUpTimes);
		for (int i = 0; i < warmUpTimes; i++) {
			new CoordinationVisitorThread(node, warmCountDown).start();
			if(i == 100){
				TravelCompUpdate.update(targetComp, scope);
			}
			Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
		}
		warmCountDown.await();
		
		Thread.sleep(3000);
		final DisruptionExp disExp = DisruptionExp.getInstance();
		disExp.setUpdateIsDoneCallBack(new CallBack(){
			@Override
			public void callback(){
				stopExp = true;
			}
		});
		DeviationExp devExp = DeviationExp.getInstance();

		for(int round = 0; round < indepRun; round++){
			ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
			System.out.println("-------------round " + round + "--------------");
			
			TimerTask sendUpdateTask = new TimerTask(){
				@Override
				public void run(){
					System.out.println("start to send update command " + new Date());
					disExp.setUpdateStartTime(System.nanoTime());
					TravelCompUpdate.update(targetComp, scope);
				}
			};
			Timer sendUpdateTimer = new Timer();
			sendUpdateTimer.schedule(sendUpdateTask, 15000);
			
			TimerTask abortTask = new TimerTask(){

				@Override
				public void run() {
					if(!stopExp){
						System.out.println("excede 20s, stop to send request " + new Date());
						stopExp = true;
					}
				}
				
			};
			Timer abortTimer = new Timer();
			abortTimer.schedule(abortTask, 35000);
			
			int threadsNum = 0;
			while(!stopExp){
				new CoordinationVisitorThread(node, threadsNum + 1, resTimeRec, "update").start();
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
				threadsNum ++;
			}
			
			CountDownLatch updateCountDown = new CountDownLatch(100);
			for(int j = threadsNum; j < threadsNum + 100; j++){
				new CoordinationVisitorThread(node, updateCountDown, j + 1, resTimeRec, "update").start();
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
			}
			
			updateCountDown.await();
			
			Thread.sleep(3000);
			
			CountDownLatch normalCountDown = new CountDownLatch(threadsNum + 100);
			for (int i = 0; i < threadsNum + 100; i++) {
				new CoordinationVisitorThread(node, normalCountDown, i + 1, resTimeRec, "normal").start();
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
			}
			normalCountDown.await();
			
			
			Thread.sleep(1000);
			
			Map<Integer, Long> normalRes = resTimeRec.getNormalRes();
			Map<Integer, Long> updateRes = resTimeRec.getUpdateRes();
			// write all normal, update response to file in devation folder
			devExp.writeToFile(round, normalRes, updateRes);
			
			System.out.println("normalRes.size() ==" + normalRes.size() + " threadsNum:" + threadsNum);
			System.out.println("updateRes.size() ==" + updateRes.size() + " threadsNum:" + threadsNum);
			
			Map<Integer, Double> disruptedTxsResTime = resTimeRec.getDisruptedTxResTime();
			Iterator<Entry<Integer, Double>> iter = disruptedTxsResTime.entrySet().iterator();
			int count = 0;
			String data = null;
			while(iter.hasNext()){
				Entry<Integer, Double> entry = iter.next();
				int curThreadId = entry.getKey();
				Double resTime = entry.getValue();
				if(count == 0)
					data = round + "," + curThreadId + "," + normalRes.get(curThreadId) * 1e-6 + "," + resTime + "," + disExp.getTimelinessTime() + "\n";
				else
					data = round + "," + curThreadId + "," + normalRes.get(curThreadId) * 1e-6 + "," + resTime + "\n";
				LOGGER.fine(data);
				disExp.writeToFile(data);
				count++;
			}
			if(round == 24)
				Thread.sleep(6000);
			else
				Thread.sleep(3500);
			
			// after one round running, reset flag
			stopExp = false;
		}
		
		Thread.sleep(1000);
		disExp.close();
    	
	}
	
	private static void doCorrectnessExp(Node node, ExpSetting expSetting) throws InterruptedException, InvalidParamsException {
		CorrectnessExp correctnessExp = CorrectnessExp.getInstance();
		
		int indepRun = expSetting.getIndepRun();
		int rqstInterval = expSetting.getRqstInterval();
		final String targetComp = expSetting.getTargetComp();
		final Scope scope = expSetting.getScope();
		
		// make request arrival obey to poission process
    	Event event = null;
    	int seed = 123456789;
    	Properties params = new Properties();
    	float MeanArrival = rqstInterval;
    	params.setProperty("meanArrival", Float.toString(MeanArrival));
    	ArrayList<Event> refEvents = new ArrayList<Event>();
    	MyPoissonProcess mpp = new MyPoissonProcess("myPoissonProcess", params, null, refEvents);
    	Random random = new Random(seed);
    	mpp.setRandom(random);
		
		int warmUpTimes = 400;
		CountDownLatch warmCountDown = new CountDownLatch(warmUpTimes);
		for (int i = 0; i < warmUpTimes; i++) {
			new CoordinationVisitorThread(node, warmCountDown).start();
			if(i == 300){
				TravelCompUpdate.update(targetComp, scope);
			}
			if(i > 200)
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
			else	
				Thread.sleep(200);
		}
		warmCountDown.await();
		TravelExpResultQuery.queryExpResult(targetComp, ExperimentOperation.GET_EXECUTION_RECORDER);
		
		Thread.sleep(3000);
		
		DisruptionExp disExp = DisruptionExp.getInstance();
		final DisruptionExp anotherDisExp = disExp;
		disExp.setUpdateIsDoneCallBack(new CallBack(){
			@Override
			public void callback(){
				stopExp = true;
			}
		});

		for(int round = 0; round < indepRun; round++){
			ResponseTimeRecorder resTimeRec = new ResponseTimeRecorder();
			System.out.println("-------------round " + round + "--------------");
			
			
			TimerTask sendUpdateTask = new TimerTask(){
				@Override
				public void run(){
					System.out.println("start send update command " + new Date());
					anotherDisExp.setUpdateStartTime(System.nanoTime());
					TravelCompUpdate.update(targetComp, scope);
				}
			};
			Timer sendUpdateTimer = new Timer();
			sendUpdateTimer.schedule(sendUpdateTask, 15000);
			
			TimerTask abortTask = new TimerTask(){

				@Override
				public void run() {
					if(!stopExp){
						System.out.println("excede 20s, stop to send request " + new Date());
						stopExp = true;
					}
				}
				
			};
			Timer abortTimer = new Timer();
			abortTimer.schedule(abortTask, 25000);
			
			int threadsNum = 0;
			while(!stopExp){
				new CoordinationVisitorThread(node, threadsNum + 1, resTimeRec, "update").start();
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
				threadsNum ++;
			}
			
			CountDownLatch updateCountDown = new CountDownLatch(100);
			for(int j = threadsNum; j < threadsNum + 100; j++){
				new CoordinationVisitorThread(node, updateCountDown, j + 1, resTimeRec, "update").start();
				Thread.sleep((long) mpp.getNextTriggeringTime(event, 0));
			}
			
			updateCountDown.await();
			
			int port = 0;
			if(targetComp.equals("CurrencyConverter")){
				port = 22300;
			} else if(targetComp.equals("TripPartner")){
				port = 22304;
			} else if(targetComp.equals("HotelPartner")){
				port = 22301;
			}
//			String updateEndTime = new RemoteConfServiceImpl().getUpdateEndTime("10.0.2.15", port, targetComp, "CONSISTENCY");
//			System.out.println("updateEndTime:" + updateEndTime + " algorithm: TRANQUILLITY");
//			DisruptionExp.getInstance().setUpdateEndTime(Long.parseLong(updateEndTime));
			System.out.println("updateEndTime:" + DisruptionExp.getInstance().getUpdateEndTime() + " updateStartTime:" + DisruptionExp.getInstance().getUpdateStartTime());
			
			Thread.sleep(3000);
			
			Map<Integer, Long> updateRes = resTimeRec.getUpdateRes();
			// write all normal, update response to file in devation folder
			
			System.out.println("updateRes.size() ==" + updateRes.size() + " threadsNum:" + threadsNum);
			
			Map<Integer, Double> disruptedTxsResTime = resTimeRec.getDisruptedTxResTime();
			int disruptedTxs = disruptedTxsResTime.size();
			String gerResult =TravelExpResultQuery.queryExpResult(targetComp, ExperimentOperation.GET_EXECUTION_RECORDER);
			System.out.println("gerResult:" + gerResult);
			ExecutionRecorderAnalyzer analyzer = new ExecutionRecorderAnalyzer(gerResult);
			int totalRecords = analyzer.getTotalRecords();
			String correctnessExpData = round + ", " + analyzer.getInconsistentRecords() + ",  " + disruptedTxs + "\n";
			correctnessExp.writeToFile(correctnessExpData);
			System.out.println("inconsistent/total/disruptedTxs: " + analyzer.getInconsistentRecords() + "/" + totalRecords + "/" + disruptedTxs);
			
			Thread.sleep(3500);
			
			// after one round running, reset flag
			stopExp = false;
		}
	}
	
	private static void printHelp(){
		System.out.println();
		System.out.println("experiment of disruption ");
		System.out.println("	[usage] disruption\n");
		
		System.out.println("experiment of timeliness ");
		System.out.println("	[usage] timeliness\n");
		
		System.out.println("experiment of overhead ");
		System.out.println("	[usage] overhead\n");
		
		System.out.println("experiment of correctness ");
		System.out.println("	[usage] correctness\n");
		
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
		System.out.println("	[usage] updateAt TripPartner 500 50 25 VER_ONE");
		System.out.println("	[usage] updateAt CurrencyConverter 200 50 15 VER_ONE 35 VER_TWO");
		System.out.println("	[behavior] access 50 times, and the thread sleep 200ms before sending each request. " +
				" Meanwhile, update component 'CurrencyConverter' to VER_ONE at 15th request and to VER_TWO at 35th request");
		System.out.println("get the execution recorder ");
		System.out.println("	[usage] ger\n");
		System.out.println("'help' shows supported commands.");
		System.out.println();
	}
	
}
