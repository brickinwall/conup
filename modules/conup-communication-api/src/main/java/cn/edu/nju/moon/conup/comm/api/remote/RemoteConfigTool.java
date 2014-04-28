package cn.edu.nju.moon.conup.comm.api.remote;

import cn.edu.nju.moon.conup.communication.client.AsynCommClient;
import cn.edu.nju.moon.conup.communication.client.SynCommClient;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.RemoteConfigContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;

/**
 * @author rgc
 * @version Nov 28, 2012 4:54:05 PM
 */
public class RemoteConfigTool {

	public boolean ondemand(String ip, int port, String targetIdentifier,
			String proctocol, Scope scope) {
		MsgType msgType = MsgType.REMOTE_CONF_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.ONDEMAND, targetIdentifier, scope);
		SynCommClient synCommClient = new SynCommClient();
		synCommClient.sendMsg(ip, port, null, targetIdentifier, proctocol,
				msgType, payload);
		return true;
	}

	public boolean isUpdated(String ip, int port, String targetIdentifier,
			String proctocol) {
		MsgType msgType = MsgType.REMOTE_CONF_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.QUERY, targetIdentifier);
		AsynCommClient asynCommClient = new AsynCommClient();
		asynCommClient.sendMsg(ip, port, null, targetIdentifier, proctocol,
				msgType, payload);
		return true;
	}

	public String getExecutionRecorder(String ip, int port,
			String targetIdentifier, String proctocol) {
		MsgType msgType = MsgType.EXPERIMENT_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.GET_EXECUTION_RECORDER, targetIdentifier);
		SynCommClient synCommClient = new SynCommClient();
		return synCommClient.sendMsg(ip, port, null, targetIdentifier,
				proctocol, msgType, payload);

	}

	public String getUpdateEndTime(String ip, int port,
			String targetIdentifier, String proctocol) {
		MsgType msgType = MsgType.EXPERIMENT_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.GET_UPDATE_ENDTIME, targetIdentifier);
		SynCommClient synCommClient = new SynCommClient();
		return synCommClient.sendMsg(ip, port, null, targetIdentifier,
				proctocol, msgType, payload);
	}

	public static void main(String[] args) {
		RemoteConfigTool rcs = new RemoteConfigTool();
		String targetIdentifier = "AuthComponent";
		String ip = "10.0.2.15";
		int port = 18082;
		String baseDir = "/home/nju/deploy/sample/update";
		String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
		String contributionUri = "conup-sample-auth";
		String compsiteUri = "auth.composite";
		String protocol = "CONSISTENCY";
		RemoteConfigContext rcc = new RemoteConfigContext(ip, port, targetIdentifier, protocol , baseDir, classFilePath, contributionUri, null, compsiteUri);
		rcs.update(rcc);
	}
	
	/**
	 * this method is used in experiment.
	 * when target component is done, it will send a message to coordination component
	 * then coordination component will record the time to calculate all these
	 * affected request.
	 */
	public void notifyUpdateIsDone(String targetComp){
		MsgType msgType = MsgType.EXPERIMENT_MSG;
//		String payload = UpdateContextPayloadCreator.createPayload(
//				UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP);
//		
//		SynCommClient synCommClient = new SynCommClient();
//		return synCommClient.sendMsg(ip, port, null, targetIdentifier,
//				proctocol, msgType, payload);
	}

	public void update(RemoteConfigContext rcc) {
		MsgType msgType = MsgType.REMOTE_CONF_MSG;
		
		
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.UPDATE, rcc);
		SynCommClient asynCommClient = new SynCommClient();
		asynCommClient.sendMsg(rcc.getIp(), rcc.getPort(), null, rcc.getTargetIdentifier(), rcc.getProtocol(),
				msgType, payload);
		return;
	}

}
