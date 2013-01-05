package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ExtensionOndemandThread extends Thread {
	private OndemandSetup ondemandSetup = null; 
	private ComponentObject compObj;
	public ExtensionOndemandThread(OndemandSetup ondemandSetup, ComponentObject compObj){
		this.ondemandSetup = ondemandSetup;
		this.compObj = compObj;
	}
	
	public void run(){
		ondemandSetup.ondemand();
	}
}
