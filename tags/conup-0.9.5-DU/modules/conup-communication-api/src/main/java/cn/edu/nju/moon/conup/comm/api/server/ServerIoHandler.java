package cn.edu.nju.moon.conup.comm.api.server;

import java.util.logging.Logger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import cn.edu.nju.moon.conup.communication.model.CommType;
import cn.edu.nju.moon.conup.communication.model.RequestObject;
import cn.edu.nju.moon.conup.communication.model.ResponseObject;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 */
public class ServerIoHandler extends IoHandlerAdapter{

	private static final Logger LOGGER = Logger.getLogger(ServerIoHandler.class.getName());
	
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		LOGGER.warning("ServerSide:" + cause.getMessage());
		session.close(true);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		RequestObject reqObj = (RequestObject) message;
		ResponseObject reponseObj = process(reqObj);
		boolean replyFlag = check(reqObj.getCommType());
		if(replyFlag)
			session.write(reponseObj);
	}

	private boolean check(CommType commType) {
		if(commType.equals(CommType.SYN)){
			return true;
		} else{
			return false;
		}
	}

	private ResponseObject process(RequestObject reqObj) {
		MsgType msgType = reqObj.getMsgType();
		
		if(msgType.equals(MsgType.DEPENDENCE_MSG)){
			return manageDep(reqObj);
		} else if(msgType.equals(MsgType.ONDEMAND_MSG)){
			return manageOndemand(reqObj);
		} else if(msgType.equals(MsgType.REMOTE_CONF_MSG)){ 
			return manageRemoteConf(reqObj);
		} else if(msgType.equals(MsgType.EXPERIMENT_MSG)){
			return manageExperimentResult(reqObj);
		} else{
			//TODO manage negotiation msg
			return null;
		}
	}
	
	private ResponseObject manageExperimentResult(RequestObject reqObj){
		CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(reqObj.getTargetIdentifier());
		String expResult = compLifeCycleMgr.experimentResult(reqObj.getPayload());
		
		ResponseObject responseObj = new ResponseObject();
		responseObj.setProtocol(reqObj.getProtocol());
		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
		responseObj.setPayload(expResult);
		return responseObj;
	}

	private ResponseObject manageRemoteConf(RequestObject reqObj) {
		CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(reqObj.getTargetIdentifier());
		boolean updateResult = compLifeCycleMgr.remoteConf(reqObj.getPayload());
		
		ResponseObject responseObj = new ResponseObject();
		responseObj.setProtocol(reqObj.getProtocol());
		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
		responseObj.setPayload("updateResult:" + updateResult);
		return responseObj;
	}
	
	/**
	 * process an ondemand msg
	 * @param reqObj
	 * @return
	 */
	private ResponseObject manageOndemand(RequestObject reqObj) {
		NodeManager nodeMgr = NodeManager.getInstance();
		OndemandSetupHelper ondemandSetupHelper = nodeMgr.getOndemandSetupHelper(reqObj.getTargetIdentifier());
		boolean ondemandResult = false;
//		if(reqObj.getSrcIdentifier() == null || reqObj.getSrcIdentifier().equals("null")
//				|| reqObj.getSrcIdentifier().equals("")){
//			ondemandResult = ondemandSetup.ondemandSetup();
//		} else{
			
			ondemandResult = ondemandSetupHelper.ondemandSetup(reqObj.getSrcIdentifier(), reqObj.getProtocol(), reqObj.getPayload());
//		}
		
		ResponseObject responseObj = new ResponseObject();
		responseObj.setProtocol(reqObj.getProtocol());
		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
		responseObj.setPayload("ondemandResult:" + ondemandResult);
		return responseObj;
	}

	/**
	 * process an msg about dependence 
	 * @param reqObj
	 * @return
	 */
	private ResponseObject manageDep(RequestObject reqObj) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager ddm = nodeMgr.getDynamicDepManager(reqObj.getTargetIdentifier());
//		if(reqObj.getPayload().contains("NOTIFY_REMOTE_UPDATE_DONE")){
//			LOGGER.warning("received NOTIFY_REMOTE_UPDATE_DONE src:" + reqObj.getSrcIdentifier() + 
//					" target:" + reqObj.getTargetIdentifier());
//		}
		boolean manageResult = ddm.manageDependence(reqObj.getProtocol(), reqObj.getPayload());
		ResponseObject responseObj = new ResponseObject();
		responseObj.setProtocol(reqObj.getProtocol());
		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
		responseObj.setPayload("manageDepResult:" + manageResult);
		return responseObj;
	}

}