package cn.edu.nju.moon.conup.remote.services.impl;

import cn.edu.nju.moon.conup.communication.client.AsynCommClient;
import cn.edu.nju.moon.conup.communication.client.SynCommClient;
import cn.edu.nju.moon.conup.remote.model.UpdateContext;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;

/**
 * @author rgc
 * @version Nov 28, 2012 4:54:05 PM
 */
public class RemoteConfServiceImpl {

	// String targetIdentifier, String baseDir,String classPath, String
	// contributionURI, String compositeURI

	public boolean update(UpdateContext updateContext) {
		return true;
	}

	public boolean update(String ip, int port, String targetIdentifier,
			String proctocol, String baseDir, String classFilePath,
			String contributionUri, String compsiteUri) {
		MsgType msgType = MsgType.REMOTE_CONF_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.UPDATE, targetIdentifier, baseDir,
				classFilePath, contributionUri, compsiteUri);
		SynCommClient asynCommClient = new SynCommClient();
		asynCommClient.sendMsg(ip, port, null, targetIdentifier, proctocol,
				msgType, payload);
		return true;
	}

	public boolean ondemand(String ip, int port, String targetIdentifier,
			String proctocol) {
		MsgType msgType = MsgType.REMOTE_CONF_MSG;
		String payload = UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.ONDEMAND, targetIdentifier);
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
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "AuthComponent";
		int port = 18082;
		String baseDir = "/home/nju/deploy/sample/update";
		String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
		String contributionUri = "conup-sample-auth";
		String compsiteUri = "auth.composite";
		rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir,
				classFilePath, contributionUri, compsiteUri);
		// rcs.ondemand("localhost", port , targetIdentifier, "CONSISTENCY");
	}

}
