package com.tuscanyscatours.launcher;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

import com.tuscanyscatours.launcher.TravelCompUpdate.UpdatableComp;

public class TravelExpResultQuery {
	public static String queryExpResult(String queryComp, QueryOperation queryOp){
		UpdatableComp targetComp = null;
		try{
			targetComp = Enum.valueOf(UpdatableComp.class, queryComp);
		} catch(Exception e){
			System.out.println("No such component for update or unsupported component.");
			return null;
		}
		
		switch (targetComp) {
		case ShoppingCart:
			return queryShoppingCart(queryOp);
//			break;
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
		
		return null;
	}
	
	public static String queryShoppingCart(QueryOperation queryOp){
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForShoppingcart();
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}
	
	private static String getExeRecorderForShoppingcart(){
		String result = null;
//		Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "ShoppingCart";
				int port = 22307;
				String ip = "192.168.248.135";
				result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
				return result;
//			}
//		});
//		
//		thread.start();
	}
}

enum QueryOperation{
	GET_EXECUTION_RECORDER
}