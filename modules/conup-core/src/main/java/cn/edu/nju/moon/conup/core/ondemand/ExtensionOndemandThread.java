package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ExtensionOndemandThread extends Thread {
	private OndemandSetup ondemandSetup = null;
	private Scope scope = null;
	
	public ExtensionOndemandThread(OndemandSetup ondemandSetup, Scope scope){
		this.ondemandSetup = ondemandSetup;
		this.scope = scope;
	}
	
	public void run(){
		ondemandSetup.ondemand(scope);
	}
}
