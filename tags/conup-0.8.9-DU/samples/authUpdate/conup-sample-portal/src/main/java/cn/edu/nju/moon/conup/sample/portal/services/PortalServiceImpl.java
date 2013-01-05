package cn.edu.nju.moon.conup.sample.portal.services;


import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

@Service(PortalService.class)
public class PortalServiceImpl implements PortalService {
	private TokenService tokenService;
	private ProcService procService;
	
	private String version = "version.1";

	public TokenService getTokenService() {
		return tokenService;
	}

	@Reference
	public void setTokenService(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public ProcService getProcService() {
		return procService;
	}

	@Reference
	public void setProcService(ProcService procService) {
		this.procService = procService;
	}

	@Override
	@ConupTransaction
	public String execute(String exeProc, String userName, String passwd) {
		
//		testUpdate();
		String threadID = getThreadID();
		InterceptorCache interceptorCache = InterceptorCache.getInstance("PortalComponent");
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance("PortalComponent");
//		exeRecorder.addAction(rootTx, exeProc);
//		exeRecorder.addAction(rootTx, "ProcComponent.process." + version);
		exeProc += "PortalComponent.execute." + version;
		exeProc = tokenService.getToken(exeProc, "");
		exeProc = procService.process(exeProc, "", "");
		
		exeRecorder.addAction(rootTx, exeProc);
		
		return exeRecorder.getCompleteAction(rootTx);
		
//		String cred = userName + "," + passwd;
//		String token = tokenService.getToken(exeProc, cred);
//		
//		//test
//
//		String data = "";
//		List<String> result = procService.process(exeProc, token, data);
//
//		return result;
	}

	private void testUpdate() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
				String targetIdentifier = "AuthComponent";
				int port = 18082;
				String baseDir = "/home/nju/deploy/sample/update";
				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
				String contributionUri = "conup-sample-auth";
				String compsiteUri = "auth.composite";
				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
				
			}
		});
		
		thread.start();
	}
	
	private void testOndemand() {
		// test
		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
		String targetIdentifier = "AuthComponent";
		int port = 18082;
		rcs.ondemand("10.0.2.15", port, targetIdentifier, "TRANQUILLITY");
		//TRANQUILLITY
		//CONSISTENCY
	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

}
