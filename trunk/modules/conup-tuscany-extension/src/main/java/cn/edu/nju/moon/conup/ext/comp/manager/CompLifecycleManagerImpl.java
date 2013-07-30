package cn.edu.nju.moon.conup.ext.comp.manager;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.utils.TuscanyPayloadResolver;
import cn.edu.nju.moon.conup.ext.utils.TuscanyPayload;
import cn.edu.nju.moon.conup.ext.utils.experiments.DisruptionExp;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.TuscanyOperationType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

/**
 * CompLifecycleManager: manage the component's lifecyle 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 10:56:13 PM
 */
public class CompLifecycleManagerImpl implements CompLifeCycleManager {
	private final static Logger LOGGER = Logger.getLogger(CompLifecycleManagerImpl.class.getName());
	/** ComponentObject that represents current CompLifecycleManager*/
	private ComponentObject compObj;
	/** current component's status */
	private CompStatus compStatus = CompStatus.NORMAL;

	public CompStatus getCompStatus() {
		return compStatus;
	}

	public CompLifecycleManagerImpl(ComponentObject compObj){
		this.compObj = compObj;
	}
	
	@Override
	public boolean stop(String contributionURI){
//		boolean isStopped = true;
		//TODO every node could install only one contribution?
//		return uninstall(contributionURI);
		return false;
	}
	

	@Override
	public void setCompObject(ComponentObject compObj){
		this.compObj = compObj;
	}
	
	@Override
	public ComponentObject getCompObject(){
		return compObj;
	}
	
	public String experimentResult(String payload){
		TuscanyPayloadResolver payloadResolver = new TuscanyPayloadResolver(payload);
		TuscanyOperationType opTyep = payloadResolver.getOperation();
		String compIdentifier = payloadResolver.getParameter(TuscanyPayload.COMP_IDENTIFIER);
		if(opTyep.equals(TuscanyOperationType.GET_EXECUTION_RECORDER)){
			return ExecutionRecorder.getInstance(compIdentifier).getActionsAndClear();
		} else if(opTyep.equals(TuscanyOperationType.GET_UPDATE_ENDTIME)){
			return Long.toString(PerformanceRecorder.getInstance(compIdentifier).getUpdateEndTime());
		} else if(opTyep.equals(TuscanyOperationType.NOTIFY_COORDINATIONIN_TRANQUILLITY_EXP)){
			DisruptionExp.getInstance().setUpdateEndTime(System.nanoTime());
			return "ok";
		} else{
			LOGGER.warning("unsupported operation type for experiment");
		}
		//TODO 
		return "no results";
	}
	

	@Override
	public boolean isReadyForUpdate() {
		DynamicDepManager depMgr = NodeManager.getInstance().getDynamicDepManager(compObj.getIdentifier());
		return compStatus.equals(CompStatus.VALID) && depMgr.isReadyForUpdate();
	}

	@Override
	public void transitToNormal(){
		synchronized (compStatus) {
			// only when CompStatus is valid or updating, it can be transitToNormal
			if(compStatus.equals(CompStatus.VALID) || compStatus.equals(CompStatus.UPDATING)){
				compStatus = CompStatus.NORMAL;
			}
		}
	}

	@Override
	public void transitToOndemand() {
		this.compStatus = CompStatus.ONDEMAND;
	}

	@Override
	public void transitToValid() {
		this.compStatus = CompStatus.VALID;
	}

	@Override
	public void transitToUpdating(){
		assert compStatus.equals(CompStatus.Free);
		compStatus = CompStatus.UPDATING;
	}

	@Override
	public void transitToFree(){
		assert compStatus.equals(CompStatus.VALID);
		compStatus = CompStatus.Free;
	}

}
