package cn.edu.nju.moon.conup.ext.comp.manager;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 26, 2013 10:43:16 PM
 */
public class OndemandSetupThread extends Thread {
	private String compIdentifier = null;
	public OndemandSetupThread(String compIdentifier){
		this.compIdentifier = compIdentifier;
	}

	@Override
	public void run() {
		OndemandSetupHelper ondemandHelper;
		NodeManager nodeMgr;
		nodeMgr = NodeManager.getInstance();
		ondemandHelper = nodeMgr.getOndemandSetupHelper(compIdentifier);
		ondemandHelper.ondemandSetup();
	}
	
	
}
