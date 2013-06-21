package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.car.impl.CarSearch;
import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.hotel.HotelSearch;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class BespoketripLauncher {
	private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static void main(String[] args) throws Exception{		
		
		LOGGER.setLevel(Level.OFF);
		
		LOGGER.fine("Starting bespoketrip node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(BespoketripLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-bespoketrip",
				"fullapp-bespoketrip.composite");
		LOGGER.fine("fullapp-bespoketrip.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("HotelPartner", "oldVersion");
		
		ComponentObject compObj = nodeMgr.getComponentObject("HotelPartner");
		CompLifecycleManagerImpl compLifecycleManager = new CompLifecycleManagerImpl(compObj);
		compLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("HotelPartner", compLifecycleManager);
		TxDepMonitor txDepMonitor = new TxDepMonitorImpl(compObj);
		nodeMgr.setTxDepMonitor("HotelPartner", txDepMonitor);
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObj);
		nodeMgr.setTxLifecycleManager("HotelPartner", txLifecycleMgr);
		
		DynamicDepManager hotelDepMgr = NodeManager.getInstance().getDynamicDepManager(compObj.getIdentifier());
		hotelDepMgr.setTxLifecycleMgr(txLifecycleMgr);
		compLifecycleManager.setDepMgr(hotelDepMgr);
		
		CommServerManager.getInstance().start("HotelPartner");

		nodeMgr.loadConupConf("FlightPartner", "oldVersion");
		ComponentObject flightCompObj = nodeMgr.getComponentObject("FlightPartner");
		CompLifecycleManagerImpl flightCompLifecycleManager = new CompLifecycleManagerImpl(flightCompObj);
		flightCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("FlightPartner", flightCompLifecycleManager);
		TxDepMonitor flightTxDepMonitor = new TxDepMonitorImpl(flightCompObj);
		nodeMgr.setTxDepMonitor("FlightPartner", flightTxDepMonitor);
		TxLifecycleManager flightTxLifecycleMgr = new TxLifecycleManagerImpl(flightCompObj);
		nodeMgr.setTxLifecycleManager("FlightPartner", flightTxLifecycleMgr);
		
		DynamicDepManager flightDepMgr = NodeManager.getInstance().getDynamicDepManager(flightCompObj.getIdentifier());
		flightDepMgr.setTxLifecycleMgr(flightTxLifecycleMgr);
		compLifecycleManager.setDepMgr(flightDepMgr);
		
		CommServerManager.getInstance().start("FlightPartner");
		
		nodeMgr.loadConupConf("CarPartner", "oldVersion");
		ComponentObject carCompObj = nodeMgr.getComponentObject("CarPartner");
		CompLifecycleManagerImpl carCompLifecycleManager = new CompLifecycleManagerImpl(carCompObj);
		carCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("CarPartner", carCompLifecycleManager);
		TxDepMonitor carTxDepMonitor = new TxDepMonitorImpl(carCompObj);
		nodeMgr.setTxDepMonitor("CarPartner", carTxDepMonitor);
		TxLifecycleManager carTxLifecycleMgr = new TxLifecycleManagerImpl(carCompObj);
		nodeMgr.setTxLifecycleManager("CarPartner", carTxLifecycleMgr);
		
		DynamicDepManager carDepMgr = NodeManager.getInstance().getDynamicDepManager(carCompObj.getIdentifier());
		carDepMgr.setTxLifecycleMgr(carTxLifecycleMgr);
		compLifecycleManager.setDepMgr(carDepMgr);
		
		CommServerManager.getInstance().start("CarPartner");

//		nodeMgr.getDynamicDepManager("HotelPartner").ondemandSetting();
//		nodeMgr.getDynamicDepManager("FlightPartner").ondemandSetting();
//		nodeMgr.getDynamicDepManager("CarPartner").ondemandSetting();
//		
//		nodeMgr.getDynamicDepManager("HotelPartner").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("FlightPartner").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("CarPartner").ondemandSetupIsDone();

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
//		 accessServices(node);
	}
	
	public static void accessServices(Node node){
		try {
//			LOGGER.fine("\nTry to update currency component");
//			testUpdate();
			
			TripLeg tripLeg = new TripLeg("", "", "FLR", "06/12/09", "06/12/09", "2");
			LOGGER.fine("\nTry to access CarPartner#service-binding(Search/Search):");
			CarSearch carSearch = node.getService(CarSearch.class, "CarPartner/CarSearch/CarSearch");
			LOGGER.fine("\t" + "carSearch.searchSynch(tripLeg)=" + carSearch.searchSynch(tripLeg));
//			System.out
//				.println("\nTry to access CarPartner#service-binding(Search/searchws):");
//			carSearch = node.getService(Search.class,
//					"CarPartner#service-binding(Search/searchws)");
//			LOGGER.fine("\t" + "carSearch.searchSynch(tripLeg)=" + carSearch.searchSynch(tripLeg));

			LOGGER.fine("\nTry to access HotelPartner#service-binding(Search/Search):");
            TripLeg flightTrip = new TripLeg("", "LGW", "FLR", "06/12/09", "06/12/09", "1");
            HotelSearch hotelSearch = node.getService(HotelSearch.class, "HotelPartner/HotelSearch/HotelSearch");
			LOGGER.fine("\t" + "hotelSearch.searchSynch(tripLeg)=" + hotelSearch.searchSynch(flightTrip));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "CurrencyConverter";
				int port = 11230;
				String baseDir = "/home/stone/deploy/travleSample/currencyNew";
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				rcs.update("192.168.137.133", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}

}
