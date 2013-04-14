package cn.edu.nju.moon.conup.experiments.utils;

import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExperimentOperation;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.UpdatableComp;
import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;


public class TravelExpResultQuery {
	public static String queryExpResult(String queryComp, ExperimentOperation expOp, String ip){
		UpdatableComp targetComp = null;
		try{
			targetComp = Enum.valueOf(UpdatableComp.class, queryComp);
		} catch(Exception e){
			System.out.println("No such component for update or unsupported component.");
			return null;
		}
		
		switch (targetComp) {
		case ShoppingCart:
			return rqstToShoppingCart(expOp, ip);
		case TripPartner:
			return rqstToTripPartner(expOp, ip);
		case CurrencyConverter:
			return rqstToCurrencyConverter(expOp, ip);
		default:
			System.out.println("No such component for update or unsupported component.");
			break;
		}
		
		return null;
	}
	
	private static String rqstToCurrencyConverter(ExperimentOperation queryOp, String ip) {
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForCurrencyConverter(ip);
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}

	private static String getExeRecorderForCurrencyConverter(String ip) {
		String result = null;
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "CurrencyConverter";
		int port = 22300;
		result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
		return result;
	}

	public static String rqstToShoppingCart(ExperimentOperation queryOp, String ip){
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForShoppingcart(ip);
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}
	
	public static String rqstToTripPartner(ExperimentOperation queryOp, String ip){
		switch (queryOp) {
		case GET_EXECUTION_RECORDER:
			return getExeRecorderForTripPartner(ip);
		default:
			System.out.println("Unsupported query operation");
			break;
		}
		return null;
	}
	
	private static String getExeRecorderForShoppingcart(String ip){
		String result = null;
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "ShoppingCart";
		int port = 22307;
		result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
		return result;
	}
	
	private static String getExeRecorderForTripPartner(String ip){
		String result = null;
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "TripPartner";
		int port = 22304;
		result = rcs.getExecutionRecorder(ip, port, targetIdentifier, "CONSISTENCY");
		return result;
	}
}

//enum ExperimentOperation{
//	GET_EXECUTION_RECORDER,
//	CLEAR_EXECUTION_RECORDER
//}
//
//enum CompVersion{
//	VER_ZERO,
//	VER_ONE,
//	VER_TWO
//}
//
//enum UpdatableComp{
//	CurrencyConverter,
//	Bank,
//	ShoppingCart,
//	HotelPartner,
//	TripPartner
//}