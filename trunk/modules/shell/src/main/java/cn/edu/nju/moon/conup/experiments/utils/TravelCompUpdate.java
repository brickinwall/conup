package cn.edu.nju.moon.conup.experiments.utils;

import cn.edu.nju.moon.conup.comm.api.remote.RemoteConfigTool;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;

public class TravelCompUpdate {
	
	private static int updateTimesCount = 0;

	public static void update(String targetComp, String ipAddress, Scope scope){
		
		switch (targetComp) {
		case "CurrencyConverter":
			updateCurrency(ipAddress, scope);
			break;
		case "TripPartner":
			updateTripPartner(ipAddress, scope);
			break;
		case "HotelPartner":
			updateHotelPartner(ipAddress, scope);
			break;
		case "Payment":
			updatePayment(ipAddress, scope);
			break;
		case "TravelCatalog":
			updateTravelCatalog(ipAddress, scope);
			break;
		case "EmailGateway":
			updateEmailGateway(ipAddress, scope);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	private static void updateEmailGateway(String ipAddress, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "EmailGateway";
		final int port = 22312;
		final String classFilePath = "com.tuscanyscatours.emailgateway.impl.EmailGatewayImpl";
		final String contributionUri = "payment-java";
		final String compsiteUri = "payment.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/emailGateWayVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/emailGateWayVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	private static void updateTravelCatalog(String ipAddress, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "TravelCatalog";
		final int port = 22305;
		final String classFilePath = "com.tuscanyscatours.travelcatalog.impl.TravelCatalogImpl";
		final String contributionUri = "fullapp-travelcatalog";
		final String compsiteUri = "fullapp-travelcatalog.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/travelCatalogVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/travelCatalogVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	private static void updatePayment(String ipAddress, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "Payment";
		final int port = 22310;
		final String classFilePath = "com.tuscanyscatours.payment.impl.PaymentImpl";
		final String contributionUri = "payment-java";
		final String compsiteUri = "payment.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/paymentVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/paymentVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	private static void updateHotelPartner(String ipAddress, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "HotelPartner";
		final int port = 22301;
		final String classFilePath = "com.tuscanyscatours.hotel.impl.HotelImpl";
		final String contributionUri = "fullapp-bespoketrip";
		final String compsiteUri = "fullapp-bespoketrip.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/hotelVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/hotelVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	public static void updateCurrency(String ipAddress, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "CurrencyConverter";
		final int port = 22300;
		final String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
		final String contributionUri = "fullapp-currency";
		final String compsiteUri = "fullapp-currency.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/currencyVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
					
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/currencyVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}
	
	public static void updateTripPartner(String ipAddress, final Scope scope){
		final String ip = ipAddress;
		final String targetIdentifier = "TripPartner";
		final int port = 22304;
		final String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
		final String contributionUri = "fullapp-packagedtrip";
		final String compsiteUri = "fullapp-packagedtrip.composite";
		final RemoteConfigTool rcs =  new RemoteConfigTool();
		final String protocol = "CONSISTENCY";
		final RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol, null, classFilePath, contributionUri, scope, compsiteUri);
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/tripVer1";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/tripVer2";
					rcc.setBaseDir(baseDir);
					rcs.update(rcc);
				}
			});
			
			thread.start();
		}
		updateTimesCount++;
	}
	
	enum CompVersion{
		VER_ZERO,
		VER_ONE,
		VER_TWO
	}
}
