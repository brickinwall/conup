package cn.edu.nju.moon.conup.core.manager.impl;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class OndemandSetupHelperImpl implements OndemandSetupHelper{
	private OndemandSetup ondemandSetup = null;
	
	public OndemandSetupHelperImpl(){
		ondemandSetup = new AlgorithmFactory().createOndemandSetup();
	}
	
	/**
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	@Override
	public boolean ondemandSetup(String freenessSetup, Scope scope){
		ondemandSetup.ondemand(freenessSetup, scope);
		return true;
	}

	@Override
	public boolean ondemandSetup(String srcIdentifier, String proctocol,
			String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}
}
