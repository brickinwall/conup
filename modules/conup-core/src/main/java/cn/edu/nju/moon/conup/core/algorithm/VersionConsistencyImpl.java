/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.core.DependenceRegistryImpl;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
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

		TxEventType txEventType = txContext.getEventType();
		String rootTx = txContext.getRootTx();
		String currentTx = txContext.getCurrentTx();
		String parentTx = txContext.getParentTx();
		String hostComponent = txContext.getHostComponent();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(hostComponent);
		assert dynamicDepMgr != null;
		DependenceRegistryImpl inDepRegistry = dynamicDepMgr.getInDepRegistry();
		DependenceRegistryImpl outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		Set<String> pastComponents = txContext.getPastComponents();
		Set<String> futureComponents = txContext.getFutureComponents();
		
		boolean isRoot = false;
		
		if (true) {

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
			
			
		} else {
			/*
			 * not valid == normal do not maintain dependences
			 */
			//TODO do something when ondemand setup!
			
			
			if (txEventType.equals(TxEventType.TransactionStart)) {
				
			} else if (txEventType.equals(TxEventType.DependencesChanged)) {
//				TransactionRegistry.getInstance().ge
				
				
			} else if (txEventType.equals(TxEventType.TransactionEnd)) {

			} else {
				//TxEventType.FirstRequestService
				//start setup();
				DepNotifyService depNotifyService = null;// = new DepNotifyService();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), "CONSISTENCY", "msgType", "payload");
			} 
			
		}
	}


	@Override
	public boolean manageDependence(String proctocol, String msgType,
			String payload) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/** return current thread ID. */
	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	@Override
	public String getAlgorithmType() {
		return VersionConsistencyImpl.ALGORITHM_TYPE;
	}

}
