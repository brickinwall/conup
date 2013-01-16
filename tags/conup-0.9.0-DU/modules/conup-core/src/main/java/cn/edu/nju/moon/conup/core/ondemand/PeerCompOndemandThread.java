package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class PeerCompOndemandThread extends Thread {
	private OndemandSetup ondemandSetup = null; 
	private String srcIdentifier = null;
	private String proctocol = null;
	private String payload = null;
	
	public PeerCompOndemandThread(OndemandSetup ondemandSetup, String srcIdentifier, 
			String proctocol, String payload){
		this.ondemandSetup = ondemandSetup;
		this.srcIdentifier = srcIdentifier;
		this.proctocol = proctocol;
		this.payload = payload;
	}
	
	public void run(){
		ondemandSetup.ondemandSetup(srcIdentifier, proctocol, payload);
	}
}
