package cn.edu.nju.moon.conup.core.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.comm.api.peer.services.impl.DepNotifyServiceImpl;
import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
//import cn.edu.nju.moon.conup.spi.utils.Printer;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.Printer;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;

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
	
	public static Map<String, Boolean> isSetupDone = new ConcurrentHashMap<String, Boolean>();
	
//	private DynamicDepManager depMgr = null;
	
//	private CompLifeCycleManager compLifeCycleMgr = null;
	
//	private TxDepRegistry txDepRegistry = null;
	
	private Logger LOGGER = Logger.getLogger(VersionConsistencyImpl.class.getName());
	
	@Override
	public void manageDependence(TransactionContext txContext, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		LOGGER.fine("VersionConsistencyImpl.manageDependence(...)");
		CompStatus compStatus = compLifeCycleMgr.getCompStatus();
		TxDepRegistry txDepRegsitry = NodeManager
				.getInstance().getTxDepMonitor(compLifeCycleMgr.getCompObject().getIdentifier()).getTxDepRegistry();
		
		assert compLifeCycleMgr != null;
		assert depMgr != null;
		assert compStatus != null;
		
//		txContext.setRootTx(txContext.getProxyRootTxId(depMgr.getScope()));
//		if(txContext.getEventType().equals(TxEventType.TransactionStart)){
//			LOGGER.fine("TxEventType.TransactionStart, rootTx:" + txContext.getRootTx() + ",currentTx:" + txContext.getCurrentTx());
//		}
		
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, depMgr, compLifeCycleMgr, txDepRegsitry);
			break;
		case VALID:
			doValid(txContext, depMgr, txDepRegsitry);
			break;
		case ONDEMAND:
			doOndemand(txContext, compLifeCycleMgr, depMgr, txDepRegsitry);
			break;
		case UPDATING:
			doValid(txContext, depMgr, txDepRegsitry);
			break;
		case FREE:
			doValid(txContext, depMgr, txDepRegsitry);
			break;
		default:
			LOGGER.fine("----------compStatus-------->" + compStatus);
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
		
		Printer printer = new Printer();
		
		switch(operationType){
		case NOTIFY_FUTURE_CREATE:
//			printer.printDeps(depMgr.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_FUTURE_CREATE:");
			
			manageDepResult = doNotifyFutureCreate(srcComp, targetComp, rootTx, depMgr);
			
//			printer.printDeps(depMgr.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "----Out----" + "after process NOTIFY_FUTURE_CREATE:");
			break;
		case NOTIFY_FUTURE_REMOVE:
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_FUTURE_REMOVE:");
			
			manageDepResult = doNotifyFutureRemove(srcComp, targetComp, rootTx, depMgr);
			
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "----Out----" + "after process NOTIFY_FUTURE_REMOVE:");
			break;
		case NOTIFY_START_REMOTE_SUB_TX:
			LOGGER.warning("deprecated notification NOTIFY_START_REMOTE_SUB_TX");
//			printer.printDeps(depMgr.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_START_REMOTE_SUB_TX:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_START_REMOTE_SUB_TX:");
//			doNotifyStartRemoteSubTx(srcComp, targetComp, rootTx);
//			printer.printDeps(depMgr.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_START_REMOTE_SUB_TX:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "----Out----" + "after process NOTIFY_START_REMOTE_SUB_TX:");
			break;
		case ACK_SUBTX_INIT:
//			LOGGER.info("before process ACK_SUBTX_INIT:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "In" + ", before process ACK_SUBTX_INIT");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "Out" + ", before process ACK_SUBTX_INIT");
			String parentTxID = params.get("parentTx");
			String subTxID = params.get("subTx");
			manageDepResult = doAckSubTxInit(srcComp, targetComp, rootTx, parentTxID, subTxID, compLifeCycleMgr, depMgr);
//			
//			LOGGER.info("after process ACK_SUBTX_INIT:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "In" + ", after process ACK_SUBTX_INIT");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "Out" + ", after process ACK_SUBTX_INIT");
			break;
		case NOTIFY_PAST_CREATE:
//			LOGGER.info("before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "In" + ",before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "Out" + ",before process NOTIFY_PAST_CREATE:");
			
			manageDepResult = doNotifyPastCreate(srcComp, targetComp, rootTx, depMgr);
			
//			LOGGER.info("after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeInDeps(), "In" + ",after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(LOGGER, depMgr.getRuntimeDeps(), "Out" + ",after process NOTIFY_PAST_CREATE:");
			break;
		case NOTIFY_PAST_REMOVE:
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In" + ",before process NOTIFY_PAST_REMOVE:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out" + ",before process NOTIFY_PAST_REMOVE:");
			
			manageDepResult = doNotifyPastRemove(srcComp, targetComp, rootTx, depMgr);
			
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In" + ",after process NOTIFY_PAST_REMOVE:");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out" + ",after process NOTIFY_PAST_REMOVE:");
//			printer.printTxs(depMgr.getTxs());
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			manageDepResult = doNotifyRemoteUpdateDone(srcComp, targetComp, depMgr);
			break;
		}
		return manageDepResult;
		
	}
	
	private void doNormal(TransactionContext txCtx, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr, TxDepRegistry txDepRegistry){
		
		if (txCtx.getEventType().equals(TxEventType.TransactionEnd)) {
			String hostComp;
			String rootTx;

			hostComp = txCtx.getHostComponent();
//			rootTx = txCtx.getRootTx();
			
			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				
				// conup refactor 20140227
				rootTx = txCtx.getProxyRootTxId(depMgr.getScope());
				if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL)){
//					Printer printer = new Printer();
//					LOGGER.fine("TxS before notified TransactionEnd:");
//					printer.printTxs(LOGGER, depMgr.getTxs());
					LOGGER.fine("depMgr.getTxs().size():" + depMgr.getTxs().size());
					depMgr.getTxs().remove(txCtx.getCurrentTx());
//					txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
					depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, rootTx);
					LOGGER.fine("removed tx from TxRegistry and TxDepMonitor, local tx: " + txCtx.getCurrentTx() + ", rootTx: " + rootTx);
					
//					LOGGER.fine("TxS after notified TransactionEnd:");
//					printer.printTxs(LOGGER, depMgr.getTxs());
					
					return;
				} else{
					try {
						if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
							LOGGER.fine("----------------ondemandSyncMonitor.wait();consistency algorithm------------");
							ondemandSyncMonitor.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			doValid(txCtx, depMgr, txDepRegistry);
			
			//remove local root tx
////			depMgr.getTxs().remove(rootTx);
//			depMgr.getTxs().remove(txCtx.getCurrentTx());
//			txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
//			LOGGER.info("removed tx from TxRegistry and TxDepMonitor, local tx: " + txCtx.getCurrentTx() + ", rootTx: " + rootTx);
			
//			Scope scope = depMgr.getScope();
//			Set<String> targetRef = new HashSet<String>();
//			if(scope != null){
//				targetRef.addAll(scope.getSubComponents(hostComp));
//			} else{
//				targetRef.addAll(depMgr.getStaticDeps());
//			}

//			for (String subComp : targetRef) {
//				String payload = ConsistencyPayloadCreator.createNormalRootTxEndPayload(
//						hostComp, subComp, rootTx,
//						OperationType.NORMAL_ROOT_TX_END);
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, subComp,
//						CommProtocol.CONSISTENCY,
//						MsgType.DEPENDENCE_MSG, payload);
//			}
		}
	}
	
	/**
	 * during notify, the component status is valid, do the following action
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doValid(TransactionContext txContext, DynamicDepManager depMgr, TxDepRegistry txDepRegistry) {
		TxEventType txEventType = txContext.getEventType();
//		String rootTx = txContext.getRootTx();
		Scope scope = depMgr.getScope();
		assert txContext != null;
		String rootTx = txContext.getProxyRootTxId(scope);
		if(scope != null && !scope.isSpecifiedScope()){
			assert rootTx.equals(txContext.getRootTx());
		}
		
		if(rootTx == null){
			System.out.println("InvocationSequence: " + txContext.getInvocationSequence());
			System.out.println("HostComp: " + txContext.getHostComponent() + " real root: " + txContext.getRootTx());
			assert rootTx != null;
		}
		
		String currentTx = txContext.getCurrentTx();
		String hostComponent = txContext.getHostComponent();
		
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		assert txDepRegistry.getLocalDep(currentTx) != null;
		Set<String> futureComponents = txDepRegistry.getLocalDep(currentTx).getFutureComponents();
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
			
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
			
			if (rootTx.equals(currentTx)) {
				/*
				 * current transaction is root
				 */
				isSetupDone.put(rootTx, false);
			}else{
				/* ACK_SUBTX_INIT
				 * current transaction is not root
				 * notify parent that a new sub-tx start
				 */
				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), rootTx, DepOperationType.ACK_SUBTX_INIT, txContext.getParentTx(), txContext.getCurrentTx());
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
			}
			
		} else if (txEventType.equals(TxEventType.DependencesChanged)) {
			
			Set<Dependence> futureDepsInODR = outDepRegistry.getDependencesViaType(FUTURE_DEP);
			Set<Dependence> futureDepSameRoot = new ConcurrentSkipListSet<Dependence>();
			for(Dependence dep : futureDepsInODR){
				if(dep.getRootTx().equals(rootTx)){
					futureDepSameRoot.add(dep);
				}
			}
			boolean hasFutureInDep = false;
			Set<Dependence> futureDepsInIDR = inDepRegistry.getDependencesViaType(FUTURE_DEP);
			for(Dependence dep : futureDepsInIDR){
				if(dep.getRootTx().equals(rootTx)){
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
					//notify sub-comp future removed(here must be coincidence with algorithm)
					String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, DepOperationType.NOTIFY_FUTURE_REMOVE);
					DepNotifyService depNotifyService = new DepNotifyServiceImpl();
					depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.CONSISTENCY,  MsgType.DEPENDENCE_MSG, payload);
				}
			}
			
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			/**
			 * if currentTx is not root, need to notify parent sub_tx_end
			 * else if currentTx is root, start cleanup
			 */
			if(!currentTx.equals(rootTx)){
//				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), rootTx, OperationType.NOTIFY_SUBTX_END, txContext.getParentTx(), txContext.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), CommProtocol.CONSISTENCY,  MsgType.DEPENDENCE_MSG, payload);
			}else{
				/** current tx is root tx */
				//start cleanup
				//remove lfe,lpe from OES,IES
				inDepRegistry.removeDependence(FUTURE_DEP, rootTx, hostComponent, hostComponent);
				inDepRegistry.removeDependence(PAST_DEP, rootTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(FUTURE_DEP, rootTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(PAST_DEP, rootTx, hostComponent, hostComponent);
				
				removeAllEdges(hostComponent, rootTx, depMgr);
				
				LOGGER.fine("rootTx END " + hostComponent + " " + rootTx);

				// remove root tx id from isSetupDone
				isSetupDone.remove(currentTx);
			}
			
			//remove tx ctx from TxRegistry
			depMgr.getTxs().remove(currentTx);
//			System.out.println("currentTx:" + currentTx);
//			Printer printer = new Printer();
//			printer.printDeps(depMgr.getRuntimeInDeps(), "In" + ", current tx is ended");
//			printer.printDeps(depMgr.getRuntimeDeps(), "Out" + ", current tx is ended");
//			printer.printTxs(depMgr.getTxs());
			
		} else {
			// up receiving FirstRequestService
			// if current tx is root tx, we need to start set up
			Set<String> targetRef = new HashSet<String>();
			if(scope != null){
				targetRef.addAll(scope.getSubComponents(hostComponent));
			} else{
				targetRef.addAll(depMgr.getStaticDeps());
			}
			assert rootTx != null;
			if(rootTx.equals(currentTx) && (isSetupDone.get(rootTx) == null || !isSetupDone.get(rootTx))){
//				Set<String> fDeps = txContext.getFutureComponents();
				Set<String> fDeps = txDepRegistry.getLocalDep(currentTx).getFutureComponents();
				Iterator<String> depIterator = fDeps.iterator();
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				while(depIterator.hasNext()){
					String targetComp = depIterator.next();
					if(!targetRef.contains(targetComp))
						continue;
					Dependence dep = new Dependence(FUTURE_DEP, currentTx, hostComponent, targetComp, null, null);
					outDepRegistry.addDependence(dep);
					String payload = ConsistencyPayloadCreator.createPayload(hostComponent, targetComp, currentTx, DepOperationType.NOTIFY_FUTURE_CREATE);
					depNotifyService.synPost(hostComponent, targetComp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
				}
				isSetupDone.put(rootTx, true);
			}
		}
	}

	/**
	 * current component status is ondemand, suspend current execution until 
	 * status becomes to valid
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doOndemand(TransactionContext txContext, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr, TxDepRegistry txDepRegistry) {
		// sleep until current status become valid
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
					LOGGER.fine("----------------ondemandSyncMonitor.wait();consistency algorithm------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doValid(txContext, depMgr, txDepRegistry);
		
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
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		Dependence dep = new Dependence(FUTURE_DEP, rootTx, srcComp, targetComp, null, null);
		inDepRegistry.addDependence(dep);
		
		Scope scope = depMgr.getScope();
		Set<String> targetRef = new HashSet<String>();
		if(scope != null){
			targetRef.addAll(scope.getSubComponents(targetComp));
		} else{
			targetRef.addAll(depMgr.getStaticDeps());
		}
		
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//		Set<String> staticDeps = dynamicDepMgr.getStaticDeps();
		for(String str : targetRef){
			Dependence futureDep = new Dependence(FUTURE_DEP, rootTx, targetComp, str, null, null);
			outDepRegistry.addDependence(futureDep);
			String payload = ConsistencyPayloadCreator.createPayload(targetComp, str, rootTx, DepOperationType.NOTIFY_FUTURE_CREATE);
			depNotifyService.synPost(targetComp, str, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
		}
		
//		LOGGER.fine("accept future create msg,print inDepRegistry:");
//		for(Dependence temp : inDepRegistry.getDependences()){
//			LOGGER.fine(temp);
//		}
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
		
		// dependency changed, check for freeness
		depMgr.dependenceChanged(targetComp);
		
		return removeAllEdges(targetComp, rootTx, depMgr);
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
//			if(tc.getRootTx().equals(rootTx) && !tc.getEventType().equals(TxEventType.TransactionEnd) && !tc.isFakeTx()){
			if(tc.getProxyRootTxId(depMgr.getScope()).equals(rootTx) && !tc.getEventType().equals(TxEventType.TransactionEnd) && !tc.isFakeTx()){
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
		
		String hostComp = depMgr.getCompObject().getIdentifier();
		TxDepRegistry txDepRegistry = NodeManager.getInstance().getTxDepMonitor(hostComp).getTxDepRegistry();
		
		removeFutureEdges(targetComp, rootTx, depMgr, txDepRegistry);
		
		return true;
	}
	
	/**
	 * be notified sub tx ends.
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifySubTxEnd(String srcComp, String targetComp,
			String rootTx, String parentTx, String subTx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " subTx:" + subTx + " rootTx:" + rootTx);
		
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			//maintain tx
			Map<String, TransactionContext> allTxs = depMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTx);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTx, srcComp);
			subTxStatuses.put(subTx, TxEventType.TransactionEnd);
			
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			Scope scope = depMgr.getScope();
			if(scope != null && !scope.getSubComponents(targetComp).contains(srcComp)){
				return true;
			}
			
			DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
			Dependence dependence = new Dependence(PAST_DEP, rootTx, targetComp, srcComp, null, null);
			outDepRegistry.addDependence(dependence);
			
			// notify past dep create
			DepNotifyService depNotifyService = new DepNotifyServiceImpl();
			String payload = ConsistencyPayloadCreator.createPayload(targetComp, srcComp, rootTx, DepOperationType.NOTIFY_PAST_CREATE);
			depNotifyService.synPost(targetComp, srcComp, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
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
	private boolean doAckSubTxInit(String srcComp, String targetComp,
			String rootTx, String parentTxID, String subTxID,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " subTx:" + subTxID + " rootTx:" + rootTx);
		
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
			
//			if(compLifeCycleMgr.isNormal())
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			return removeFutureEdges(targetComp, rootTx, parentTxID, subTxID, depMgr);
		}
		
//		Map<String, TransactionContext> allTxs = dynamicDepMgr.getTxs();
//		TransactionContext txCtx;
//		txCtx = allTxs.get(parentTxID);
//		assert(txCtx!=null);
//		Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
//		Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
//		subTxHostComps.put(subTxID, srcComp);
//		subTxStatuses.put(subTxID, TxEventType.TransactionStart);
//		
//		if(dynamicDepMgr.getCompStatus().equals(CompStatus.NORMAL))
//			return true;
//		
//		return removeFutureEdges(dynamicDepMgr, targetComp, rootTx, parentTxID, subTxID);
	}
	
	/**
	 * receive NotifyFutureRemove
	 * try to remove src--->target future dep
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyFutureRemove(String srcComp, String targetComp,
			String rootTx, DynamicDepManager depMgr) {
		LOGGER.fine(srcComp + "-->" + targetComp + " rootTx: " + rootTx);
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		
		inDepRegistry.removeDependence(FUTURE_DEP, rootTx, srcComp, targetComp);
		
		String hostComp = depMgr.getCompObject().getIdentifier();
		TxDepRegistry txDepRegistry = NodeManager.getInstance().getTxDepMonitor(hostComp).getTxDepRegistry();
		boolean result = removeFutureEdges(targetComp, rootTx, depMgr, txDepRegistry);
		
		return result; 
	}
	
	@Deprecated
	private boolean doNotifyStartRemoteSubTx(String srcComp, String targetComp,
			String rootTx, DynamicDepManager depMgr) {
		Set<Dependence>	rtInDeps = depMgr.getRuntimeInDeps();
		Set<Dependence> rtOutDeps = depMgr.getRuntimeDeps();
		
		Dependence lfe = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
				rootTx, targetComp, targetComp, null, null);
		Dependence lpe = new Dependence(VersionConsistencyImpl.PAST_DEP, 
				rootTx, targetComp, targetComp, null, null);
		
		rtInDeps.add(lfe);
		rtInDeps.add(lpe);
		rtOutDeps.add(lfe);
		rtOutDeps.add(lpe);
		
		((DynamicDepManagerImpl)depMgr).getInDepRegistry().removeDependence(FUTURE_DEP, rootTx, srcComp, targetComp);
//		boolean result = removeFutureEdges(dynamicDepMgr, targetComp, rootTx);
		
		return true; 
	}
	
	private boolean doNotifyRemoteUpdateDone(String srComp, String hostComp, DynamicDepManager depMgr){
		LOGGER.fine(hostComp + " received notifyRemoteUpdateDone from " + srComp);
//		NodeManager nodeManager = NodeManager.getInstance();
//		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		
		//notify parent components that remote dynamic update is done
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		if(scope != null){
			parentComps = scope.getParentComponents(hostComp);
		}
		else{
			parentComps = depMgr.getCompObject().getStaticInDeps();
		}
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for(String comp : parentComps){
			String payload = ConsistencyPayloadCreator.createRemoteUpdateIsDonePayload(hostComp, comp, DepOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.asynPost(hostComp, comp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
		}
		
		//clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
		depMgr.setScope(null);
		
//		compLifeCycleMgr.remoteDynamicUpdateIsDone();
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
	private boolean removeFutureEdges(String currentComp, String rootTx,
			String currentTxID, String subTxID, DynamicDepManager depMgr) {
		DependenceRegistry outDepRegistry = ((DynamicDepManagerImpl)depMgr).getOutDepRegistry();
		DependenceRegistry inDepRegistry = ((DynamicDepManagerImpl)depMgr).getInDepRegistry();
		Set<Dependence> outFutureDeps = outDepRegistry.getDependencesViaType(FUTURE_DEP);
		Set<Dependence> outFutureOneRoot = new HashSet<Dependence>();
		
		Iterator<Dependence> iterator = outFutureDeps.iterator();
		while(iterator.hasNext()){
			Dependence dep = iterator.next(); 
			if(dep.getRootTx().equals(rootTx) && !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				outFutureOneRoot.add(dep);
//				break;
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
//			TransactionContext txContext = depMgr.getTxs().get(currentTxID);
			TxDepMonitor txDepMonitor = NodeManager.getInstance().getTxDepMonitor(currentComp);
			
			for(Dependence dep : outFutureOneRoot){
				boolean isLastUse = txDepMonitor.isLastUse(currentTxID, dep.getTargetCompObjIdentifer(), currentComp);
				if(isLastUse){
					outDepRegistry.removeDependence(dep);
					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), DepOperationType.NOTIFY_FUTURE_REMOVE);
					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
//					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), OperationType.NOTIFY_START_REMOTE_SUB_TX);
//					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				}else{
				}
			}
			
		}

		return true;
	}
	
	/**
	 * try to remove future dep when receive NOTIFY_FUTURE_REMOVE, NOTIFY_PAST_CREATE
	 * according the condition to decide whether need to remove the future dep
	 * @param dynamicDepMgr
	 * @param currentComp
	 * @param rootTx
	 * @return
	 */
	private boolean removeFutureEdges(String currentComp, String rootTx, DynamicDepManager depMgr, TxDepRegistry txDepRegistry){
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
		
		boolean willNotUseFlag = true;
		
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		Iterator<Dependence> iter = outFutureOneRoot.iterator();
		while(iter.hasNext()){
			Dependence dep = iter.next();
			if(!inFutureFlag ){
				Map<String, TransactionContext> localTxs = depMgr.getTxs();
				Iterator<Entry<String, TransactionContext>> localTxsIterator = localTxs.entrySet().iterator();
				while(localTxsIterator.hasNext()){
					TransactionContext T = localTxsIterator.next().getValue();
					
					if(T.isFakeTx())
						continue;
					
//					Set<String> fDeps = T.getFutureComponents();
					Set<String> fDeps = txDepRegistry.getLocalDep(T.getCurrentTx()).getFutureComponents();
					for(String fdep : fDeps){
//						if(fdep.equals(dep.getTargetCompObjIdentifer()) && T.getRootTx().equals(rootTx)){
						if(fdep.equals(dep.getTargetCompObjIdentifer()) && T.getProxyRootTxId(depMgr.getScope()).equals(rootTx)){
							willNotUseFlag = false;
							break;
						}
					}
					if(!willNotUseFlag){
						break;
					}
				}
				if(willNotUseFlag){
					Scope scope = depMgr.getScope();
					try{
						if(scope!= null && !scope.getSubComponents(dep.getSrcCompObjIdentifier()).contains(dep.getTargetCompObjIdentifer())){
							continue;
						}
					} catch (Exception e) {
						LOGGER.fine("scope=" + scope);
						LOGGER.fine("scope.getSubComponents()=" + scope.getSubComponents(dep.getSrcCompObjIdentifier()));
						e.printStackTrace();
					}
					
					outDepRegistry.removeDependence(dep);
					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), DepOperationType.NOTIFY_FUTURE_REMOVE);
					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				}
			}
		}

		return true;
	}
	
	/**
	 * cleaning up
	 * @param hostComponent
	 * @param rootTx
	 * @param depMgr
	 * @return
	 */
	private boolean removeAllEdges(String hostComponent, String rootTx, DynamicDepManager depMgr){
		Set<Dependence> rtOutDeps;
		
		//remove deps
		rtOutDeps = depMgr.getRuntimeDeps();
		for(Dependence dep : rtOutDeps){
			if(dep.getRootTx().equals(rootTx) && dep.getType().equals(FUTURE_DEP)
				&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, DepOperationType.NOTIFY_FUTURE_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.CONSISTENCY,  MsgType.DEPENDENCE_MSG, payload);
			} else if(dep.getRootTx().equals(rootTx) && dep.getType().equals(PAST_DEP)
					&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, DepOperationType.NOTIFY_PAST_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.CONSISTENCY,  MsgType.DEPENDENCE_MSG, payload);
			}
			
			if(dep.getRootTx().equals(rootTx)){
				rtOutDeps.remove(dep);
			}
		}
		
		//remove tx from TxDepMonitor
		Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
		boolean isPastDepExist = false;
		for(Dependence dep : rtInDeps){
			if(dep.getRootTx().equals(rootTx) ){
				isPastDepExist = true;
				break;
			}
		}
		
//		Printer printer = new Printer();
//		printer.printDeps(LOGGER, rtInDeps, "rtInDeps removeAllEdges");
//		
//		if( !isPastDepExist ){
//			if(depMgr.getTxDepMonitor() != null){
//				depMgr.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
//			} else if(!depMgr.getTxs().isEmpty()){
//				for(Entry<String,TransactionContext> entry : depMgr.getTxs().entrySet()){
//					TransactionContext txCtx = entry.getValue();
//					txCtx.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
//					break;
//				}
//			} else{
//				LOGGER.warning("failed to remove rootTx " + rootTx + ", due to fail to get TxDepMonitor");
//			}
//		}
		
		//remove tx
		for(Entry<String,TransactionContext> entry : depMgr.getTxs().entrySet()){
			TransactionContext txCtx = entry.getValue();
//			if(txCtx.getRootTx().equals(rootTx)){
			if(txCtx.getProxyRootTxId(depMgr.getScope()).equals(rootTx)){
				//if there are any in deps marked with rootTx, the rootTx id should not be removed
//				Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
//				boolean isPastDepExist = false;
//				for(Dependence dep : rtInDeps){
//					if(dep.getRootTx().equals(rootTx) ){
//						isPastDepExist = true;
//						break;
//					}
//				}
				// TODO HAVE A BUG!!
				if( !isPastDepExist ){
//					txCtx.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
					//remove all the tx associated with the given rootTx
					Iterator<Entry<String, TransactionContext>> inIte;
					inIte = depMgr.getTxs().entrySet().iterator();
					while(inIte.hasNext()){
						TransactionContext inCtx = inIte.next().getValue();
//						if(inCtx.getRootTx().equals(rootTx)){
						if(inCtx.getProxyRootTxId(depMgr.getScope()).equals(rootTx)){
							inIte.remove();
						}
					}
				}
			}
		}
		
//		assert depMgr.getTxDepMonitor() != null;
//		depMgr.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
		
		assert depMgr.getTxLifecycleMgr() != null;
		depMgr.getTxLifecycleMgr().rootTxEnd(hostComponent, rootTx);
		
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
	public String getAlgorithmType() {
		return VersionConsistencyImpl.ALGORITHM_TYPE;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allInDeps) {
		Set<String> oldRootTx = new HashSet<String>();
		for(Dependence dep : allInDeps){
//			if(dep.getType().equals(PAST_DEP) && !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer()))
			if(dep.getType().equals(PAST_DEP))
				oldRootTx.add(dep.getRootTx());
		}
		
		// test
		String inDepsStr = "";
		for(Dependence dep : allInDeps){
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("inDepsStr:" + inDepsStr);
		String outDepsStr = "";
		
		for(String dep : oldRootTx){
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.fine("oldRootTx:" + outDepsStr);
		
		LOGGER.fine("in consisitency algorithm(allInDeps):" + allInDeps);
//		LOGGER.fine("in consisitency algorithm(oldrootTxs):" + oldRootTx);
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
				LOGGER.fine(deps.toString());
//				Printer printer = new Printer();
//				printer.printDeps(LOGGER, deps, "ALL:");
				freeFlag = false;
				break;
			}
			
		}
		return freeFlag;
	}

	@Override
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD, DynamicDepManager depMgr) {
		if( !isUpdateReqRCVD ){
			return false;
		}
		
//		String rootTx = txContext.getRootTx();
		String rootTx = txContext.getProxyRootTxId(depMgr.getScope());
		if ((algorithmOldVersionRootTxs != null)
				&& algorithmOldVersionRootTxs.contains(rootTx) ){
			LOGGER.fine("real rootTxId:" + txContext.getRootTx() + " proxyRootTxId:" + rootTx + " not blocked, \n algorithm:" + algorithmOldVersionRootTxs);
			return false;
		} else{
			LOGGER.fine("real rootTxId:" + txContext.getRootTx() + " proxyRootTxId:" + rootTx + " is blocked, \n algorithm:" + algorithmOldVersionRootTxs);
			return true;
		}
	}

	@Override
	public boolean updateIsDone(String hostComp, DynamicDepManager depMgr) {
		//clear local status maintained for update
		isSetupDone.clear();

		//notify parent components that remote dynamic update is done
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		if (scope != null) {
			parentComps = scope.getParentComponents(hostComp);
		} else {
			parentComps = depMgr.getCompObject().getStaticInDeps();
		}
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for(String comp : parentComps){
			LOGGER.fine("Sending NOTIFY_REMOTE_UPDATE_DONE to " + comp);
			String payload = ConsistencyPayloadCreator.createRemoteUpdateIsDonePayload(hostComp, comp, DepOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.asynPost(hostComp, comp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
		}
		
		//clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
		depMgr.setScope(null);
		
		// add for experiment
//		depNotifyService.asynPost(hostComp, "Coordination", CommProtocol.CONSISTENCY, 
//				MsgType.EXPERIMENT_MSG, UpdateContextPayloadCreator.createPayload(
//				UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP));
		LOGGER.fine("update is done, print Txs: txs.size()" + depMgr.getTxs().size() + "\n" + depMgr.getTxs());
		
		return true;
	}

	@Override
	public void initiate(String identifier, DynamicDepManager depMgr) {
		
	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		String hostComp = txContext.getHostComponent();
		String fakeSubTx = txContext.getCurrentTx();
		String rootTx = txContext.getRootTx();
		String parentTx = txContext.getParentTx();
		String parentComp = txContext.getParentComponent();
		Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
		Set<Dependence> rtOutDeps = depMgr.getRuntimeDeps();
		
		Object ondemandMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandMonitor) {
			rootTx = txContext.getProxyRootTxId(depMgr.getScope());
			if( compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND) ){
				Dependence lfe = new Dependence(FUTURE_DEP, rootTx, hostComp, hostComp, null, null);
				LOGGER.fine(lfe.toString());
				if(!rtInDeps.contains(lfe)){
					rtInDeps.add(lfe);
				}
				if (!rtOutDeps.contains(lfe)) {
					rtOutDeps.add(lfe);
				}
				
				Dependence lpe = new Dependence(PAST_DEP, rootTx, hostComp, hostComp, null, null);
				LOGGER.fine(lpe.toString());
				if(!rtInDeps.contains(lpe)){
					rtInDeps.add(lpe);
				}
				if (!rtOutDeps.contains(lpe)) {
					rtOutDeps.add(lpe);
				}
				
				// ACK_SUBTX_INIT
				String payload = ConsistencyPayloadCreator.createPayload(hostComp, parentComp, rootTx, DepOperationType.ACK_SUBTX_INIT, parentTx, fakeSubTx);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComp, parentComp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
				
			}
		}
		
		return true;
	}

	//	@Override
	//	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs) {
	//		Set<String> result = new HashSet<String>();
	//		
	//		Iterator<Entry<String, String>> iterator;
	//		iterator = oldRootTxs.entrySet().iterator();
	//		while(iterator.hasNext()){
	//			result.add(iterator.next().getValue());
	//		}
	//		
	//		return result;
	//	}
		
	//	@Override
	//	public String getAlgorithmRoot(String parentTx, String rootTx) {
	//		return rootTx;
	//	}
	
	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr,
			String proxyRootTxId) {
		String parentTx = invocationCtx.getParentTx();
		String subTx = invocationCtx.getSubTx();
		String subComp = invocationCtx.getSubComp();
		String rootTx = invocationCtx.getRootTx();
		String curComp = invocationCtx.getParentComp();
		if (subTxStatus.equals(TxEventType.TransactionStart)) {

			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject()
					.getOndemandSyncMonitor();
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
		} else if (subTxStatus.equals(TxEventType.TransactionEnd))
			return doNotifySubTxEnd(subComp, curComp, proxyRootTxId, parentTx, subTx,
					compLifeCycleMgr, depMgr);
		else {
			LOGGER.warning("unexpected sub transaction status: " + subTxStatus
					+ " for rootTx " + rootTx);
			return false;
		}
	}

//	@Override
//	public void setDynamicDepMgr(DynamicDepManager depMgr) {
//		this.depMgr = depMgr;		
//	}
//
//	@Override
//	public void setTxDepRegistry(TxDepRegistry txDepRegistry){
//		this.txDepRegistry = txDepRegistry;
//	}

}
