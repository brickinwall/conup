package cn.edu.nju.moon.conup.sample.proc.services;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.remote.services.impl.RemoteConfServiceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

@Service(ProcService.class)
public class ProcServiceImpl implements ProcService {
	private VerificationService verify;
	private DBService db;
	String version = "version.1";
	public VerificationService getVerify() {
		return verify;
	}

	@Reference
	public void setVerify(VerificationService verify) {
		this.verify = verify;
	}

	public DBService getDb() {
		return db;
	}

	@Reference
	public void setDb(DBService db) {
		this.db = db;
	}

	@ConupTransaction
	public String process(String exeProc, String token, String data) {
		String threadID = getThreadID();
		InterceptorCache interceptorCache = InterceptorCache.getInstance("ProcComponent");
		TransactionContext txContextInCache = interceptorCache.getTxCtx(threadID);
		String rootTx = txContextInCache.getRootTx();
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance("ProcComponent");
//		exeRecorder.addAction(rootTx, exeProc);
//		exeRecorder.addAction(rootTx, "ProcComponent.process." + version);
		exeProc += "; ProcComponent.process." + version;
		
		exeProc = verify.verify(exeProc, token);
		
		exeProc = db.dbOperation(exeProc);
		
		exeRecorder.addAction(rootTx, exeProc);
		
		return exeRecorder.getAction(rootTx);
		
		//test dynamic update
//		testUpdate();
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

//		if (authResult) {
//
//			List<String> result = db.dbOperation(exeProc);
//			return result;
//		} else {
//			return null;
//		}
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
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
}
