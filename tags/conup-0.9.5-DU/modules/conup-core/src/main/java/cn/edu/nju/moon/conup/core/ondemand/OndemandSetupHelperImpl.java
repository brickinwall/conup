package cn.edu.nju.moon.conup.core.ondemand;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class OndemandSetupHelperImpl implements OndemandSetupHelper{
	private final static Logger LOGGER = Logger.getLogger(OndemandSetupHelperImpl.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	private DynamicDepManager depMgr = null;
	private OndemandSetup ondemandSetup = null;
	private ComponentObject compObj;
	
	/** Has on-demand setup already received?   */
	private boolean isOndemandRqstRcvd = false;
	
	public OndemandSetupHelperImpl(){
		
	}
	
	/**
	 * once receive ondemand msg from remote config service
	 * invoke OndemandSetup and begin on-demand setup
	 * @return
	 */
	@Override
	public boolean ondemandSetup(){
		
		if(isOndemandRqstRcvd){
			LOGGER.info("duplicated OndemandSetup request for component " + compObj.getIdentifier());
			return true;
		}
		LOGGER.fine("-----------------received ondemand setup request.");
		isOndemandRqstRcvd = true;
		
		ondemandSetup.setOndemandHelper(this);
		
		ExtensionOndemandThread extOndemandThread;
		extOndemandThread = new ExtensionOndemandThread(ondemandSetup, compObj);
		extOndemandThread.start();
		
//		ondemandSetup.ondemand();
		return true;
	}

	@Override
	public boolean ondemandSetup(String srcIdentifier, String proctocol, String payload) {
		ondemandSetup.setOndemandHelper(this);
		
		PeerCompOndemandThread peerCompThread;
		peerCompThread = new PeerCompOndemandThread(ondemandSetup, srcIdentifier, proctocol, payload);
		peerCompThread.start();
		
//		ondemandSetup.ondemandSetup(srcIdentifier, proctocol, payload);
		return true;
	}

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	@Override
	public void setOndemand(OndemandSetup ondemandSetup) {
		this.ondemandSetup = ondemandSetup;
	}

	@Override
	public void setDynamicDepManager(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
	}

	@Override
	public DynamicDepManager getDynamicDepManager() {
		return depMgr;
	}
	
	public boolean isOndemandRqstRcvd() {
		return isOndemandRqstRcvd;
	}
	
	@Override
	public void resetIsOndemandRqstRcvd(){
		isOndemandRqstRcvd = false;
	}
	
	public boolean isOndemandDone(){
		return ondemandSetup.isOndemandDone();
	}
	
	public void onDemandIsDone(){
		ondemandSetup.onDemandIsDone();
	}
	
}
