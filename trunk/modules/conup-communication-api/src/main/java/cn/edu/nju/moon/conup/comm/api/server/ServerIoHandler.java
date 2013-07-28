package cn.edu.nju.moon.conup.comm.api.server;

import java.util.logging.Logger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import cn.edu.nju.moon.conup.spi.datamodel.CommType;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.ResponseObject;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * @author rgc
 */
public class ServerIoHandler extends IoHandlerAdapter{

	private static final Logger LOGGER = Logger.getLogger(ServerIoHandler.class.getName());
	private String result = null;
	private UpdateManager updateManager = null; 
	
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		LOGGER.warning("ServerSide:" + cause.getMessage());
		session.close(true);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		RequestObject reqObj = (RequestObject) message;
		result = updateManager.processMsg(reqObj);
		ResponseObject responseObj = new ResponseObject();
		responseObj.setProtocol(reqObj.getProtocol());
		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
		responseObj.setPayload(result);
		boolean replyFlag = check(reqObj.getCommType());
		if(replyFlag)
			session.write(responseObj);
		
////		System.out.println("before:" + result + " " + reqObj.getPayload() + " " + ddm.getCompStatus());
//		notifyObservers(message);
////		System.out.println("after:" + result + " " + reqObj.getPayload() + " " + ddm.getCompStatus());
//		ResponseObject responseObj = new ResponseObject();
//		responseObj.setProtocol(reqObj.getProtocol());
//		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
//		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
//		responseObj.setPayload(result);
//		boolean replyFlag = check(reqObj.getCommType());
//		if(replyFlag)
//			session.write(responseObj);
//		result = null;
////		ResponseObject reponseObj = process(reqObj);
////		boolean replyFlag = check(reqObj.getCommType());
////		if(replyFlag)
////			session.write(reponseObj);
	}

	private boolean check(CommType commType) {
		if(commType.equals(CommType.SYN)){
			return true;
		} else{
			return false;
		}
	}

	public void registerUpdateManager(UpdateManager updateMgr){
		this.updateManager = updateMgr;
	}
	

}
