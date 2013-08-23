package cn.edu.nju.moon.conup.sample.db.launcher;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.server.ServerIoHandler;
import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.comp.manager.UpdateManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.sample.db.services.DBService;
import cn.edu.nju.moon.conup.sample.db.services.User;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

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
		String compIdentifier = "DBComponent";
		nodeMgr.loadConupConf(compIdentifier, "oldVersion");
		// LOGGER.fine(compObj.getStaticDeps() + "\n" +
		// compObj.getStaticInDeps() + "\n" + compObj.getAlgorithmConf());
		ComponentObject compObj = nodeMgr.getComponentObject(compIdentifier);
		TxDepMonitor txDepMonitor = new TxDepMonitorImpl(compObj);
		nodeMgr.setTxDepMonitor(compIdentifier, txDepMonitor);
		CompLifecycleManagerImpl compLifecycleManager = new CompLifecycleManagerImpl(compObj);
//		compLifecycleManager.setNode(node);
		nodeMgr.setTuscanyNode(node);
		nodeMgr.setCompLifecycleManager(compIdentifier, compLifecycleManager);

//		nodeMgr.getDynamicDepManager(compIdentifier).ondemandSetupIsDone();
		
		TxLifecycleManager txLifecycleMgr = new TxLifecycleManagerImpl(compObj);
		nodeMgr.setTxLifecycleManager(compIdentifier, txLifecycleMgr);
		
		DynamicDepManager depMgr = NodeManager.getInstance().getDynamicDepManager(compObj.getIdentifier());
		depMgr.setTxLifecycleMgr(txLifecycleMgr);
		depMgr.setCompLifeCycleMgr(compLifecycleManager);
//		compLifecycleManager.setDepMgr(depMgr);
		
		OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		
		CommServerManager.getInstance().start(compIdentifier);
		UpdateManager updateMgr = new UpdateManagerImpl(compObj);
		nodeMgr.setUpdateManager(compIdentifier, updateMgr);
		ServerIoHandler serverIoHandler = CommServerManager.getInstance().getCommServer(compIdentifier).getServerIOHandler();
		serverIoHandler.registerUpdateManager(updateMgr);
 		// send ondemand request
		// sendOndemandRqst();

		// access
		 accessServices(node);

		System.in.read();
		LOGGER.fine("Stopping ...");
		node.stop();
		
	}

	private static void accessServices(Node node) {
		try {

			System.out.println("\nTry to access DBComponent#service-binding(DBService/DBService):");
			DBService db = node.getService(DBService.class,	"DBComponent");
//			User user = new User("rgc", 10);
//			db.sayHello(user);
			LOGGER.info("\t" + "" + db.dbOperation("emptyExeProc"));

		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}

	public static void sendOndemandRqst() {
		CompLifeCycleManager compLcMgr;
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		OndemandSetupHelper ondemandHelper;
		String compIdentifier = "DBComponent";
		nodeMgr = NodeManager.getInstance();
		compLcMgr = nodeMgr.getCompLifecycleManager(compIdentifier);
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
