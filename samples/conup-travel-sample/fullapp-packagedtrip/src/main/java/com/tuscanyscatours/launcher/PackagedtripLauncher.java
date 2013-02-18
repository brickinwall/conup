package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.Search;
import com.tuscanyscatours.common.TripLeg;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class PackagedtripLauncher {
	private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static void main(String[] args) throws Exception{
		
		LOGGER.setLevel(Level.OFF);
		
		LOGGER.fine("Starting packagedtrip node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(PackagedtripLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-packagedtrip",
				"fullapp-packagedtrip.composite");
		LOGGER.fine("fullapp-packagedtrip.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("TripPartner", "oldVersion");
//		nodeMgr.getDynamicDepManager("TripPartner").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("TripPartner").setNode(node);
		CommServerManager.getInstance().start("TripPartner");

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
//		 accessServices(node);
	}
	public static void accessServices(Node node){
		try {
			
			TripLeg tripLeg = new TripLeg("", "", "FLR", "06/12/09", "06/12/09", "2");
			System.out
				.println("\nTry to access TripPartner#service-binding(Search/Search):");
			Search carSearch = node.getService(Search.class,
					"TripPartner#service-binding(Search/Search)");
			LOGGER.fine("\t" + "carSearch.searchSynch(tripLeg)=" + carSearch.searchSynch(tripLeg));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
}
