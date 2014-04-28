package com.tuscanyscatours.experiments.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.utils.experiments.Experiment;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;
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
				RemoteConfigTool rcs = new RemoteConfigTool();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String baseDir = "/home/valerio/workspace/conUp/tuscany-sca/updated-components";
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				
				String ip = "10.0.2.15";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port,
						targetIdentifier, protocol, baseDir, classFilePath,
						contributionUri, null, compsiteUri);
				rcs.update(rcc);
				
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
//		CompLifecycleManager.getInstance(compIdentifier).setNode(node);
		ComponentObject compObj = nodeMgr.getComponentObject(compIdentifier);
		CompLifecycleManagerImpl compLifecycleManager = new CompLifecycleManagerImpl(compObj);
//		compLifecycleManager.setNode(node);
		nodeMgr.setTuscanyNode(node);
		CommServerManager.getInstance().start(compIdentifier);
	}
	
}
