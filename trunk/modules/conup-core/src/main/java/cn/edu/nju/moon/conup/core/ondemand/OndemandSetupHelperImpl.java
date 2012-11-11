package cn.edu.nju.moon.conup.core.ondemand;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
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
	private ComponentObject compObj;
	
	public OndemandSetupHelperImpl(ComponentObject compObj){
		ondemandSetup = new AlgorithmFactory().createOndemandSetup();
		this.compObj = compObj;
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

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}
}
