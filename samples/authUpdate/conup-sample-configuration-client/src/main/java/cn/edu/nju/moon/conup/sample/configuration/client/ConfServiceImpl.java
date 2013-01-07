package cn.edu.nju.moon.conup.sample.configuration.client;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;

@Service(ConfService.class)
public class ConfServiceImpl implements ConfService {


	@Override
	public void ondemand() {
		RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		String targetIdentifier = "AuthComponent";
		int port = 18082;
		rcs.ondemand("10.0.2.15", port, targetIdentifier, "CONSISTENCY");
	}
	@Override
	public void update(String baseDir) {
		RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
		String targetIdentifier = "AuthComponent";
		int port = 18082;
//		baseDir = "/home/artemis/Documents/20121225-distribution/tuscany-sca-2.0-DU/samples/update";
		String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
		String contributionUri = "conup-sample-auth";
		String compsiteUri = "auth.composite";
		rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
	}

}
