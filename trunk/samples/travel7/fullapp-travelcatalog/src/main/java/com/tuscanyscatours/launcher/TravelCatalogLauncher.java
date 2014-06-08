package com.tuscanyscatours.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.travelcatalog.TravelCatalogSearch;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
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
		ComponentObject travelCatalogCompObj = nodeMgr.getComponentObject("TravelCatalog");
		CompLifecycleManagerImpl travelCatalogCompLifecycleManager = new CompLifecycleManagerImpl(travelCatalogCompObj);
		nodeMgr.setTuscanyNode(node);
		nodeMgr.setCompLifecycleManager("TravelCatalog", travelCatalogCompLifecycleManager);
		TxLifecycleManager travelCatalogTxLifecycleMgr = new TxLifecycleManagerImpl(travelCatalogCompObj);
		nodeMgr.setTxLifecycleManager("TravelCatalog", travelCatalogTxLifecycleMgr);
		TxDepMonitor travelCatalogTxDepMonitor = new TxDepMonitorImpl(travelCatalogCompObj);
		nodeMgr.setTxDepMonitor("TravelCatalog", travelCatalogTxDepMonitor);
		
		DynamicDepManager travelCatalogDepMgr = NodeManager.getInstance().getDynamicDepManager(travelCatalogCompObj.getIdentifier());
		travelCatalogDepMgr.setTxLifecycleMgr(travelCatalogTxLifecycleMgr);
		travelCatalogDepMgr.setCompLifeCycleMgr(travelCatalogCompLifecycleManager);
		
		nodeMgr.getOndemandSetupHelper("TravelCatalog");
		UpdateManager travelCatalogUpdateMgr = nodeMgr.getUpdateManageer("TravelCatalog");
		CommServerManager.getInstance().start("TravelCatalog");
		ServerIoHandler travelCatalogServerIoHandler = CommServerManager.getInstance().getCommServer("TravelCatalog").getServerIOHandler();
		travelCatalogServerIoHandler.registerUpdateManager(travelCatalogUpdateMgr);

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
