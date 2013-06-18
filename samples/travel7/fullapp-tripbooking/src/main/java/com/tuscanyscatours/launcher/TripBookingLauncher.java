package com.tuscanyscatours.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class TripBookingLauncher {
	private static Logger LOGGER = Logger.getLogger(TripBookingLauncher.class.getName());
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting fullapp-tripbooking node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(TripBookingLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-tripbooking",
				"fullapp-tripbooking.composite");
		LOGGER.fine("fullapp-tripbooking.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		
		nodeMgr.loadConupConf("TripBooking", "oldVersion");
//		nodeMgr.getDynamicDepManager("TripBooking").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("TripBooking").setNode(node);
		CommServerManager.getInstance().start("TripBooking");
		
		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
//		 accessServices(node);
	}
	
	public static void accessServices(Node node) throws Exception{
//		try {
//			
//			TripLeg tripLeg = new TripLeg("", "", "FLR", "06/12/09", "06/12/09", "2");
//			System.out
//				.println("\nTry to access TravelCatalogSearch#service-binding(TravelCatalogSearch/TravelCatalogSearch):");
//			TripBooking booking = node.getService(TripBooking.class, "TripBooking/TripBooking");
//			LOGGER.fine("\t" + "booking.search(tripLeg)=" + booking.search(tripLeg));
//
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
//		}
	}
	
}
