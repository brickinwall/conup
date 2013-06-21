package com.tuscanyscatours.launcher;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.shoppingcart.CartCheckout;
import com.tuscanyscatours.shoppingcart.CartInitialize;
import com.tuscanyscatours.shoppingcart.CartUpdates;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitorImpl;
import cn.edu.nju.moon.conup.ext.tx.manager.TxLifecycleManagerImpl;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class ShoppingCartLauncher {
	
	private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static void main(String[] args) throws Exception {
		
		LOGGER.setLevel(Level.OFF);
		
		LOGGER.fine("Starting ShoppingCart node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(ShoppingCartLauncher.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-shoppingcart",
				"fullapp-shoppingcart.composite");
		LOGGER.fine("fullapp-shoppingcart.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
//		nodeMgr.loadConupConf("ShoppingCart", "oldVersion");
//		CompLifecycleManager.getInstance("ShoppingCart").setNode(node);
//		CommServerManager.getInstance().start("ShoppingCart");
		
		nodeMgr.loadConupConf("ShoppingCart", "oldVersion");
		ComponentObject shoppingCartCompObj = nodeMgr.getComponentObject("ShoppingCart");
		CompLifecycleManagerImpl shoppingCartCompLifecycleManager = new CompLifecycleManagerImpl(shoppingCartCompObj);
		shoppingCartCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("ShoppingCart", shoppingCartCompLifecycleManager);
		TxDepMonitor shoppingCartTxDepMonitor = new TxDepMonitorImpl(shoppingCartCompObj);
		nodeMgr.setTxDepMonitor("ShoppingCart", shoppingCartTxDepMonitor);
		TxLifecycleManager shoppingCartTxLifecycleMgr = new TxLifecycleManagerImpl(shoppingCartCompObj);
		nodeMgr.setTxLifecycleManager("ShoppingCart", shoppingCartTxLifecycleMgr);
		
		DynamicDepManager shoppingcartDepMgr = NodeManager.getInstance().getDynamicDepManager(shoppingCartCompObj.getIdentifier());
		shoppingcartDepMgr.setTxLifecycleMgr(shoppingCartTxLifecycleMgr);
		shoppingCartCompLifecycleManager.setDepMgr(shoppingcartDepMgr);
		
		CommServerManager.getInstance().start("ShoppingCart");
		
//		nodeMgr.loadConupConf("CartStore", "oldVersion");
//		CompLifecycleManager.getInstance("CartStore").setNode(node);
//		CommServerManager.getInstance().start("CartStore");
		
		nodeMgr.loadConupConf("CartStore", "oldVersion");
		ComponentObject cartStoreCompObj = nodeMgr.getComponentObject("CartStore");
		CompLifecycleManagerImpl cartStoreCompLifecycleManager = new CompLifecycleManagerImpl(cartStoreCompObj);
		cartStoreCompLifecycleManager.setNode(node);
		nodeMgr.setCompLifecycleManager("CartStore", cartStoreCompLifecycleManager);
		TxDepMonitor cartStoreTxDepMonitor = new TxDepMonitorImpl(cartStoreCompObj);
		nodeMgr.setTxDepMonitor("CartStore", cartStoreTxDepMonitor);
		TxLifecycleManager cartStoreTxLifecycleMgr = new TxLifecycleManagerImpl(cartStoreCompObj);
		nodeMgr.setTxLifecycleManager("CartStore", cartStoreTxLifecycleMgr);
		
		DynamicDepManager cartStoreDepMgr = NodeManager.getInstance().getDynamicDepManager(cartStoreCompObj.getIdentifier());
		cartStoreDepMgr.setTxLifecycleMgr(shoppingCartTxLifecycleMgr);
		cartStoreCompLifecycleManager.setDepMgr(cartStoreDepMgr);
		
		CommServerManager.getInstance().start("CartStore");
		
//		nodeMgr.getDynamicDepManager("ShoppingCart").ondemandSetting();
//		nodeMgr.getDynamicDepManager("CartStore").ondemandSetting();
//
//		nodeMgr.getDynamicDepManager("ShoppingCart").ondemandSetupIsDone();
//		nodeMgr.getDynamicDepManager("CartStore").ondemandSetupIsDone();

		// launch DepRecorder
		DepRecorder depRecorder;
		depRecorder = DepRecorder.getInstance();

		// access
//		accessServices(node);
	}

	private static void accessServices(Node node) throws Exception {

		try {
//			testUpdate();
//			Thread.sleep(2000);
			
			LOGGER.fine("\nTry to access ShoppingCart#service-binding(CartInitialize/CartInitialize):");
			CartInitialize cartInitialize = node.getService(CartInitialize.class, "ShoppingCart/CartInitialize");
			String cartId = cartInitialize.newCart();
			LOGGER.fine("create a new cartId: " + cartId);
			
			CartUpdates cartUpdate = node.getService(CartUpdates.class, "ShoppingCart/CartUpdates");
			TripItem trip = new TripItem("", "", TripItem.CAR, "FS1DEC06", "Florence and Siena pre-packaged tour", "LGW - FLR", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd");
			trip.setTripItems(new TripItem[]{});
//			trip.setTripItems(new TripItem[]{new TripItem("", "", TripItem.CAR, "FS1DEC06", "Florence and Siena pre-packaged tour", "LGW - FLR", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd")});
			TripItem trip2 = new TripItem("", "", TripItem.TRIP, "FS1DEC06", "Florence and Siena pre-packaged tour", "FLR - LGW", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd");
			cartUpdate.addTrip(cartId, trip);
			cartUpdate.addTrip(cartId, trip2);

			TripItem[] tripItems = cartInitialize.getTrips(cartId);

			
			CartCheckout cartCheckout = node.getService(CartCheckout.class, "ShoppingCart/CartCheckout");
			cartCheckout.checkout(cartId, "c-0");
//			String cartId2 = cartInitialize.newCart();
//			LOGGER.fine("create a new cartId: " + cartId2);
//			LOGGER.fine(cartInitialize.proxyEqual(cartId, cartId2));
			
//			
//			TripItem[] allAddedTripItems = cartInitialize.getTrips(cartId);
//			LOGGER.fine(allAddedTripItems.length);
//			LOGGER.fine(allAddedTripItems[0]);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
	
	private static void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
//				String targetIdentifier = "ShoppingCart";
//				int port = 22307;
//				String baseDir = "/home/rgc";
//				String classFilePath = "com.tuscanyscatours.shoppingcart.impl.ShoppingCartImpl";
//				String contributionUri = "fullapp-shoppingcart";
//				String compsiteUri = "fullapp-shoppingcart.composite";
//				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
				String targetIdentifier1 = "CurrencyConverter";
				int port1 = 22300;
				String baseDir1 = "/home/rgc";
				String classFilePath1 = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri1 = "fullapp-currency";
				String compsiteUri1 = "fullapp-currency.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
				
			}
		});
		
		thread.start();
	}

}
