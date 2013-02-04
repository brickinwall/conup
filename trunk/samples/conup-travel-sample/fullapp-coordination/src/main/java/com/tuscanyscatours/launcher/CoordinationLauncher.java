package com.tuscanyscatours.launcher;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.Search;
import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.coordination.Coordination;
import com.tuscanyscatours.shoppingcart.CartInitialize;
import com.tuscanyscatours.travelcatalog.TravelCatalogSearch;
import com.tuscanyscatours.tripbooking.TripBooking;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class CoordinationLauncher {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting coordination node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(CoordinationLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-coordination",
				"fullapp-coordination.composite");
		System.out.println("fullapp-coordination.composite is ready!");

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
		FileWriter fw;
		String resultFileName = "expResult.csv"; // "expResult.expResultNoUpdate"
		int totalExecution = 400;
		Random random = new Random(System.nanoTime());
		fw = new FileWriter(resultFileName);
		fw.write("#Quiescence:Blocking, Execution time(unit:ms)\n");
		fw.close();
		for (int i = 0; i < totalExecution; i++) {
			new CoordinationVisitorThread(node, resultFileName, i + 1,
					totalExecution).start();
			Thread.sleep((long) expRandom(random, (float) 1 / 2000));
			if (i == 200) {
				testUpdate();
			}
		}
	}
	
	private static float expRandom(Random random, float lambda) {
		float randomFloat = (float) (-Math.log(1-random.nextFloat()) / lambda);
		return randomFloat;
	}
	
	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "Bank";
				int port1 = 22313;
				String baseDir1 = "/home/rgc";
				String classFilePath1 = "com.tuscanyscatours.bank.impl.BankImpl";
				String contributionUri1 = "fullapp-bank";
				String compsiteUri1 = "bank.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}

	private static void testUpdateShoppingCart() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "ShoppingCart";
				int port1 = 22307;
				String baseDir1 = "/home/rgc";
				String classFilePath1 = "com.tuscanyscatours.shoppingcart.impl.ShoppingCartImpl";
				String contributionUri1 = "fullapp-shoppingcart";
				String compsiteUri1 = "fullapp-shoppingcart.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}
	
	private static void testUpdateCar() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "CarPartner";
				int port1 = 22303;
				String baseDir1 = "/home/rgc";
				String classFilePath1 = "com.tuscanyscatours.car.impl.CarImpl";
				String contributionUri1 = "fullapp-bespoketrip";
				String compsiteUri1 = "fullapp-bespoketrip.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}
}
