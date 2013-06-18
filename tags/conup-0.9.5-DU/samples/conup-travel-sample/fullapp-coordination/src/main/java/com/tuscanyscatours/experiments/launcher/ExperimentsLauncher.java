package com.tuscanyscatours.experiments.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.ext.utils.experiments.Experiment;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

import com.tuscanyscatours.launcher.CoordinationLauncher;
import com.tuscanyscatours.launcher.CoordinationVisitorThread;

public class ExperimentsLauncher {
	private static Node node;
	private static int numberOfRuns = 10;
	private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public ExperimentsLauncher() {	
		
		LOGGER.setLevel(Level.OFF);
		
		initNodeAndStart();
		
		loadConupConfAndStart("TravelCatalog", "oldVersion");
		loadConupConfAndStart("TripBooking", "oldVersion");
		loadConupConfAndStart("Coordination", "oldVersion");

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();
	}
	public static void main (String [] args) {
		ExperimentsLauncher timelinessExperiment = new ExperimentsLauncher();
		timelinessExperiment.runExperiment();
	}
	public void runExperiment () {
		for (int i = 0; i < numberOfRuns; i++) {
			accessServices(node);
		}
		Experiment.getInstance().close();
	}

	public static void accessServices(Node node) {
		for (int i = 0; i < 20; i++) {
			new CoordinationVisitorThread(node).start();
			try {
				Thread.sleep(200);
				if (i == 15) {
					testUpdate();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
	}

	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
				String targetIdentifier1 = "CurrencyConverter";
				int port1 = 22300;
				String baseDir1 = "/home/valerio/workspace/conUp/tuscany-sca/updated-components";
				String classFilePath1 = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri1 = "fullapp-currency";
				String compsiteUri1 = "fullapp-currency.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1,
						"CONSISTENCY", baseDir1, classFilePath1,
						contributionUri1, compsiteUri1);
			}
		});
		thread.start();
	}
	
	private static void initNodeAndStart() {
		LOGGER.fine("Starting coordination node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(CoordinationLauncher.class);

		try {
			node.installContribution(contributionURL);
			node.startComposite("fullapp-coordination",	"fullapp-coordination.composite");
			LOGGER.fine("fullapp-coordination.composite is ready!");
		
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	private static void loadConupConfAndStart(String compIdentifier, String versionNum) {
		NodeManager nodeMgr= NodeManager.getInstance();
		nodeMgr.loadConupConf(compIdentifier, versionNum);
		CompLifecycleManager.getInstance(compIdentifier).setNode(node);
		CommServerManager.getInstance().start(compIdentifier);
	}
	
}
