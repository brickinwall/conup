package cn.edu.nju.moon.conup.experiments.utils;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

public class TravelCompUpdate {
	
	private static int updateTimesCount = 0;

	public static void update(String targetComp, String ipAddress, String baseDir){
		
		switch (targetComp) {
		case "CurrencyConverter":
			updateCurrency(ipAddress, baseDir);
			break;
		case "TripPartner":
			updateTripPartner(ipAddress, baseDir);
			break;
		case "HotelPartner":
			updateHotelPartner(ipAddress, baseDir);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	private static void updateHotelPartner(String ipAddress, String baseDir) {
		final String ip = ipAddress;
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "HotelPartner";
					int port = 22301;
					String classFilePath = "com.tuscanyscatours.hotel.impl.HotelImpl";
					String contributionUri = "fullapp-bespoketrip";
					String compsiteUri = "fullapp-bespoketrip.composite";
					String baseDir = "/home/conup/redeploy/hotelVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
					
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "HotelPartner";
					int port = 22301;
					String classFilePath = "com.tuscanyscatours.hotel.impl.HotelImpl";
					String contributionUri = "fullapp-bespoketrip";
					String compsiteUri = "fullapp-bespoketrip.composite";
					String baseDir = "/home/conup/redeploy/hotelVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
					
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	public static void updateCurrency(String ipAddress, String newClassLocation) {
		final String ip = ipAddress;
//		final String baseDir = newClassLocation;
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "CurrencyConverter";
					int port = 22300;
					String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
					String contributionUri = "fullapp-currency";
					String compsiteUri = "fullapp-currency.composite";
					String baseDir = "/home/conup/redeploy/currencyVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
					
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "CurrencyConverter";
					int port = 22300;
					String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
					String contributionUri = "fullapp-currency";
					String compsiteUri = "fullapp-currency.composite";
					String baseDir = "/home/conup/redeploy/currencyVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
					
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}
	
	public static void updateTripPartner(String ipAddress, String newClassLocation){
		final String ip = ipAddress;
//		final String baseDir = newClassLocation;
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "TripPartner";
					int port = 22304;
					String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
					String contributionUri = "fullapp-packagedtrip";
					String compsiteUri = "fullapp-packagedtrip.composite";
					String baseDir = "/home/conup/redeploy/tripVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
					String targetIdentifier = "TripPartner";
					int port = 22304;
					String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
					String contributionUri = "fullapp-packagedtrip";
					String compsiteUri = "fullapp-packagedtrip.composite";
					String baseDir = "/home/conup/redeploy/tripVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
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
