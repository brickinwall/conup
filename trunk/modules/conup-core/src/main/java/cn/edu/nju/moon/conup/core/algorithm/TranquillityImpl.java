/**
 * 
 */
package cn.edu.nju.moon.conup.core.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.comm.api.peer.services.impl.DepNotifyServiceImpl;
import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayloadCreator;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;


/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TranquillityImpl implements Algorithm {
	
	/** dependence type is "future" */
	public final static String FUTURE_DEP = "FUTURE_DEP";
	/** dependence type is "past" */
	public final static String PAST_DEP = "PAST_DEP";
	private static final Logger LOGGER = Logger.getLogger(TranquillityImpl.class.getName());
	/** represent tranquillity algorithm */
	public final static String ALGORITHM_TYPE = "TRANQUILLITY_ALGORITHM";
	
	public static Map<String, Boolean> isSetupDone = new ConcurrentHashMap<String, Boolean>();
	
	@Override
	public void manageDependence(TransactionContext txContext, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		
		LOGGER.fine("TranquillityImpl.manageDependence(...)");
		CompStatus compStatus = compLifeCycleMgr.getCompStatus();
		
		assert depMgr != null;
		
		assert compStatus != null;
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, depMgr, compLifeCycleMgr);
			break;
		case VALID:
			doValid(txContext, depMgr);
			break;
		case ONDEMAND:
			doOndemand(txContext, depMgr, compLifeCycleMgr);
			break;
		case UPDATING:
			doValid(txContext, depMgr);
			break;
		case FREE:
			doValid(txContext, depMgr);
			break;
		default:			
			LOGGER.warning("process nothing for rootTx:" + txContext.getRootTx() + "----------compStatus-------->" + compStatus);
			LOGGER.fine("default process...");
		}
		
	}
	
	@Override
	public boolean manageDependence(DepOperationType operationType, Map<String, String> params, 
			DynamicDepManager depMgr,
			CompLifeCycleManager compLifeCycleMgr) {
		if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL))
			return true;
		
		boolean manageDepResult = false;
		
		String srcComp = params.get("srcComp");
		String targetComp = params.get("targetComp");
		String rootTx = params.get("rootTx");
		
		assert operationType != null;
		
		
		switch(operationType){
		case NOTIFY_FUTURE_CREATE:
//			LOGGER.fine("before process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyFutureCreate(srcComp, targetComp, rootTx, depMgr);
			
//			LOGGER.fine("after process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_FUTURE_REMOVE:
//			LOGGER.fine("before process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyFutureRemove(srcComp, targetComp, rootTx, depMgr);
			
//			LOGGER.fine("after process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			break;
		case ACK_SUBTX_INIT:
//			LOGGER.fine("before process ACK_SUBTX_INIT:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			String parentTxID = params.get("parentTx");
			String subTxID = params.get("subTx");
			manageDepResult = doAckSubTxInit(srcComp, targetComp, rootTx, parentTxID, subTxID, depMgr, compLifeCycleMgr);
//			
//			LOGGER.fine("after process ACK_SUBTX_INIT:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_PAST_CREATE:
//			LOGGER.fine("before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyPastCreate(srcComp, targetComp, rootTx, depMgr);
			
//			LOGGER.fine("after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_PAST_REMOVE:
			manageDepResult = doNotifyPastRemove(srcComp, targetComp, rootTx, depMgr);
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			manageDepResult = doNotifyRemoteUpdateDone(srcComp, targetComp, depMgr);
			break;
		default:
			break;
		}
		return manageDepResult;
	}
	
	private void doNormal(TransactionContext txCtx, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr){
		if(txCtx.getEventType().equals(TxEventType.TransactionEnd)){
			String hostComp;
			
			hostComp = txCtx.getHostComponent();
			
			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL)){
					//TODO need to think more about rootTxEnd
					depMgr.getTxs().remove(txCtx.getCurrentTx());
					depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, txCtx.getRootTx());
					
//					Printer printer = new Printer();
//					LOGGER.fine("depMgr.getTxs:");
//					printer.printTxs(LOGGER, depMgr.getTxs());
//					printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "---in----");
					return;
				} else{
					try {
						if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
							LOGGER.fine("----------------suspend thread due to ondemandSyncMonitor.wait()------------");
							ondemandSyncMonitor.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			doValid(txCtx, depMgr);
			
		}
		
	}
	
	/**
	 * during notify, the component status is valid, do the following action
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doValid(TransactionContext txContext, DynamicDepManager depMgr) {
		TxEventType txEventType = txContext.getEventType();
		String parentTx = txContext.getParentTx();
		String currentTx = txContext.getCurrentTx();
		String hostComponent = txContext.getHostComponent();
		
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		
		TxDepRegistry txDepRegistry = NodeManager.getInstance().getTxDepMonitor(hostComponent).getTxDepRegistry();
		Set<String> futureComponents = txDepRegistry.getLocalDep(currentTx).getFutureComponents();
		
		Set<String> targetRef = null;
		Scope scope;
		
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
		String rootTx = null;
		
		//calculate target(sub) components
		if(scope == null){
			targetRef.addAll(depMgr.getStaticDeps());
		}
		else{
			targetRef.addAll(scope.getSubComponents(hostComponent));
		}
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
			Set<String> parentComps;
			if(scope == null){
				parentComps = depMgr.getStaticInDeps();
			} else{
				parentComps = scope.getParentComponents(hostComponent);
			}
			if(parentComps != null && parentComps.size() != 0){
				rootTx = parentTx;
			} else{
				rootTx = currentTx;
			}
			
			Dependence lfe = new Dependence(FUTURE_DEP, rootTx, hostComponent, hostComponent, null, null);
			if(!inDepRegistry.contain(lfe)){
				inDepRegistry.addDependence(lfe);
			}
			if (!outDepRegistry.contain(lfe)) {
				outDepRegistry.addDependence(lfe);
			}

			Dependence lpe = new Dependence(PAST_DEP, rootTx, hostComponent, hostComponent, null, null);
			if(!inDepRegistry.contain(lpe)){
				inDepRegistry.addDependence(lpe);
			}
			if (!outDepRegistry.contain(lpe)) {
				outDepRegistry.addDependence(lpe);
			}
			
//			NodeManager nodeMgr = NodeManager.getInstance();
//			Printer printer = new Printer();
//			LOGGER.fine("doVALID(TxEventType.TransactionStart):print deps:");
//			DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComponent);
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out");
			
			/* ACK_SUBTX_INIT
			 * current transaction is not root
			 * notify parent that a new sub-tx start
			 */
			if( !parentTx.equals(currentTx) ) {
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), parentTx, DepOperationType.ACK_SUBTX_INIT, txContext.getParentTx(), txContext.getCurrentTx());
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), CommProtocol.TRANQUILLITY, MsgType.DEPENDENCE_MSG, payload);
			}
			
		} else if (txEventType.equals(TxEventType.DependencesChanged)) {
			
			Set<Dependence> futureDepsInODR = outDepRegistry.getDependencesViaType(FUTURE_DEP);
			Set<Dependence> futureDepSameRoot = new ConcurrentSkipListSet<Dependence>();
			for(Dependence dep : futureDepsInODR){
				if(dep.getRootTx().equals(currentTx)){
					futureDepSameRoot.add(dep);
				}
			}
			boolean hasFutureInDep = false;
			Set<Dependence> futureDepsInIDR = inDepRegistry.getDependencesViaType(FUTURE_DEP);
			for(Dependence dep : futureDepsInIDR){
				if(dep.getRootTx().equals(currentTx)){
					hasFutureInDep = true;
					break;
				}
			}
			/*
			 * during tx running, find some components will never be used anymore
			 * if current component do not have any other future in deps, delete the future deps from current to
			 * sub components.
			 */
			for(Dependence dep : futureDepSameRoot){
				if(!hasFutureInDep && !futureComponents.contains(dep.getTargetCompObjIdentifer()) && !dep.getTargetCompObjIdentifer().equals(hostComponent)){
					outDepRegistry.removeDependence(dep.getType(), dep.getRootTx(), dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer());
					if(targetRef.contains(dep.getTargetCompObjIdentifer())){
						//notify sub-comp future removed(here must be coincidence with algorithm)
						String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), currentTx, DepOperationType.NOTIFY_FUTURE_REMOVE);
						DepNotifyService depNotifyService = new DepNotifyServiceImpl();
						depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
					}
				}
			}
			
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			/**
			 * if currentTx is not root, need to notify parent sub_tx_end
			 * else if currentTx is root, start cleanup
			 * 
			 * In tranquillity algorithm, there is only one layer between
			 * parent component and target component, so when current component
			 * is target component, then current tx hosted in current component 
			 * is not root tx
			 */
			assert scope != null;
			boolean isTarget = scope.isTarget(hostComponent);
			if(isTarget){
				
			} else{
				inDepRegistry.removeDependence(FUTURE_DEP, currentTx, hostComponent, hostComponent);
				inDepRegistry.removeDependence(PAST_DEP, currentTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(FUTURE_DEP, currentTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(PAST_DEP, currentTx, hostComponent, hostComponent);
				assert depMgr.getScope() != null;
				removeAllEdges(hostComponent, currentTx, depMgr);
			}
			
		} else {
			// txEventType.equals(FirstRequestService)
			Set<String> fDeps = txDepRegistry.getLocalDep(currentTx).getFutureComponents();
			Iterator<String> depIterator = fDeps.iterator();
			DepNotifyService depNotifyService = new DepNotifyServiceImpl();
			while (depIterator.hasNext()) {
				String targetComp = depIterator.next();
				if (targetRef.contains(targetComp)) {
					Dependence dep = new Dependence(FUTURE_DEP, currentTx,
							hostComponent, targetComp, null, null);
					if (!outDepRegistry.contain(dep)) {
						outDepRegistry.addDependence(dep);
						String payload = TranquillityPayloadCreator
								.createPayload(	hostComponent, targetComp, currentTx, DepOperationType.NOTIFY_FUTURE_CREATE);
						depNotifyService.synPost(hostComponent, targetComp, CommProtocol.TRANQUILLITY, MsgType.DEPENDENCE_MSG, payload);
					}
				}
			}

		}
		//remove tx ctx from TxRegistry
//		depMgr.getTxs().remove(currentTx);
	}

	/**
	 * current component status is ondemand, suspend current execution until 
	 * status becomes to valid
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doOndemand(TransactionContext txContext, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		// sleep until current status become valid
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
					System.out
							.println("----------------ondemandSyncMonitor.wait();tranquillity algorithm------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// after component status become valid, doValid(...)
		doValid(txContext, depMgr);
		
	}
	
	@Override
	public String getAlgorithmType() {
		return TranquillityImpl.ALGORITHM_TYPE;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps) {
		Set<String> oldRootTx = new HashSet<String>();
		for(Dependence dep : allDeps){
			if(dep.getType().equals(PAST_DEP))
				oldRootTx.add(dep.getRootTx());
		}
		
		LOGGER.fine("in tranquillity algorithm(allInDeps):" + allDeps);
		LOGGER.fine("in tranquillity algorithm(oldrootTxs):" + oldRootTx);
		return oldRootTx;
	}

	@Override
	public boolean readyForUpdate(String compIdentifier, DynamicDepManager depMgr) {
		Set<Dependence> rtInDeps;
		rtInDeps = depMgr.getRuntimeInDeps();
		
		Set<String> allRootTxs = new HashSet<String>();
		for(Dependence dep : rtInDeps)
			allRootTxs.add(dep.getRootTx());
		
		boolean freeFlag = true;
		for(String tmpRoot : allRootTxs){
			Set<Dependence> deps = getDepsBelongToSameRootTx(tmpRoot, rtInDeps);
			boolean pastFlag = false;
			boolean futureFlag = false;
			for (Dependence dependence : deps) {
				if(dependence.getType().equals(PAST_DEP)){
					pastFlag = true;
				} else{
					futureFlag = true;
				}
			}
			if( pastFlag&&futureFlag ){
				freeFlag = false;
				break;
			}
			
		}
		return freeFlag;
	}
	
	/**
	 * notify sub component there is future dep should be established
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyFutureCreate(String srcComp, String targetComp, String rootTx, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " rootTx: " + rootTx);
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		Dependence dep = new Dependence(FUTURE_DEP, rootTx, srcComp, targetComp, null, null);
		inDepRegistry.addDependence(dep);
		
		return true;
	}

	/**
	 * be notified that a past dep removed
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyPastRemove(String srcComp, String targetComp, String rootTx, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " rootTx: " + rootTx);
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		inDepRegistry.removeDependence(PAST_DEP, rootTx, srcComp, targetComp);
		LOGGER.fine(PAST_DEP + " rootTx:" + rootTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
		
		//remove tx
		TransactionContext txCtx = null;
		for (Entry<String, TransactionContext> entry : depMgr.getTxs().entrySet()) {
			txCtx = entry.getValue();
			if (txCtx.getParentTx().equals(rootTx) && !txCtx.isFakeTx()) {
				depMgr.getTxs().remove(entry.getKey());
			}
		}
		
		depMgr.getTxLifecycleMgr().rootTxEnd(targetComp, rootTx);
		
		return true;
	}
	
	/**
	 * be notified that a past dep create
	 * add src--->target past dep
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyPastCreate(String srcComp, String targetComp, String rootTx, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " rootTx: " + rootTx);
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		Dependence dependence = new Dependence(PAST_DEP, rootTx, srcComp, targetComp, null, null);
		inDepRegistry.addDependence(dependence);
		
		Map<String, TransactionContext> txs = depMgr.getTxs();
		Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet().iterator();
		// flag indicate that there will be no localTxs
		// whose rootTx is T
		boolean flag = false;
		while(iterator.hasNext()){
			TransactionContext tc = iterator.next().getValue();
			if(tc.getParentTx().equals(rootTx) && !tc.getEventType().equals(TxEventType.TransactionEnd) && !tc.isFakeTx()){
				flag = true;
				break;
			}
		}
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		if(!flag){
			inDepRegistry.removeDependence(FUTURE_DEP, rootTx, targetComp, targetComp);
			inDepRegistry.removeDependence(PAST_DEP, rootTx, targetComp, targetComp);
			outDepRegistry.removeDependence(FUTURE_DEP, rootTx, targetComp, targetComp);
			outDepRegistry.removeDependence(PAST_DEP, rootTx, targetComp, targetComp);
		}
		
		depMgr.dependenceChanged(targetComp);
		
		return true;
	}
	
	/**
	 * be notified sub tx ends.
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifySubTxEnd(String srcComp, String targetComp, String rootTx, String parentTx, String subTx, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " subTx:" + subTx + " rootTx:" + rootTx);
		
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			LOGGER.fine("doNotifySubTxEnd rootTx:" + parentTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
			//maintain tx
			Map<String, TransactionContext> allTxs = depMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTx);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTx, srcComp);
			subTxStatuses.put(subTx, TxEventType.TransactionEnd);
//			txCtx.getPastComponents().add(srcComp);
			
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			Scope scope = depMgr.getScope();
			if(scope!=null && !scope.getSubComponents(targetComp).contains(srcComp)){
				return true;
			}
			
			Set<String> subComps;
			if(scope == null){
				subComps = depMgr.getStaticDeps();
			} else{
				subComps = scope.getSubComponents(targetComp);
			}
			
			if(subComps.contains(srcComp)){
				DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
				Dependence dependence = new Dependence(PAST_DEP, parentTx, targetComp, srcComp, null, null);
				outDepRegistry.addDependence(dependence);
				
				// notify past dep create
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				String payload = TranquillityPayloadCreator.createPayload(targetComp, srcComp, parentTx, DepOperationType.NOTIFY_PAST_CREATE);
				depNotifyService.synPost(targetComp, srcComp, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				
				// TODO remove PAST_DEP should be deferred to cleanup 
//				if(txCtx.getTxDepMonitor().isLastUse(txCtx.getCurrentTx(), srcComp, txCtx.getHostComponent())){
//					payload = TranquillityPayloadCreator.createPayload(targetComp, srcComp, rootTx, OperationType.NOTIFY_PAST_REMOVE);
//					depNotifyService.synPost(targetComp, srcComp, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
//					outDepRegistry.removeDependence(dependence);
//				}
			}
			
			return true;
		}
		
	}
	
	/**
	 * be notified that a sub tx being initiated
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @param parentTxID
	 * @param subTxID
	 * @return
	 */
	private boolean doAckSubTxInit(String srcComp, String targetComp, String rootTx, String parentTxID, String subTxID, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			Map<String, TransactionContext> allTxs = depMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTxID);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTxID, srcComp);
			subTxStatuses.put(subTxID, TxEventType.TransactionStart);
			
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			return removeFutureEdges(targetComp, rootTx, parentTxID, subTxID, depMgr);
		}
		
	}
	
	/**
	 * receive NotifyFutureRemove
	 * try to remove src--->target future dep
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyFutureRemove(String srcComp, String targetComp, String rootTx, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " rootTx: " + rootTx);
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		
		inDepRegistry.removeDependence(FUTURE_DEP, rootTx, srcComp, targetComp);
		
		return true;
	}
	
	/**
	 * receive NOTIFY_REMOTE_UPDATE_DONE
	 * change component status from valid --> normal
	 * because in tranquillity we only consider one depth layer, do not need to send to parent any more.
	 * @param srComp
	 * @param hostComp
	 * @return
	 */
	private boolean doNotifyRemoteUpdateDone(String srComp, String hostComp, DynamicDepManager depMgr){
		LOGGER.fine(hostComp + " received notifyRemoteUpdateDone from " + srComp);
		
		//clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
//		depMgr.setScope(null);
		
		UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(hostComp);
		updateMgr.remoteDynamicUpdateIsDone();
		
		return true;
	}
	
	/**
	 * try to remove future dep when receive ACK_SUB_INIT
	 * @param dynamicDepMgr
	 * @param currentComp
	 * @param rootTx
	 * @param currentTxID
	 * @param subTxID
	 * @return
	 */
	private boolean removeFutureEdges(String currentComp, String rootTx, String currentTxID, String subTxID, DynamicDepManager depMgr){
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		Set<Dependence> outFutureDeps = outDepRegistry.getDependencesViaType(FUTURE_DEP);
		Set<Dependence> outFutureOneRoot = new HashSet<Dependence>();
		
		Iterator<Dependence> iterator = outFutureDeps.iterator();
		while(iterator.hasNext()){
			Dependence dep = iterator.next(); 
			if(dep.getRootTx().equals(rootTx) && !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				outFutureOneRoot.add(dep);
			}
		}
		
		boolean inFutureFlag = false;
		Set<Dependence> futureDep = inDepRegistry.getDependencesViaType(FUTURE_DEP);
		iterator = futureDep.iterator();
		while(iterator.hasNext()){
			Dependence dep = iterator.next();
			
			if(dep.getRootTx().equals(rootTx) && !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				inFutureFlag = true;
				break;
			}
		}
		
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		
		if(!inFutureFlag){
//			TransactionContext txContext = TransactionRegistry.getInstance().getTransactionContext(currentTxID);
//			TransactionContext txContext = depMgr.getTxs().get(currentTxID);
			TxDepMonitor txDepMonitor = NodeManager.getInstance().getTxDepMonitor(currentComp);
			
			for(Dependence dep : outFutureOneRoot){
				boolean isLastUse = txDepMonitor.isLastUse(currentTxID, dep.getTargetCompObjIdentifer(), currentComp);
				if(isLastUse){
					outDepRegistry.removeDependence(dep);
					String payload = TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), DepOperationType.NOTIFY_FUTURE_REMOVE);
					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				}else{
				}
			}
			
		}

		return true;
	}
	
	
	private boolean removeAllEdges(String hostComponent, String rootTx, DynamicDepManager depMgr){
		Set<Dependence> rtOutDeps;
		rtOutDeps = depMgr.getRuntimeDeps();
		
		//remove deps
		for(Dependence dep : rtOutDeps){
			if(dep.getRootTx().equals(rootTx) && dep.getType().equals(FUTURE_DEP)
				&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, DepOperationType.NOTIFY_FUTURE_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
			} else if(dep.getRootTx().equals(rootTx) && dep.getType().equals(PAST_DEP)
					&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, DepOperationType.NOTIFY_PAST_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
				LOGGER.fine("removeAllEdges:PAST_DEP:rootTx:" + rootTx + ":srcHost:" + hostComponent + ":targetHost:" + dep.getTargetCompObjIdentifer());
			}
			
			if(dep.getRootTx().equals(rootTx)){
				rtOutDeps.remove(dep);
			}
		}
		
		
		if(depMgr.getTxLifecycleMgr() != null){
			depMgr.getTxLifecycleMgr().rootTxEnd(hostComponent, rootTx);
		} else {
			LOGGER.warning("failed to remove rootTx " + rootTx + ", due to fail to get TxDepMonitor");
		}
		
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		
		assert scope != null;
		parentComps = scope.getParentComponents(hostComponent);
		if(parentComps == null || parentComps.size() == 0 ){
			//remove tx
			for (Entry<String, TransactionContext> entry : depMgr.getTxs().entrySet()) {
				TransactionContext txCtx = entry.getValue();
				if(txCtx.getCurrentTx().equals(rootTx)){
//					txCtx.getTxDepMonitor().rootTxEnd(hostComponent, txCtx.getParentTx(), txCtx.getRootTx());
					depMgr.getTxs().remove(entry.getKey());
				}
			}
		}
		
		return true;
	}

	private Set<Dependence> getDepsBelongToSameRootTx(String rootTxID, Set<Dependence> allDeps){
		Set<Dependence> result = new HashSet<Dependence>();
		for (Dependence dep : allDeps) {
			if(dep.getRootTx().equals(rootTxID)){
				result.add(dep);
			}
		}
		return result;
	}
	
	@Override
	public boolean isBlockRequiredForFree( Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD, DynamicDepManager depMgr) {
		if( !isUpdateReqRCVD ){
			return false;
		}
		
		String rootTx = txContext.getParentTx();
		if ((algorithmOldVersionRootTxs != null)
				&& algorithmOldVersionRootTxs.contains(rootTx) ){
			return false;
		} else{
			return true;
		}
	}

	@Override
	public boolean updateIsDone(String hostComp, DynamicDepManager depMgr) {
		// clear local status maintained for update
		isSetupDone.clear();

		// notify parent components that remote dynamic update is done
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		
		assert scope != null;
		parentComps = scope.getParentComponents(hostComp);
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for (String comp : parentComps) {
			LOGGER.fine("Sending NOTIFY_REMOTE_UPDATE_DONE to " + comp);
			String payload = TranquillityPayloadCreator
					.createRemoteUpdateIsDonePayload(hostComp, comp,
							DepOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.asynPost(hostComp, comp, CommProtocol.TRANQUILLITY,
					MsgType.DEPENDENCE_MSG, payload);
		}
		
		// clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
//		depMgr.setScope(null);
		
		// add for experiment
		depNotifyService.asynPost(hostComp, "Coordination", CommProtocol.TRANQUILLITY, 
				MsgType.EXPERIMENT_MSG, UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP));
		
		return true;
	}

	@Override
	public void initiate(String identifier, DynamicDepManager depMgr) {
		
	}


	@Override
	public boolean initLocalSubTx(TransactionContext txContext, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		String hostComp = txContext.getHostComponent();
		String fakeSubTx = txContext.getCurrentTx();
		String parentTx = txContext.getParentTx();
		String parentComp = txContext.getParentComponent();
		
		Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
		Set<Dependence> rtOutDeps = depMgr.getRuntimeDeps();
		
		Object ondemandMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandMonitor) {
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)){
				Dependence lfe = new Dependence(FUTURE_DEP, parentTx, hostComp, hostComp, null, null);
				if(!rtInDeps.contains(lfe)){
					rtInDeps.add(lfe);
				}
				if (!rtOutDeps.contains(lfe)) {
					rtOutDeps.add(lfe);
				}
				
				Dependence lpe = new Dependence(PAST_DEP, parentTx, hostComp, hostComp, null, null);
				if(!rtInDeps.contains(lpe)){
					rtInDeps.add(lpe);
				}
				if (!rtOutDeps.contains(lpe)) {
					rtOutDeps.add(lpe);
				}
				
				// ACK_SUBTX_INIT
				String payload = ConsistencyPayloadCreator.createPayload(hostComp, parentComp, parentTx, DepOperationType.ACK_SUBTX_INIT, parentTx, fakeSubTx);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComp, parentComp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
				
			}
		}
		return true;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr,
			String proxyRootTxId) {
		String parentTx = invocationCtx.getParentTx();
		
		String subTx = invocationCtx.getSubTx();
		String subComp = invocationCtx.getSubComp();
		String rootTx = invocationCtx.getRootTx();
		if(subTxStatus.equals(TxEventType.TransactionStart)){

			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				Map<String, TransactionContext> allTxs = depMgr.getTxs();
				TransactionContext txCtx;
				txCtx = allTxs.get(parentTx);
				assert (txCtx != null);
				Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
				Map<String, TxEventType> subTxStatuses = txCtx
						.getSubTxStatuses();
				subTxHostComps.put(subTx, subComp);
				subTxStatuses.put(subTx, TxEventType.TransactionStart);

			}
			return true;
		}
//			return doAckSubTxInit(subComp, curComp, rootTx, parentTx, subTx);
		else if(subTxStatus.equals(TxEventType.TransactionEnd)){
			String curComp = invocationCtx.getParentComp();
			return doNotifySubTxEnd(subComp, curComp, rootTx, parentTx, subTx, depMgr, compLifeCycleMgr);
		}
		else{
			LOGGER.warning("unexpected sub transaction status: " + subTxStatus + " for rootTx " + rootTx);
			return false;
		}
	}

}
