package cn.edu.nju.moon.conup.sample.auth.services;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

import org.oasisopen.sca.annotation.Service;

@Service({ TokenService.class, VerificationService.class })
public class AuthServiceImpl implements TokenService, VerificationService {
	Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());
	String version = "version.1";
	
	@ConupTransaction
	public String getToken(String exeProc, String cred) {
//		testOndemand();
//		testUpdate();
//		logger.warning("\n\n\n\n=================" + getThreadID()+ ":AuthComponent.getToken(String)"+version + "================\n\n\n\n");
//		String[] creds = cred.split(",");
//		if ("nju".equals(creds[0]) && "cs".equals(creds[1])) {
//			StringBuilder sb = new StringBuilder(cred);
//			sb.append(",pass,").append(version);
//			return sb.toString();
//		}
//		StringBuilder tmp = new StringBuilder(cred);
//		tmp.append(",fail,").append(version);

//		return tmp.toString();
		
//		String threadID = getThreadID();
//		InterceptorCache interceptorCache = InterceptorCache.getInstance("AuthComponent");
//		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
//		String rootTx = txContextInCache.getRootTx();
//		ExecutionRecorder exeRecorder;
//		exeRecorder = ExecutionRecorder.getInstance("AuthComponent");
//		exeRecorder.addAction(rootTx, "");
//		exeRecorder.addAction(rootTx, exeProc);
//		exeRecorder.addAction(rootTx, "AuthComponent.getToken." + version);
//
//		return exeRecorder.getAction(rootTx);
		return exeProc + ", " + "AuthComponent.getToken." + version;
	}

	@ConupTransaction
	public String verify(String exeProc, String token) {
		
//		String[] tokens = token.split(",");
//		if(!tokens[3].equals(version)){
//			String threadID = getThreadID();
//			InterceptorCache interceptorCache = InterceptorCache.getInstance("AuthComponent");
//			TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
//			String rootTx = txContextInCache.getRootTx();
//			
//			throw new InConsistencyException("threadID:" + threadID + "rootTx:" + rootTx + "-----------------" + "[GET_TOKEN, VERIFY]=" + tokens[3] + ", " +version);
//		}
//		logger.warning("\n\n\n\n=================" + getThreadID()+ ":AuthComponent.verify(String)" + version + "================\n\n\n\n");
//		if (tokens[2].equals("pass")) {
//			return true;
//		} else if (tokens[2].equals("fail")) {
//			return false;
//		} else {
//			return false;
//		}
		
		String threadID = getThreadID();
		InterceptorCache interceptorCache = InterceptorCache.getInstance("AuthComponent");
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance("AuthComponent");
		exeRecorder.addAction(rootTx, exeProc);
		exeRecorder.addAction(rootTx, "AuthComponent.verify." + version);

		return exeRecorder.getAction(rootTx);
	}

	//	private void testUpdate() {
//		Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				RemoteConfServiceImpl rcs =  new RemoteConfServiceImpl();
//				String targetIdentifier = "AuthComponent";
//				int port = 18082;
//				String baseDir = "/home/nju/deploy/sample/update";
//				String classFilePath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
//				String contributionUri = "conup-sample-auth";
//				String compsiteUri = "auth.composite";
//				rcs.update("10.0.2.15", port, targetIdentifier, "CONSISTENCY", baseDir, classFilePath, contributionUri, compsiteUri);
//				
//			}
//		});
//		
//		thread.start();
//	}
//	
//	private void testOndemand() {
//		// test
//		RemoteConfServiceImpl rcs = new RemoteConfServiceImpl();
//		String targetIdentifier = "AuthComponent";
//		int port = 18082;
//		rcs.ondemand("10.0.2.15", port, targetIdentifier, "TRANQUILLITY");
//		//TRANQUILLITY
//		//CONSISTENCY
//	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
}
