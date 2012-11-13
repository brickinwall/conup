/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.core.ondemand.VersionConsistencyOndemandSetupImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class VersionConsistencyImpl implements Algorithm {
	/** dependence type is "future" */
	public final static String FUTURE_DEP = "FUTURE_DEP";
	/** dependence type is "past" */
	public final static String PAST_DEP = "PAST_DEP";
	/** represent version-consistency algorithm */
	public final static String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
	
	@Override
	public void manageDependence(TransactionContext txContext) {

		String hostComponent = txContext.getHostComponent();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(hostComponent);
		CompStatus compStatus = dynamicDepMgr.getCompStatus();
		
		assert dynamicDepMgr != null;
		
		boolean isRoot = false;
		
		assert compStatus != null;
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, dynamicDepMgr);
			break;
		case VALID:
			doValid(txContext, dynamicDepMgr);
			break;
		case ONDEMAND:
			doOndemand(txContext, dynamicDepMgr);
			break;
		case UPDATING:
			doUpdating(txContext, dynamicDepMgr);
			break;
		case UPDATED:
			doUpdating(txContext, dynamicDepMgr);
			break;
		default:
			System.out.println("default process...");
		}
		
	}
	/**
	 * during notify, the component status is normal, do the following action
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doNormal(TransactionContext txContext, DynamicDepManagerImpl dynamicDepMgr){
		// normal do not maintain dependences
		TxEventType txEventType = txContext.getEventType();
		String rootTx = txContext.getRootTx();
		String currentTx = txContext.getCurrentTx();
		String parentTx = txContext.getParentTx();
		String hostComponent = txContext.getHostComponent();
		ComponentObject currentComp = NodeManager.getInstance().getComponentObject(hostComponent);
		if(txEventType.equals(TxEventType.FirstRequestService)){
			//TxEventType.FirstRequestService
			//start setup();
			VersionConsistencyOndemandSetupImpl vcOndemand = new VersionConsistencyOndemandSetupImpl();
			vcOndemand.ondemand(currentComp.getFreenessConf(), dynamicDepMgr.getScope());
			
//			DepNotifyService depNotifyService = null;// = new DepNotifyService();
//			depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
		}else{
			// ignore all other notify 
			// do nothing
			
		}
		
	}
	
	/**
	 * during notify, the component status is valid, do the following action
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doValid(TransactionContext txContext, DynamicDepManagerImpl dynamicDepMgr) {
		TxEventType txEventType = txContext.getEventType();
		String rootTx = txContext.getRootTx();
		String currentTx = txContext.getCurrentTx();
		String parentTx = txContext.getParentTx();
		String hostComponent = txContext.getHostComponent();
		
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		Set<String> futureComponents = txContext.getFutureComponents();
		boolean isRoot = false;
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
			if (rootTx.equals(currentTx)) {
				/**
				 * current transaction is root
				 */
				isRoot = true;
			}else{
				/*
				 * current transaction is not root
				 * notify parent that a new sub-tx start
				 */
				//TODO how to get this service?
				DepNotifyService depNotifyService = null;// = new DepNotifyService();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
				
			}

			Dependence lfe = new Dependence();
			lfe.setType(FUTURE_DEP);
			lfe.setRootTx(rootTx);
			lfe.setSrcCompObjIdentifier(hostComponent);
			lfe.setTargetCompObjIdentifer(hostComponent);
			if(!inDepRegistry.contain(lfe)){
				inDepRegistry.addDependence(lfe);
			}

			Dependence lpe = new Dependence();
			lpe.setType(PAST_DEP);
			lpe.setRootTx(rootTx);
			lpe.setSrcCompObjIdentifier(hostComponent);
			lpe.setTargetCompObjIdentifer(hostComponent);
			if (!outDepRegistry.contain(lpe)) {
				outDepRegistry.addDependence(lpe);
			}

		} else if (txEventType.equals(TxEventType.DependencesChanged)) {
			//TODO setup is done berfore root's first sub-tx ???
			if(rootTx.equals(currentTx)){// && !isSetupDone
				isRoot = true;
				// start setup
				DepNotifyService depNotifyService = null;// = new DepNotifyService();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
			}
			
			Set<Dependence> futureDepsInODR = outDepRegistry.getDependencesViaType(FUTURE_DEP);
			Set<Dependence> futureDepSameRoot = new ConcurrentSkipListSet<Dependence>();
			for(Dependence dep : futureDepsInODR){
				if(dep.getRootTx().equals(rootTx)){
					futureDepSameRoot.add(dep);
				}
			}
			
			/**
			 * find these components which never be used anymore
			 */
			for(Dependence dep : futureDepSameRoot){
				if(!futureComponents.contains(dep.getTargetCompObjIdentifer()) && !dep.getTargetCompObjIdentifer().equals(hostComponent)){
					//first, remove future dep, need to change interface
					outDepRegistry.removeDependence(dep.getType(), dep.getRootTx(), dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer());
					//notify sub-comp future removed(here must be coincidence with algorithm)
					DepNotifyService depNotifyService = null;// = new DepNotifyService();
					depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
				}
			}
			
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			/**
			 * if currentTx is not root, need to notify parent sub_tx_end
			 * else if currentTx is root, start cleanup
			 */
			if(!currentTx.equals(rootTx)){
				//need to notify parent sub_tx_end
				DepNotifyService depNotifyService = null;// = new DepNotifyService();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
			}else{
				//start cleanup
				DepNotifyService depNotifyService = null;// = new DepNotifyService();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
			}
			
		} else {
			// FirstRequestService
			// do not take any action
		}
	}

	/**
	 * during updating process, we still need to maintain the dependences
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doUpdating(TransactionContext txContext, DynamicDepManagerImpl dynamicDepMgr) {
		doValid(txContext, dynamicDepMgr);
	}

	/**
	 * current component status is ondemand, suspend current execution until 
	 * status becomes to valid
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doOndemand(TransactionContext txContext, DynamicDepManagerImpl dynamicDepMgr) {
		//TODO need to find synchronize monitor
		
		
		// after component status become valid, doValid(...)
		doValid(txContext, dynamicDepMgr);
		
	}

	@Override
	public boolean manageDependence(String proctocol, String msgType, String payload) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getAlgorithmType() {
		return VersionConsistencyImpl.ALGORITHM_TYPE;
	}

}
