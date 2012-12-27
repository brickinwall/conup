package cn.edu.nju.moon.conup.communication.services;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.communication.launcher.LaunchCommunication;
import cn.edu.nju.moon.conup.communication.services.OndemandService;
import cn.edu.nju.moon.conup.def.Scope;

public class ReqOndemandSetupSender extends Thread {
	private String currentComponent;
	private String requestSourceComponent;
	private Scope scope;
	private String freenessSetup;
	private Node node;
	
	public ReqOndemandSetupSender(Node node, String currentComponent,
			String requestSourceComponent, Scope scope, String freenessSetup){
		this.node = node;
		this.currentComponent = currentComponent;
		this.requestSourceComponent = requestSourceComponent;
		this.scope = scope;
		this.freenessSetup = freenessSetup;
	}
	
	public void run(){
		String endpoint = null;
		OndemandService ondemandService;
		endpoint = currentComponent + "Comm#service-binding(OndemandService/OndemandService)";
		try {
			ondemandService = node.getService(OndemandService.class,endpoint);
			ondemandService.reqOndemandSetup(currentComponent, requestSourceComponent, scope, freenessSetup);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}

}
