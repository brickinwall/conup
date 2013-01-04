package cn.edu.nju.moon.conup.communication.services;

import cn.edu.nju.moon.conup.communication.services.OndemandService;

public class ConfirmOndemandSetupSender extends Thread {
	private OndemandService ondemandService;
	private String hostComponent;
	private String subComponent;
	public ConfirmOndemandSetupSender(OndemandService ondemandService, 
			String hostComponent, String subComponent){
		this.ondemandService = ondemandService;
		this.hostComponent = hostComponent;
		this.subComponent = subComponent;
	}
	
	public void run(){
		ondemandService.confirmOndemandSetup(hostComponent, subComponent);
	}

}
