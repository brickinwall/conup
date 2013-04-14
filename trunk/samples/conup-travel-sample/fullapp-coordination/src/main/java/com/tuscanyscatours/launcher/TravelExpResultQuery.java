package com.tuscanyscatours.launcher;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExperimentOperation;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.UpdatableComp;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

public class TravelExpResultQuery {
	public static String queryExpResult(String queryComp, ExperimentOperation expOp){
		UpdatableComp targetComp = null;
		try{
			targetComp = Enum.valueOf(UpdatableComp.class, queryComp);
		} catch(Exception e){
			System.out.println("No such component for update or unsupported component.");
			return null;
		}
		
		switch (targetComp) {
		case ShoppingCart:
			return rqstTOShoppingCart(expOp);
//			break;
		case TripPartner:
			return rqstToTripPartner(expOp);
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
		
		return null;
	}
	
	public static String rqstTOShoppingCart(ExperimentOperation queryOp){
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForShoppingcart();
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}
	
	public static String rqstToTripPartner(ExperimentOperation queryOp){
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForTripPartner();
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}
	
	private static String getExeRecorderForShoppingcart(){
		String result = null;
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "ShoppingCart";
		int port = 22307;
		String ip = "10.0.2.15";
		result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
		return result;
	}
	
	private static String getExeRecorderForTripPartner(){
		String result = null;
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "TripPartner";
		int port = 22304;
		String ip = "10.0.2.15";
		result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
		return result;
	}
}

//enum ExperimentOperation{
//	GET_EXECUTION_RECORDER,
//	CLEAR_EXECUTION_RECORDER
//}