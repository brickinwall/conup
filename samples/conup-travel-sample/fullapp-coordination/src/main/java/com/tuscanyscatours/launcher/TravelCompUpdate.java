package com.tuscanyscatours.launcher;

import java.util.Arrays;

import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.CompVersion;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.UpdatableComp;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;

/**
 * 
 * @Author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class TravelCompUpdate {
//	public static String VER_ZERO = "VER_0";
//	public static String VER_ONE = "VER_1";
//	public static String VER_TWO = "VER_2";
	private static int updateTimesCount = 0;
	
	public static void updateBankToVerOne(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "Bank";
				int port = 22313;
				String baseDir = "/home/artemis/Tuscany/deploy/bankVer1";	//update component to version 1
				String classFilePath = "com.tuscanyscatours.bank.impl.BankImpl";
				String contributionUri = "fullapp-bank";
				String compsiteUri = "bank.composite";
				
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
	
	public static void updateBankToVerTwo(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "Bank";
				int port = 22313;
				String baseDir = "/home/artemis/Tuscany/deploy/bankVer2";	//update component to version 2
				String classFilePath = "com.tuscanyscatours.bank.impl.BankImpl";
				String contributionUri = "fullapp-bank";
				String compsiteUri = "bank.composite";
				
				String ip = "10.0.2.15";
				String protocol = "CONSISTENCY";
				RemoteConfigContext rcc = new RemoteConfigContext(ip, port,
						targetIdentifier, protocol, baseDir, classFilePath,
						contributionUri, null, compsiteUri);
				rcs.update(rcc);
				
//				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
			}
		});
		
		thread.start();
		
	}
	
	public static void updateShoppingcartToVerOne(final Scope scope){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "ShoppingCart";
				int port = 22307;
				String baseDir = "/home/artemis/Tuscany/deploy/shoppingcartVer1";	//update component to version 
				String classFilePath = "com.tuscanyscatours.shoppingcart.impl.ShoppingCartImpl";
				String contributionUri = "fullapp-shoppingcart";
				String compsiteUri = "fullapp-shoppingcart.composite";
				
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
	
	public static void updateHotelPartnerToVerOne(final Scope scope){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "HotelPartner";
				int port = 22301;
				String baseDir = "/home/artemis/Tuscany/deploy/hotelVer1";	//update component to version 
				String classFilePath = "com.tuscanyscatours.hotel.impl.HotelImpl";
				String contributionUri = "fullapp-bespoketrip";
				String compsiteUri = "fullapp-bespoketrip.composite";
				
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
	
	public static void updateTripPartnerToVerOne(final Scope scope){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "TripPartner";
				int port = 22304;
				String baseDir = "/home/artemis/Tuscany/deploy/tripVer1";	//update component to version 
				String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
				String contributionUri = "fullapp-packagedtrip";
				String compsiteUri = "fullapp-packagedtrip.composite";
				
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
	
	public static void updateTripPartnerToVerTwo(final Scope scope){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "TripPartner";
				int port = 22304;
				String baseDir = "/home/artemis/Tuscany/deploy/tripVer2";	//update component to version 
				String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
				String contributionUri = "fullapp-packagedtrip";
				String compsiteUri = "fullapp-packagedtrip.composite";
				
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
	
	public static void updateCurrencyToVerOne(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String baseDir = "/home/artemis/Tuscany/deploy/currencyVer1";
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
	
	public static void updateCurrencyToVerTwo(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String baseDir = "/home/artemis/Tuscany/deploy/currencyVer2";
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
	
	public static void updateBankToVer(CompVersion toVer, Scope scope){
//		CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		switch (toVer) {
		case VER_ONE:
			updateBankToVerOne(scope);
			break;
		case VER_TWO:
			updateBankToVerTwo(scope);
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	private static void updateTravelCatalog(CompVersion targetVer, Scope scope) {
		switch (targetVer) {
		case VER_ONE:
			updateTravelCatalogToVerOne(scope);
			break;
		case VER_TWO:
			updateTravelCatalogToVerTwo(scope);
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}

	private static void updateTravelCatalogToVerTwo(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "CurrencyConverter";
				String baseDir = "/home/artemis/Tuscany/deploy/travelCatalogVer2";
				
				final int port = 22305;
				final String classFilePath = "com.tuscanyscatours.travelcatalog.impl";
				final String contributionUri = "fullapp-travelcatalog";
				final String compsiteUri = "fullapp-travelcatalog.composite";
				
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

	private static void updateTravelCatalogToVerOne(final Scope scope) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfigTool rcs =  new RemoteConfigTool();
				String targetIdentifier = "CurrencyConverter";
				String baseDir = "/home/artemis/Tuscany/deploy/travelCatalogVer1";
				
				final int port = 22305;
				final String classFilePath = "com.tuscanyscatours.travelcatalog.impl";
				final String contributionUri = "fullapp-travelcatalog";
				final String compsiteUri = "fullapp-travelcatalog.composite";
				
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

	public static void updateCurrencyToVer(CompVersion toVer, Scope scope){
//		CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		switch (toVer) {
		case VER_ONE:
			updateCurrencyToVerOne(scope);
			break;
		case VER_TWO:
			updateCurrencyToVerTwo(scope);
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateShoppingcartToVer(CompVersion toVer, Scope scope){
		switch (toVer) {
		case VER_ONE:
			updateShoppingcartToVerOne(scope);
			break;
//		case VER_TWO:
//			updateCurrencyToVerTwo();
//			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateHotelPartnerToVer(CompVersion toVer, Scope scope){
		switch (toVer) {
		case VER_ONE:
			updateHotelPartnerToVerOne(scope);
			break;
//		case VER_TWO:
//			updateCurrencyToVerTwo();
//			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateTripPartnerToVer(CompVersion toVer, Scope scope){
		switch (toVer) {
		case VER_ONE:
			updateTripPartnerToVerOne(scope);
			break;
		case VER_TWO:
			updateTripPartnerToVerTwo(scope);
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void update(String updateComp, String toVer, Scope scope){
		UpdatableComp targetComp = null;
		CompVersion targetVer = null;
		try{
			targetComp = Enum.valueOf(UpdatableComp.class, updateComp);
			targetVer = Enum.valueOf(CompVersion.class, toVer);
		} catch(Exception e){
			System.out.println("No such component for update or unsupported component.");
			return;
		}
		
		switch (targetComp) {
		case CurrencyConverter:
			updateCurrencyToVer(targetVer, scope);
			break;
		case Bank:
			updateBankToVer(targetVer, scope);
			break;
		case ShoppingCart:
			updateShoppingcartToVer(targetVer, scope);
			break;
		case HotelPartner:
			updateHotelPartnerToVer(targetVer, scope);
			break;
		case TripPartner:
			updateTripPartnerToVer(targetVer, scope);
			break;
		case TravelCatalog:
			updateTravelCatalog(targetVer, scope);
			break;
		case EmailGateway:
			updateEmailGateway(scope);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	private static void updateEmailGateway(final Scope scope) {
		final String ip = "10.0.2.15";
		final String targetIdentifier = "EmailGateway";
		final int port = 22312;
		final String classFilePath = "com.tuscanyscatours.emailgateway.impl.EmailGatewayImpl";
		final String contributionUri = "payment-java";
		final String compsiteUri = "payment.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/artemis/Tuscany/deploy/emailGateWayVer1";
					
					String protocol = "CONSISTENCY";
					RemoteConfigContext rcc = new RemoteConfigContext(ip, port,
							targetIdentifier, protocol, baseDir, classFilePath,
							contributionUri, null, compsiteUri);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/artemis/Tuscany/deploy/emailGateWayVer2";
					String protocol = "CONSISTENCY";
					RemoteConfigContext rcc = new RemoteConfigContext(ip, port,
							targetIdentifier, protocol, baseDir, classFilePath,
							contributionUri, null, compsiteUri);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;		
	}

	public static void update(String updateComp, Scope scope){
		if(updateTimesCount % 2 == 0)
			update(updateComp, CompVersion.VER_ONE.toString(), scope);
		else
			update(updateComp, CompVersion.VER_TWO.toString(), scope);
		
		updateTimesCount++;
	}
	
	public static void main(String []args){
		String updateComp = "ABC";
		String toVer = "VER_ONE";
		UpdatableComp [] values = UpdatableComp.values();
		Arrays.asList(values);
	}
	
}
