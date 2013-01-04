package cn.edu.nju.moon.conup.sample.portal.services;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.communication.services.OndemandService;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.def.ComponentStatus;
import cn.edu.nju.moon.conup.def.Scope;

public class UpdateThread extends Thread {
	private String targetComponent;
	public UpdateThread(String targetComponent){
		this.targetComponent = targetComponent;
	}
	public void run(){
		Node node = VcContainerImpl.getInstance().getCommunicationNode();
		String endPoint;
//		String targetComponent;
//		targetComponent = "AuthComponent";
		
		endPoint = targetComponent + "Comm#service-binding(OndemandService/OndemandService)";
		OndemandService ondemandService;
		try {
			ondemandService = node.getService(OndemandService.class, endPoint);
			ondemandService.reqOndemandSetup(targetComponent, 
					targetComponent, new Scope(), ComponentStatus.WAITING);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}//END CLASS
