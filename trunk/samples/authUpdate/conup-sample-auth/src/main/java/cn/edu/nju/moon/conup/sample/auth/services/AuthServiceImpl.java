package cn.edu.nju.moon.conup.sample.auth.services;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

import org.oasisopen.sca.annotation.Service;

@Service({TokenService.class, VerificationService.class })
public class AuthServiceImpl implements TokenService,VerificationService {
	Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());
	private String versionTag = "_is_old_version";
//	private String versionTag = "_is_new_version";
	
	@VcTransaction
	public String getToken(String cred) {
//		ComponentListener listener = new ComponentListenerImpl();//.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
////		
		
		logger.info("\n\n=======" + Thread.currentThread().hashCode() + " " + Thread.currentThread().toString() +": getToken()" + versionTag + "========\n\n");
		String[] creds = cred.split(",");
		if("nju".equals(creds[0]) && "cs".equals(creds[1])){
			StringBuilder sb = new StringBuilder(cred);
			sb.append(",pass") ;
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			listener.notify("end", threadID, futureC, pastC);
			return sb.append(",thread=" + Thread.currentThread().hashCode() + "_" + Thread.currentThread().toString().replace(",", "_") + "getToken()" + versionTag).toString();
//			return sb.toString();
		}
		StringBuilder tmp = new StringBuilder(cred);
		tmp.append(",fail") ;
		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		listener.notify("end", threadID, futureC, pastC);
		return tmp.append(",thread=" + Thread.currentThread().hashCode() + "_" + Thread.currentThread().toString() + "getToken()" + versionTag).toString();
//		return tmp.toString();
	}
	@VcTransaction
	public Boolean verify(String token) {
//		ComponentListener listener = new ComponentListenerImpl();//.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
		
//		listener.notify("running", threadID, futureC, pastC);
		String[] tokens = token.split(",");
		
		if(tokens[2].equals("pass")){
			logger.info("\n\n=======verify for " + tokens[3] + "===verify()" + versionTag + "========\n\n");
			
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			listener.notify("end", threadID, futureC, pastC);
			return true;
		}
		else if(tokens[2].equals("fail")){
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			listener.notify("end", threadID, futureC, pastC);
			return false;
		}
		else{
			
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			listener.notify("end", threadID, futureC, pastC);
			return false;
		}
		
	}

}
