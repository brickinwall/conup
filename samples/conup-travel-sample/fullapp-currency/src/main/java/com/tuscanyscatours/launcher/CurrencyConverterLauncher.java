package com.tuscanyscatours.launcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.currencyconverter.CurrencyConverter;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

/**
 *	@author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class CurrencyConverterLauncher {

	public static void main(String[] args) throws Exception{
		
		System.out.println("Starting CurrencyConverter node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(CurrencyConverterLauncher.class);
		node.installContribution(contributionURL);
		node.startComposite("fullapp-currency", "fullapp-currency.composite");
		System.out.println("fullapp-currency.composite is ready!");
		
        NodeManager nodeMgr;
        nodeMgr = NodeManager.getInstance();
        nodeMgr.loadConupConf("CurrencyConverter", "oldVersion");
//        nodeMgr.getDynamicDepManager("CurrencyConverter").ondemandSetupIsDone();
        CompLifecycleManager.getInstance("CurrencyConverter").setNode(node);
        CommServerManager.getInstance().start("CurrencyConverter");
        
        //launch DepRecorder
        DepRecorder depRecorder;
        depRecorder = DepRecorder.getInstance();
        
        //access
//        accessServices(node);
	}
	
	public static void accessServices(Node node){
		try {
			System.out.println("\nTry to update currency component");
//			testUpdate();
			
			System.out
					.println("\nTry to access CurrencyConverter#service-binding(CurrencyConverter/CurrencyConverter):");
			CurrencyConverter currency = node.getService(CurrencyConverter.class,
					"CurrencyConverter#service-binding(CurrencyConverter/CurrencyConverter)");
			System.out.println("\t" + "CurrencyConverter.convert(\"USD\", \"EUR\", 100)=" + currency.convert("USD", "EUR", 100));

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
