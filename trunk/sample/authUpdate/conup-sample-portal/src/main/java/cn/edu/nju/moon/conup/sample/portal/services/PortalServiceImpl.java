package cn.edu.nju.moon.conup.sample.portal.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

//@Service(PortalService.class)
@Service(PortalService.class)
public class PortalServiceImpl implements PortalService {
	private TokenService tokenService;
	private ProcService procService;

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

	public String getToken(String cred) {
		return tokenService.getToken(cred);
	}
	
	@Override
	@VcTransaction
	public List<String> execute(String userName, String passwd) {
//		ComponentListener listener = ComponentListenerImpl.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		futureC.add("AuthComponent");
//		futureC.add("ProcComponent");
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
//		
//		listener.notify("running", threadID, futureC, pastC);
		
		String cred = userName + "," + passwd;
		String token = tokenService.getToken(cred);
		
//		futureC.remove("AuthComponent");
//		pastC.add("AuthComponent");
//		listener.notify("running", threadID, futureC, pastC);
		
		String data ="";
		List<String> result = procService.process(token, data);

//		futureC.remove("ProcComponent");
//		pastC.add("ProcComponent");
//		listener.notify("running", threadID, futureC, pastC);
//		listener.notify("end", threadID, futureC, pastC);
		return result;
		 
	}

}
