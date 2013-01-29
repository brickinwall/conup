package cn.edu.nju.moon.conup.sample.configuration.client;

import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

@Service(ConfService.class)
public class ConfServiceImpl implements ConfService {
	private static final Logger LOGGER = Logger.getLogger(ConfServiceImpl.class.getName());

	@Override
	public void ondemand() {
		RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		String targetIdentifier = "AuthComponent";
		int port = 18082;
		rcs.ondemand("114.212.83.140", port, targetIdentifier, "CONSISTENCY");
	}
	@Override
	public void update(String compIdentifier, String baseDir) {
		String classFilePath = null;
		String contributionUri = null;
		String compsiteUri = null;
		int port = 0;
		RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		if (compIdentifier.equals("AuthComponent")) {
			classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
			contributionUri = "conup-sample-auth";
			compsiteUri = "auth.composite";
			port = 18082;
		} else if (compIdentifier.equals("DBComponent")) {
			classFilePath = "cn.edu.nju.moon.conup.sample.db.services.DBServiceImpl";
			contributionUri = "conup-sample-db";
			compsiteUri = "db.composite";
			port = 18081;
		} else if (compIdentifier.equals("ProcComponent")) {
			classFilePath = "cn.edu.nju.moon.conup.sample.proc.services.ProcServiceImpl";
			contributionUri = "conup-sample-proc";
			compsiteUri = "proc.composite";
			port = 18083;
		} else if (compIdentifier.equals("PortalComponent")) {
			classFilePath = "cn.edu.nju.moon.conup.sample.portal.services.PortalServiceImpl";
			contributionUri = "conup-sample-portal";
			compsiteUri = "portal.composite";
			port = 18084;
		} else {
			LOGGER.warning("only support AuthComponent, DBComponent, ProcComponent, PortalComponent update...");
			return;
		}
		
		
		rcs.update("114.212.83.140", port, compIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
	}

}
