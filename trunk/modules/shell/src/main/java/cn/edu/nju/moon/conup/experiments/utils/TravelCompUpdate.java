package cn.edu.nju.moon.conup.experiments.utils;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

public class TravelCompUpdate {

	public static void update(String targetComp, String ipAddress, String baseDir){
		
		switch (targetComp) {
		case "CurrencyConverter":
			updateCurrency(ipAddress, baseDir);
			break;
		case "TripPartner":
			updateTripPartner(ipAddress, baseDir);
			break;
		case "HotelPartner":
			
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	public static void updateCurrency(String ipAddress, String newClassLocation) {
		final String ip = ipAddress;
		final String baseDir = newClassLocation;
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	public static void updateTripPartner(String ipAddress, String newClassLocation){
		final String ip = ipAddress;
		final String baseDir = newClassLocation;
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "TripPartner";
				int port = 22304;
				String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
				String contributionUri = "fullapp-packagedtrip";
				String compsiteUri = "fullapp-packagedtrip.composite";
				rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
			}
		});
		
		thread.start();
	}
	
	enum CompVersion{
		VER_ZERO,
		VER_ONE,
		VER_TWO
	}
}
