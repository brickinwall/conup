package cn.edu.nju.moon.conup.sample.portal2.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

@Service(PortalService.class)
public class PortalServiceImpl implements PortalService {
	private ProcService procService;
	private TokenService tokenService;
	
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

//	@Override
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
