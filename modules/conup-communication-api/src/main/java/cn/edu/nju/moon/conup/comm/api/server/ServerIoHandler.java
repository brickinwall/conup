package cn.edu.nju.moon.conup.comm.api.server;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import cn.edu.nju.moon.conup.spi.datamodel.CommType;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.ResponseObject;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;
import cn.edu.nju.moon.conup.spi.pubsub.Subject;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * @author rgc
 */
public class ServerIoHandler extends IoHandlerAdapter implements Subject{

	private static final Logger LOGGER = Logger.getLogger(ServerIoHandler.class.getName());
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private String result = null;
	private UpdateManager updateManager = null; 
	
	public ServerIoHandler(String compIdentifier) {
		NodeManager nodeMgr = NodeManager.getInstance();
		updateManager = nodeMgr.getUpdateManageer(compIdentifier); 
		
//		DynamicDepManager ddm = nodeMgr.getDynamicDepManager(compIdentifier);
//		OndemandSetupHelper ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
//		CompLifecycleManager compLifeCycleMgr = nodeMgr.getCompLifecycleManager(compIdentifier);
//		registerObserver(ddm);
//		registerObserver(ondemandHelper);
//		registerObserver(compLifeCycleMgr);
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		LOGGER.warning("ServerSide:" + cause.getMessage());
		session.close(true);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		RequestObject reqObj = (RequestObject) message;
		result = updateManager.process(reqObj);
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

//	private ResponseObject process(RequestObject reqObj) {
//		MsgType msgType = reqObj.getMsgType();
//		
//		if(msgType.equals(MsgType.DEPENDENCE_MSG)){
//			return manageDep(reqObj);
//		} else if(msgType.equals(MsgType.ONDEMAND_MSG)){
//			return manageOndemand(reqObj);
//		} else if(msgType.equals(MsgType.REMOTE_CONF_MSG)){ 
////			return manageRemoteConf(reqObj);
//		} else if(msgType.equals(MsgType.EXPERIMENT_MSG)){
//			return manageExperimentResult(reqObj);
//		} else{
//			//TODO manage negotiation msg
//			return null;
//		}
//		return null;
//	}
//	
//	private ResponseObject manageExperimentResult(RequestObject reqObj){
//		CompLifecycleManagerImpl compLifeCycleMgr = CompLifecycleManagerImpl.getInstance(reqObj.getTargetIdentifier());
//		String expResult = compLifeCycleMgr.experimentResult(reqObj.getPayload());
//		
//		ResponseObject responseObj = new ResponseObject();
//		responseObj.setProtocol(reqObj.getProtocol());
//		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
//		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
//		responseObj.setPayload(expResult);
//		return responseObj;
//	}

//	private ResponseObject manageRemoteConf(RequestObject reqObj) {
//		CompLifecycleManager compLifeCycleMgr = CompLifecycleManagerImpl.getInstance(reqObj.getTargetIdentifier());
//		boolean updateResult = compLifeCycleMgr.remoteConf(reqObj.getPayload());
//		
//		ResponseObject responseObj = new ResponseObject();
//		responseObj.setProtocol(reqObj.getProtocol());
//		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
//		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
//		responseObj.setPayload("updateResult:" + updateResult);
//		return responseObj;
//	}
	
	/**
	 * process an ondemand msg
	 * @param reqObj
	 * @return
	 */
//	private ResponseObject manageOndemand(RequestObject reqObj) {
//		NodeManager nodeMgr = NodeManager.getInstance();
//		OndemandSetupHelper ondemandSetupHelper = nodeMgr.getOndemandSetupHelper(reqObj.getTargetIdentifier());
//		boolean ondemandResult = false;
////		if(reqObj.getSrcIdentifier() == null || reqObj.getSrcIdentifier().equals("null")
////				|| reqObj.getSrcIdentifier().equals("")){
////			ondemandResult = ondemandSetup.ondemandSetup();
////		} else{
//			
//			ondemandResult = ondemandSetupHelper.ondemandSetup(reqObj.getSrcIdentifier(), reqObj.getProtocol(), reqObj.getPayload());
////		}
//		
//		ResponseObject responseObj = new ResponseObject();
//		responseObj.setProtocol(reqObj.getProtocol());
//		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
//		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
//		responseObj.setPayload("ondemandResult:" + ondemandResult);
//		return responseObj;
//	}
//
//	/**
//	 * process an msg about dependence 
//	 * @param reqObj
//	 * @return
//	 */
//	private ResponseObject manageDep(RequestObject reqObj) {
//		NodeManager nodeMgr = NodeManager.getInstance();
//		DynamicDepManager ddm = nodeMgr.getDynamicDepManager(reqObj.getTargetIdentifier());
////		if(reqObj.getPayload().contains("NOTIFY_REMOTE_UPDATE_DONE")){
////			LOGGER.warning("received NOTIFY_REMOTE_UPDATE_DONE src:" + reqObj.getSrcIdentifier() + 
////					" target:" + reqObj.getTargetIdentifier());
////		}
//		boolean manageResult = ddm.manageDependence(reqObj.getProtocol(), reqObj.getPayload());
//		ResponseObject responseObj = new ResponseObject();
//		responseObj.setProtocol(reqObj.getProtocol());
//		responseObj.setSrcIdentifier(reqObj.getTargetIdentifier());
//		responseObj.setTargetIdentifier(reqObj.getSrcIdentifier());
//		responseObj.setPayload("manageDepResult:" + manageResult);
//		return responseObj;
//	}

	@Override
	public void registerObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		if(observers.contains(o))
			observers.remove(o);
	}

	@Override
	public void notifyObservers(Object arg) {
		for(int i = 0; i < observers.size(); i++){
			observers.get(i).update(this, arg);
		}
	}

	@Override
	public void setResult(String result) {
		this.result = result;
	}

}
