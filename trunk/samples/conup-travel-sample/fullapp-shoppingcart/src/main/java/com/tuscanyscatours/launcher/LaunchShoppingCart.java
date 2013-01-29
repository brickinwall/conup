package com.tuscanyscatours.launcher;

import java.util.UUID;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.shoppingcart.CartCheckout;
import com.tuscanyscatours.shoppingcart.CartInitialize;
import com.tuscanyscatours.shoppingcart.CartUpdates;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.DepRecorder;

public class LaunchShoppingCart {

	public static void main(String[] args) throws Exception {

		System.out.println("Starting ShoppingCart node...");
		String domainURI = "uri:default";
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		Node node = runtime.createNode(domainURI);
		String contributionURL = ContributionLocationHelper
				.getContributionLocation(LaunchShoppingCart.class);
		node.installContribution(contributionURL);

		node.startComposite("fullapp-shoppingcart",
				"fullapp-shoppingcart.composite");
		System.out.println("fullapp-shoppingcart.composite is ready!");

		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		nodeMgr.loadConupConf("ShoppingCart", "oldVersion");
//		nodeMgr.getDynamicDepManager("ShoppingCart").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("ShoppingCart").setNode(node);
		CommServerManager.getInstance().start("ShoppingCart");
		
		nodeMgr.loadConupConf("CartStore", "oldVersion");
//		nodeMgr.getDynamicDepManager("CartStore").ondemandSetupIsDone();
		CompLifecycleManager.getInstance("CartStore").setNode(node);
		CommServerManager.getInstance().start("CartStore");

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
			
			System.out.println("\nTry to access ShoppingCart#service-binding(CartInitialize/CartInitialize):");
			CartInitialize cartInitialize = node
					.getService(CartInitialize.class,
							"ShoppingCart#service-binding(CartInitialize/CartInitialize)");
			String cartId = cartInitialize.newCart();
			System.out.println("create a new cartId: " + cartId);
			
			CartUpdates cartUpdate = node.getService(CartUpdates.class, "ShoppingCart#service-binding(CartUpdates/CartUpdates)");
			TripItem trip = new TripItem("", "", TripItem.CAR, "FS1DEC06", "Florence and Siena pre-packaged tour", "LGW - FLR", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd");
			trip.setTripItems(new TripItem[]{});
//			trip.setTripItems(new TripItem[]{new TripItem("", "", TripItem.CAR, "FS1DEC06", "Florence and Siena pre-packaged tour", "LGW - FLR", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd")});
			TripItem trip2 = new TripItem("", "", TripItem.TRIP, "FS1DEC06", "Florence and Siena pre-packaged tour", "FLR - LGW", "06/12/09", "13/12/09", 450, "EUR", "http://localhost:8085/tbd");
			cartUpdate.addTrip(cartId, trip);
			cartUpdate.addTrip(cartId, trip2);

			TripItem[] tripItems = cartInitialize.getTrips(cartId);
			System.out.println(tripItems.length);
			System.out.println(tripItems[0]);
			System.out.println(tripItems[1]);
			
			CartCheckout cartCheckout = node.getService(CartCheckout.class, "ShoppingCart#service-binding(CartCheckout/CartCheckout)");
			cartCheckout.checkout(cartId, "c-0");
//			String cartId2 = cartInitialize.newCart();
//			System.out.println("create a new cartId: " + cartId2);
//			System.out.println(cartInitialize.proxyEqual(cartId, cartId2));
			
//			
//			TripItem[] allAddedTripItems = cartInitialize.getTrips(cartId);
//			System.out.println(allAddedTripItems.length);
//			System.out.println(allAddedTripItems[0]);
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
