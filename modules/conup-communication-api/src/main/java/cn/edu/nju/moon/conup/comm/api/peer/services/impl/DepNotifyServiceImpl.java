package cn.edu.nju.moon.conup.comm.api.peer.services.impl;

import java.util.logging.Logger;

import cn.edu.nju.conup.comm.api.manager.CommServerManager;
import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.comm.api.utils.CompCommAddress;
import cn.edu.nju.moon.conup.communication.client.AsynCommClient;
import cn.edu.nju.moon.conup.communication.client.SynCommClient;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;

/**
 * @author rgc
 */
public class DepNotifyServiceImpl implements DepNotifyService{

	private static final Logger LOGGER =  Logger.getLogger(DepNotifyServiceImpl.class.getName());
	
	public String synPost(String srcIdentifier, String targetIdentifier,
			String proctocol, MsgType msgType, String payload) {
		// ipAndPort,example-->"ip:10.0.2.15,port:18080"
//		if(payload.contains("NOTIFY_REMOTE_UPDATE_DONE")){
//			LOGGER.warning("src:" + srcIdentifier + " target:"
//					+ targetIdentifier + " payload:" + payload);
//		}
		CompCommAddress ipAndPort = CommServerManager.getInfos(srcIdentifier, targetIdentifier);
		return new SynCommClient().sendMsg(ipAndPort.getIp(), ipAndPort.getPort(), srcIdentifier, targetIdentifier, proctocol, msgType, payload);
	}

	public void asynPost(String srcIdentifier, String targetIdentifier,
			String proctocol, MsgType msgType, String payload) {
		// ipAndPort,example-->"ip:10.0.2.15,port:18080"
		CompCommAddress ipAndPort = CommServerManager.getInfos(srcIdentifier, targetIdentifier);
		if(targetIdentifier.equals("Coordination") && ipAndPort == null) {
			LOGGER.info("In current domain, Coordination component does not exist.");
			return;
		}
		new AsynCommClient().sendMsg(ipAndPort.getIp(), ipAndPort.getPort(), srcIdentifier, targetIdentifier, proctocol, msgType, payload);
	}


}
