package cn.edu.nju.moon.conup.sample.db.launcher;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.sample.db.services.DBService;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

public class LaunchDB {
	private static Logger LOGGER = Logger.getLogger(LaunchDB.class.getName());
	/**
	 * distributed.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting conup-sample-db node ....");
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(LaunchDB.class);

		// domain uri
		String domainUri = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		// create Tuscany node
		Node node = runtime.createNode(domainUri);
		node.installContribution(contributionURL);
		node.startComposite("conup-sample-db", "db.composite");

		// add current business node to container

		LOGGER.fine("db.composite ready for big business !!!");

		// initiate NodeManager
		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("DBComponent", "oldVersion");
		// ComponentObject compObj = nodeMgr.getComponentObject("DBComponent");
		// LOGGER.fine(compObj.getStaticDeps() + "\n" +
		// compObj.getStaticInDeps() + "\n" + compObj.getAlgorithmConf());

		CompLifecycleManager.getInstance("DBComponent").setNode(node);

//		nodeMgr.getDynamicDepManager("DBComponent").ondemandSetupIsDone();
		CommServerManager.getInstance().start("DBComponent");

		// send ondemand request
		// sendOndemandRqst();

		// access
//		 accessServices(node);

		System.in.read();
		LOGGER.fine("Stopping ...");
		node.stop();
		
	}

	private static void accessServices(Node node) {
		try {

			System.out
					.println("\nTry to access DBComponent#service-binding(DBService/DBService):");
			DBService db = node.getService(DBService.class,
					"DBComponent#service-binding(DBService/DBService)");
			LOGGER.fine("\t" + "" + db.dbOperation("emptyExeProc"));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}

	public static void sendOndemandRqst() {
		CompLifecycleManager compLcMgr;
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		OndemandSetupHelper ondemandHelper;
		String compIdentifier = "DBComponent";
		compLcMgr = CompLifecycleManager.getInstance(compIdentifier);
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
		ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		ondemandHelper.ondemandSetup();
		Set<Dependence> outDeps = depMgr.getRuntimeDeps();
		LOGGER.fine("OutDepRegistry:");
		for (Iterator iterator = outDeps.iterator(); iterator.hasNext();) {
			Dependence dependence = (Dependence) iterator.next();
			LOGGER.fine(dependence.toString());
		}

		LOGGER.fine("InDepRegistry:");
		Set<Dependence> inDeps = depMgr.getRuntimeInDeps();
		for (Iterator iterator = inDeps.iterator(); iterator.hasNext();) {
			Dependence dependence = (Dependence) iterator.next();
			LOGGER.fine(dependence.toString());
		}
	}

}
