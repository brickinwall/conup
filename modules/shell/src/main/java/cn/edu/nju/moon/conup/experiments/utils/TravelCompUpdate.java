package cn.edu.nju.moon.conup.experiments.utils;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;

public class TravelCompUpdate {
	
	private static int updateTimesCount = 0;

	public static void update(String targetComp, String ipAddress, String baseDir, Scope scope){
		
		switch (targetComp) {
		case "CurrencyConverter":
			updateCurrency(ipAddress, baseDir, scope);
			break;
		case "TripPartner":
			updateTripPartner(ipAddress, baseDir, scope);
			break;
		case "HotelPartner":
			updateHotelPartner(ipAddress, baseDir, scope);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	private static void updateHotelPartner(String ipAddress, String baseDir, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "HotelPartner";
		final int port = 22301;
		final String classFilePath = "com.tuscanyscatours.hotel.impl.HotelImpl";
		final String contributionUri = "fullapp-bespoketrip";
		final String compsiteUri = "fullapp-bespoketrip.composite";
		final RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/hotelVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/hotelVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}

	public static void updateCurrency(String ipAddress, String newClassLocation, final Scope scope) {
		final String ip = ipAddress;
		final String targetIdentifier = "CurrencyConverter";
		final int port = 22300;
		final String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
		final String contributionUri = "fullapp-currency";
		final String compsiteUri = "fullapp-currency.composite";
		final RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/currencyVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
					
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/currencyVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
					
				}
			});
			
			thread.start();
		}
		
		updateTimesCount++;
	}
	
	public static void updateTripPartner(String ipAddress, String newClassLocation, final Scope scope){
		final String ip = ipAddress;
		final String targetIdentifier = "TripPartner";
		final int port = 22304;
		final String classFilePath = "com.tuscanyscatours.trip.impl.TripImpl";
		final String contributionUri = "fullapp-packagedtrip";
		final String compsiteUri = "fullapp-packagedtrip.composite";
		final RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		if(updateTimesCount % 2 == 0){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/tripVer1";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
				}
			});
			
			thread.start();
		} else{
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String baseDir = "/home/conup/redeploy/tripVer2";
					rcs.update(ip, port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri, scope);
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
