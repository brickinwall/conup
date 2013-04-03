package com.tuscanyscatours.launcher;

import java.lang.reflect.Array;
import java.util.Arrays;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

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
	
	public static void updateBankToVerOne() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "Bank";
				int port1 = 22313;
				String baseDir1 = "/home/stone/deploy/travleSample/bankVer1";	//update component to version 1
				String classFilePath1 = "com.tuscanyscatours.bank.impl.BankImpl";
				String contributionUri1 = "fullapp-bank";
				String compsiteUri1 = "bank.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
		
	}
	
	public static void updateBankToVerTwo() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "Bank";
				int port1 = 22313;
				String baseDir1 = "/home/stone/deploy/travleSample/bankVer2";	//update component to version 2
				String classFilePath1 = "com.tuscanyscatours.bank.impl.BankImpl";
				String contributionUri1 = "fullapp-bank";
				String compsiteUri1 = "bank.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
		
	}
	
	public static void updateShoppingcartToVerOne(){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "ShoppingCart";
				int port1 = 22307;
				String baseDir1 = "/home/stone/deploy/travleSample/shoppingcartVer1";	//update component to version 
				String classFilePath1 = "com.tuscanyscatours.shoppingcart.impl.ShoppingCartImpl";
				String contributionUri1 = "fullapp-shoppingcart";
				String compsiteUri1 = "fullapp-shoppingcart.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}
	
	public static void updateHotelPartnerToVerOne(){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "HotelPartner";
				int port1 = 22301;
				String baseDir1 = "/home/stone/deploy/travleSample/hotelVer1";	//update component to version 
				String classFilePath1 = "com.tuscanyscatours.hotel.impl.HotelImpl";
				String contributionUri1 = "fullapp-bespoketrip";
				String compsiteUri1 = "fullapp-bespoketrip.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}
	
	public static void updateTripPartnerToVerOne(){
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier1 = "TripPartner";
				int port1 = 22304;
				String baseDir1 = "/home/stone/deploy/travleSample/tripVer1";	//update component to version 
				String classFilePath1 = "com.tuscanyscatours.trip.impl.TripImpl";
				String contributionUri1 = "fullapp-packagedtrip";
				String compsiteUri1 = "fullapp-packagedtrip.composite";
				rcs.update("10.0.2.15", port1, targetIdentifier1, "CONSISTENCY", baseDir1, classFilePath1, contributionUri1, compsiteUri1);
			}
		});
		
		thread.start();
	}
	
	public static void updateCurrencyToVerOne() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String baseDir = "/home/stone/deploy/travleSample/currencyVer1";
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	public static void updateCurrencyToVerTwo() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "CurrencyConverter";
				int port = 22300;
				String baseDir = "/home/stone/deploy/travleSample/currencyVer2";
				String classFilePath = "com.tuscanyscatours.currencyconverter.impl.CurrencyConverterImpl";
				String contributionUri = "fullapp-currency";
				String compsiteUri = "fullapp-currency.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	public static void updateBankToVer(CompVersion toVer){
//		CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		switch (toVer) {
		case VER_ONE:
			updateBankToVerOne();
			break;
		case VER_TWO:
			updateBankToVerTwo();
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateCurrencyToVer(CompVersion toVer){
//		CompVersion targetVer = Enum.valueOf(CompVersion.class, toVer);
		switch (toVer) {
		case VER_ONE:
			updateCurrencyToVerOne();
			break;
		case VER_TWO:
			updateCurrencyToVerTwo();
			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateShoppingcartToVer(CompVersion toVer){
		switch (toVer) {
		case VER_ONE:
			updateShoppingcartToVerOne();
			break;
//		case VER_TWO:
//			updateCurrencyToVerTwo();
//			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateHotelPartnerToVer(CompVersion toVer){
		switch (toVer) {
		case VER_ONE:
			updateHotelPartnerToVerOne();
			break;
//		case VER_TWO:
//			updateCurrencyToVerTwo();
//			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void updateTripPartnerToVer(CompVersion toVer){
		switch (toVer) {
		case VER_ONE:
			updateTripPartnerToVerOne();
			break;
//		case VER_TWO:
//			updateCurrencyToVerTwo();
//			break;
		default:
			System.out.println("Unsupported component verson for update");
			break;
		}
	}
	
	public static void update(String updateComp, String toVer){
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
			updateCurrencyToVer(targetVer);
			break;
		case Bank:
			updateBankToVer(targetVer);
			break;
		case ShoppingCart:
			updateShoppingcartToVer(targetVer);
			break;
		case HotelPartner:
			updateHotelPartnerToVer(targetVer);
			break;
		case TripPartner:
			updateTripPartnerToVer(targetVer);
			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
	}
	
	public static void update(){
		if(updateTimesCount % 2 == 0)
			updateCurrencyToVerOne();
		else
			updateCurrencyToVerTwo();
		
		updateTimesCount++;
	}
	
	public static void main(String []args){
		String updateComp = "ABC";
		String toVer = "VER_ONE";
		UpdatableComp [] values = UpdatableComp.values();
		Arrays.asList(values);
	}
	
	enum CompVersion{
		VER_ZERO,
		VER_ONE,
		VER_TWO
	}
	
	enum UpdatableComp{
		CurrencyConverter,
		Bank,
		ShoppingCart,
		HotelPartner,
		TripPartner
	}
}
