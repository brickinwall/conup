package cn.edu.nju.moon.conup.ext.lifecycle;

import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 * @version Dec 12, 2012 8:09:07 PM
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
