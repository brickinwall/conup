package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.trip.impl.TripSearch;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class PackagedtripLauncher {
	private static Logger LOGGER = Logger.getLogger(PackagedtripLauncher.class.getName());
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

		String compIdentifier = "TripPartner";
		nodeMgr.loadConupConf(compIdentifier, "oldVersion");
		ComponentObject triPartnerCompObj = nodeMgr.getComponentObject(compIdentifier);
		CompLifecycleManagerImpl triPartnerCompLifecycleManager = new CompLifecycleManagerImpl(triPartnerCompObj);
		
		nodeMgr.setTuscanyNode(node);
		nodeMgr.setCompLifecycleManager(compIdentifier, triPartnerCompLifecycleManager);
		TxLifecycleManager triPartnerTxLifecycleMgr = new TxLifecycleManagerImpl(triPartnerCompObj);
		nodeMgr.setTxLifecycleManager(compIdentifier, triPartnerTxLifecycleMgr);
		TxDepMonitor triPartnerTxDepMonitor = new TxDepMonitorImpl(triPartnerCompObj);
		nodeMgr.setTxDepMonitor(compIdentifier, triPartnerTxDepMonitor);
		
		DynamicDepManager tripPartnerDepMgr = NodeManager.getInstance().getDynamicDepManager(triPartnerCompObj.getIdentifier());
		tripPartnerDepMgr.setTxLifecycleMgr(triPartnerTxLifecycleMgr);
		tripPartnerDepMgr.setCompLifeCycleMgr(triPartnerCompLifecycleManager);
		
		OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		
		UpdateManager currencyConverterUpdateMgr = nodeMgr.getUpdateManageer(compIdentifier);
		CommServerManager.getInstance().start(compIdentifier);
		ServerIoHandler currencyConverterServerIoHandler = CommServerManager.getInstance().getCommServer(compIdentifier).getServerIOHandler();
		currencyConverterServerIoHandler.registerUpdateManager(currencyConverterUpdateMgr);
		
//		nodeMgr.getDynamicDepManager(compIdentifier).ondemandSetting();
//		nodeMgr.getDynamicDepManager(compIdentifier).ondemandSetupIsDone();

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
			TripSearch carSearch = node.getService(TripSearch.class, "TripPartner/TripSearch");
			LOGGER.info("\t" + "carSearch.searchSynch(tripLeg)=" + carSearch.searchSynch(tripLeg));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
}
