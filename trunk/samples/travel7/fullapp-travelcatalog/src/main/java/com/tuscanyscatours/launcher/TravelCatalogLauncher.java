package com.tuscanyscatours.launcher;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.travelcatalog.TravelCatalogSearch;
import com.tuscanyscatours.trip.impl.TripSearch;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExperimentOperation;
import cn.edu.nju.moon.conup.ext.utils.experiments.utils.ExpXMLUtil;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class TravelCatalogLauncher {
	private static Logger LOGGER = Logger.getLogger(TravelCatalogLauncher.class.getName());
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting travelcatalog node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(TravelCatalogLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-travelcatalog",
				"fullapp-travelcatalog.composite");
		LOGGER.fine("fullapp-travelcatalog.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("TravelCatalog", "oldVersion");
//		nodeMgr.getDynamicDepManager("TravelCatalog").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("TravelCatalog").setNode(node);
		CommServerManager.getInstance().start("TravelCatalog");

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
//		 accessServices(node);
	}
	
	public static void accessServices(Node node) throws Exception{
		try {
			
			TripLeg tripLeg = new TripLeg("", "", "FLR", "06/12/09", "06/12/09", "2");
			System.out
				.println("\nTry to access TravelCatalogSearch#service-binding(TravelCatalogSearch/TravelCatalogSearch):");
			TravelCatalogSearch catalogSearch = node.getService(TravelCatalogSearch.class, "TravelCatalogSearch/TravelCatalogSearch");
			LOGGER.fine("\t" + "catalogSearch.search(tripLeg)=" + catalogSearch.search(tripLeg));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
}
