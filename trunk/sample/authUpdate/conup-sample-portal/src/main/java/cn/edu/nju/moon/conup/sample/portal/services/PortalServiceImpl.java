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

import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.data.MessageQueue;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.domain.services.TransactionIDService;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;
import cn.edu.nju.moon.conup.pre.DynamicDependency;

//@Service(PortalService.class)
@Service({PortalService.class, NotifyService.class})
public class PortalServiceImpl implements PortalService, NotifyService {
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
//	@Override
//	public List<String> process(String token, String data) {
//		return procService.process(token, data);
//	}
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
		
//		listener.notify("running", threadID, futureC, pastC);
		
		String cred = userName + "," + passwd;
		
		
//		test
//		String targetEndpoint = "AuthComponentComm#service-binding(UpdateService/UpdateService)";
//		String targetEndpoint = "DomainManagerComponent#service-binding(DomainComponentUpdateService/DomainComponentUpdateService)";
//		DomainComponentUpdateService updateService;
//		try {
//			System.out.println("Access DomainManagerComponent Update Service...");
//			
//			updateService = VcContainerImpl.getInstance().getCommunicationNode().getService(
//					DomainComponentUpdateService.class, targetEndpoint);
//			boolean result = updateService.onDemandRequest("AuthComponent", "WF");
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
//		}
		
		String token = tokenService.getToken(cred);
		
		
//		futureC.remove("AuthComponent");
//		pastC.add("AuthComponent");
//		listener.notify("running", threadID, futureC, pastC);
		
//		ComponentUpdateService cu = null;
//		try {
//			cu = VcContainerImpl.getInstance().getCommunicationNode().getService(ComponentUpdateService.class, "AuthComponentComm#service-binding(ComponentUpdateService/ComponentUpdateService)");
////			String baseDir = "/home/nju/workspace/vc-policy-auth-node/target/classes";
////			String classpath = "cn.edu.nju.moon.vc.auth.services.AuthServiceImpl";
//			String classpath = "cn.edu.nju.moon.vc.auth.services.AuthServiceImpl";
//			String baseDir = "/home/nju/classes";
//			String contributionURI = "vc-policy-auth-node";
//			String compositeURI = "auth.composite";
//			cu.update(baseDir, classpath, contributionURI, compositeURI);
//		} catch (NoSuchServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String data ="";
		
		List<String> result = procService.process(token, data);

//		System.out.println("After procService.process(...)");
//		System.out.println("Press ENTER to continue");
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();							
//		}
		
//		futureC.remove("ProcComponent");
//		pastC.add("ProcComponent");
//		listener.notify("running", threadID, futureC, pastC);
//		listener.notify("end", threadID, futureC, pastC);
		return result;
		 
	}
	@Override
	public boolean notifyInterceptor() {
//		System.out.println("in notifyInterceptor:");
//		System.out.println(VcContainerImpl.getInstance().getComponentStatus().getCurrentStatus());
//		MessageQueue ms = VcContainerImpl.getInstance().getMessageQueue();
//		System.out.println(ms);
//		VcContainer vcContaner = VcContainerImpl.getInstance();
//		vcContaner.getComponentStatus().setCurrentStatus(ComponentStatus.VALID);
//		System.out.println(VcContainerImpl.getInstance().getComponentStatus().getCurrentStatus());
//		Map<PhasedInterceptor, Queue<Message>> msgs =  ms.getMsgMap();
//		Iterator iterator = msgs.entrySet().iterator();
//		while(iterator.hasNext()){
//			System.out.println("in while.....");
//			Map.Entry<BufferPolicyInterceptor, List<Message>> entry = (Entry<BufferPolicyInterceptor, List<Message>>) iterator.next();
//			BufferPolicyInterceptor interceptor = entry.getKey();
//			synchronized(interceptor){
////				interceptor.notifyAll();
//				interceptor.notifyAll();
//			}
//		}
		return true;
	}

}
