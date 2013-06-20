package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

import com.tuscanyscatours.currencyconverter.CurrencyConverter;

/**
 *	@author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CurrencyConverterLauncher {
	private static 	Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) throws Exception{
	
		LOGGER.setLevel(Level.OFF);
		
		LOGGER.fine("Starting CurrencyConverter node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(CurrencyConverterLauncher.class);
		node.installContribution(contributionURL);
		node.startComposite("fullapp-currency", "fullapp-currency.composite");
		LOGGER.fine("fullapp-currency.composite is ready!");
		
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
//        nodeMgr.loadConupConf("CurrencyConverter", "oldVersion");
//        CompLifecycleManager.getInstance("CurrencyConverter").setNode(node);
//        CommServerManager.getInstance().start("CurrencyConverter");
        
        nodeMgr.loadConupConf("CurrencyConverter", "oldVersion");
		ComponentObject currencyCompObj = nodeMgr.getComponentObject("CurrencyConverter");
		CompLifecycleManagerImpl currencyCompLifecycleManager = new CompLifecycleManagerImpl(currencyCompObj);
		currencyCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("CurrencyConverter", currencyCompLifecycleManager);
		TxDepMonitor currencyTxDepMonitor = new TxDepMonitorImpl(currencyCompObj);
		nodeMgr.setTxDepMonitor("CurrencyConverter", currencyTxDepMonitor);
		TxLifecycleManager currencyTxLifecycleMgr = new TxLifecycleManagerImpl(currencyCompObj);
		nodeMgr.setTxLifecycleManager("CurrencyConverter", currencyTxLifecycleMgr);
		CommServerManager.getInstance().start("CurrencyConverter");
        
//        nodeMgr.getDynamicDepManager("CurrencyConverter").ondemandSetting();
//        nodeMgr.getDynamicDepManager("CurrencyConverter").ondemandSetupIsDone();

        //launch DepRecorder
        DepRecorder depRecorder;
        depRecorder = DepRecorder.getInstance();
        
        //access
//        accessServices(node);
	}
	
	public static void accessServices(Node node){
		try {
			LOGGER.fine("\nTry to update currency component");
//			testUpdate();
			
			System.out
					.println("\nTry to access CurrencyConverter#service-binding(CurrencyConverter/CurrencyConverter):");
			CurrencyConverter currency = node.getService(CurrencyConverter.class, "CurrencyConverter/CurrencyConverter");
			LOGGER.fine("\t" + "CurrencyConverter.convert(\"USD\", \"EUR\", 100)=" + currency.convert("USD", "EUR", 100));

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
				int port = 22300;
				String baseDir = "/home/stone/deploy/travleSample/currencyNew";
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}

}
