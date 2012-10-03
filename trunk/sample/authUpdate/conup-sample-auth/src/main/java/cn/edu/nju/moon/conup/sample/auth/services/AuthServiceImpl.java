package cn.edu.nju.moon.conup.sample.auth.services;

import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;
import cn.edu.nju.moon.conup.pre.DynamicDependency;

import org.oasisopen.sca.annotation.Service;

@Service({TokenService.class, VerificationService.class })
public class AuthServiceImpl implements TokenService,VerificationService {
	
	@VcTransaction
	public String getToken(String cred) {
//		ComponentListener listener = ComponentListenerImpl.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
//		
//		listener.notify("running", threadID, futureC, pastC);
		
		String[] creds = cred.split(",");
		if("nju".equals(creds[0]) && "cs".equals(creds[1])){
			StringBuilder sb = new StringBuilder(cred);
			sb.append(",pass") ;
			System.out.println("version 23...");
//			listener.notify("end", threadID, futureC, pastC);
			return sb.toString();
		}
		StringBuilder tmp = new StringBuilder(cred);
		tmp.append(",fail") ;
//		listener.notify("end", threadID, futureC, pastC);
//		System.out.println("version 18...");
		
		
//		return "nju,cs,pass";
		
		return tmp.toString();
	}
	@VcTransaction
	public Boolean verify(String token) {
//		ComponentListener listener = ComponentListenerImpl.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
		
//		listener.notify("running", threadID, futureC, pastC);
		String[] tokens = token.split(",");
		if(tokens[2].equals("pass")){
//			listener.notify("end", threadID, futureC, pastC);
			return true;
		}
		else if(tokens[2].equals("fail")){
//			listener.notify("end", threadID, futureC, pastC);
			return false;
		}
		else{
//			listener.notify("end", threadID, futureC, pastC);
			return false;
		}
		
	}

}
