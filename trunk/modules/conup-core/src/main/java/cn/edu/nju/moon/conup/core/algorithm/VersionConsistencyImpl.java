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
import cn.edu.nju.moon.conup.core.TransactionRegistry;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.core.utils.ConsistencyOndemandPayloadResolver;
import cn.edu.nju.moon.conup.core.utils.ConsistencyOperationType;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayload;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;

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
	
	/**
	 * When dynamic update is done, the CompStatus of a component should be changed to NORMAL.
	 * Before that, however, there are many dynamic dependences which should be removed.
	 * Map<String, Map<String, Boolean>> takes the hostComponent as the key, 
	 * and the inner Map<String, Boolean> takes the parent-components as the key.
	 */
//	public static Map<String, Map<String, Boolean>> isCleanUpForUpdateSent = new ConcurrentHashMap<String, Map<String, Boolean>>();
//	public static Map<String, Integer> isCleanUpForUpdateDone = new ConcurrentHashMap<String, Integer>();
	
	private Logger LOGGER = Logger.getLogger(VersionConsistencyImpl.class.getName());
	
	@Override
	public void manageDependence(TransactionContext txContext) {
		LOGGER.fine("VersionConsistencyImpl.manageDependence(...)");
		String hostComponent = txContext.getHostComponent();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(hostComponent);
		CompStatus compStatus = dynamicDepMgr.getCompStatus();
		
		assert dynamicDepMgr != null;
		assert compStatus != null;
		
//		if(txContext.getEventType().equals(TxEventType.TransactionStart)){
//			LOGGER.fine("TxEventType.TransactionStart, rootTx:" + txContext.getRootTx() + ",currentTx:" + txContext.getCurrentTx());
//		}
		
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, dynamicDepMgr);
			return;
		case VALID:
			doValid(txContext, dynamicDepMgr);
			break;
		case ONDEMAND:
			doOndemand(txContext, dynamicDepMgr);
			break;
		case UPDATING:
//			during updating, there is no notify happen, do not need to maintain deps, should throw an exception
//			doUpdating(txContext, dynamicDepMgr);
			break;
//		case UPDATED:
//			doValid(txContext, dynamicDepMgr);
//			break;
		default:
			LOGGER.fine("----------compStatus-------->" + compStatus);
			LOGGER.fine("default process...");
		}
		
	}
	
	@Override
	public boolean manageDependence(String payload) {
		// print to terminal
		NodeManager nodeMgr = NodeManager.getInstance();
		Printer printer = new Printer();

		boolean manageDepResult = false;
		
		ConsistencyOndemandPayloadResolver plResolver;
		plResolver = new ConsistencyOndemandPayloadResolver(payload);
		String srcComp = plResolver.getParameter(ConsistencyPayload.SRC_COMPONENT);
		String targetComp = plResolver.getParameter(ConsistencyPayload.TARGET_COMPONENT);
		String rootTx = plResolver.getParameter(ConsistencyPayload.ROOT_TX);
		ConsistencyOperationType operationType = plResolver.getOperation();
		
		
		DynamicDepManager ddm = nodeMgr.getDynamicDepManager(targetComp);
		
		assert operationType != null;
		
		switch(operationType){
		case NOTIFY_FUTURE_CREATE:
//			printer.printDeps(ddm.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_FUTURE_CREATE:");
			
			manageDepResult = doNotifyFutureCreate(srcComp, targetComp, rootTx);
			
//			printer.printDeps(ddm.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + "after process NOTIFY_FUTURE_CREATE:");
			break;
		case NOTIFY_FUTURE_REMOVE:
//			printer.printDeps(ddm.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_FUTURE_REMOVE:");
			
			manageDepResult = doNotifyFutureRemove(srcComp, targetComp, rootTx);
			
//			printer.printDeps(ddm.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + "after process NOTIFY_FUTURE_REMOVE:");
			break;
		case NOTIFY_START_REMOTE_SUB_TX:
			printer.printDeps(ddm.getRuntimeInDeps(), "----IN----" + ", before process NOTIFY_START_REMOTE_SUB_TX:");
			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + ", before process NOTIFY_START_REMOTE_SUB_TX:");
			doNotifyStartRemoteSubTx(srcComp, targetComp, rootTx);
			printer.printDeps(ddm.getRuntimeInDeps(), "---IN----" + "after process NOTIFY_START_REMOTE_SUB_TX:");
			printer.printDeps(ddm.getRuntimeDeps(), "----Out----" + "after process NOTIFY_START_REMOTE_SUB_TX:");
			break;
		case ACK_SUBTX_INIT:
			LOGGER.warning("deprecated notification ACK_SUBTX_INIT");
//			LOGGER.info("before process ACK_SUBTX_INIT:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ", before process ACK_SUBTX_INIT");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ", before process ACK_SUBTX_INIT");
//			
//			String parentTxID = plResolver.getParameter(ConsistencyPayload.PARENT_TX);
//			String subTxID = plResolver.getParameter(ConsistencyPayload.SUB_TX);
//			manageDepResult = doAckSubTxInit(srcComp, targetComp, rootTx, parentTxID, subTxID);
//			
//			LOGGER.info("after process ACK_SUBTX_INIT:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ", after process ACK_SUBTX_INIT");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ", after process ACK_SUBTX_INIT");
			break;
		case NOTIFY_SUBTX_END:
			LOGGER.warning("deprecated notification NOTIFY_SUBTX_END");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ", before process NOTIFY_SUBTX_END");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ", before process NOTIFY_SUBTX_END");
//			
//			String subTxEndParentTxID = plResolver.getParameter(ConsistencyPayload.PARENT_TX);
//			String subTxEndSubTxID = plResolver.getParameter(ConsistencyPayload.SUB_TX);
//			manageDepResult = doNotifySubTxEnd(srcComp, targetComp, rootTx, subTxEndParentTxID, subTxEndSubTxID);
//			
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ", after process NOTIFY_SUBTX_END");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ", after process NOTIFY_SUBTX_END");
			break;
		case NOTIFY_PAST_CREATE:
//			LOGGER.info("before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ",before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ",before process NOTIFY_PAST_CREATE:");
			
			manageDepResult = doNotifyPastCreate(srcComp, targetComp, rootTx);
			
//			LOGGER.info("after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ",after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ",after process NOTIFY_PAST_CREATE:");
			break;
		case NOTIFY_PAST_REMOVE:
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ",before process NOTIFY_PAST_REMOVE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ",before process NOTIFY_PAST_REMOVE:");
			
			manageDepResult = doNotifyPastRemove(srcComp, targetComp, rootTx);
			
//			printer.printDeps(ddm.getRuntimeInDeps(), "In" + ",after process NOTIFY_PAST_REMOVE:");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out" + ",after process NOTIFY_PAST_REMOVE:");
			break;
		case NORMAL_ROOT_TX_END:
			LOGGER.warning("deprecated notification: NORMAL_ROOT_TX_END");
//			ConsistencyOndemandPayloadResolver resolver;
//			resolver = new ConsistencyOndemandPayloadResolver(payload);
//			manageDepResult = doNormalRootTxEnd(resolver.getParameter(ConsistencyPayload.SRC_COMPONENT),
//					resolver.getParameter(ConsistencyPayload.TARGET_COMPONENT),
//					resolver.getParameter(ConsistencyPayload.ROOT_TX));
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			manageDepResult = doNotifyRemoteUpdateDone(srcComp, targetComp);
			break;
		}
		return manageDepResult;
	}
	
	private void doNormal(TransactionContext txCtx, DynamicDepManagerImpl depMgr){
		//if current tx is not a root tx
		if(!txCtx.getCurrentTx().equals(txCtx.getRootTx())){
			String hostComp = txCtx.getHostComponent();
			if (txCtx.getEventType().equals(TxEventType.TransactionStart)) {
//				String payload = ConsistencyPayloadCreator.createPayload(hostComp, txCtx.getParentComponent(),
//						txCtx.getRootTx(), ConsistencyOperationType.ACK_SUBTX_INIT, txCtx.getParentTx(), txCtx.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, txCtx.getParentComponent(), CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
				
			} else if (txCtx.getEventType().equals(TxEventType.TransactionEnd)) {
//				String payload = ConsistencyPayloadCreator.createPayload(hostComp, txCtx.getParentComponent(),
//						txCtx.getRootTx(), ConsistencyOperationType.NOTIFY_SUBTX_END, txCtx.getParentTx(), txCtx.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, txCtx.getParentComponent(), CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
			}
		}
		//if a root tx ends, it should recursively notify its sub-components
//		if (txCtx.getEventType().equals(TxEventType.TransactionEnd)
//				&& txCtx.getCurrentTx().equals(txCtx.getRootTx())) {
		if (txCtx.getEventType().equals(TxEventType.TransactionEnd)) {
			String hostComp;
			String rootTx;

			hostComp = txCtx.getHostComponent();
			rootTx = txCtx.getRootTx();
			
			Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				if( depMgr.isNormal()){
//					Printer printer = new Printer();
//					LOGGER.fine("TxS before notified TransactionEnd:");
//					printer.printTxs(LOGGER, depMgr.getTxs());
					
					depMgr.getTxs().remove(txCtx.getCurrentTx());
					txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
					LOGGER.fine("removed tx from TxRegistry and TxDepMonitor, local tx: " + txCtx.getCurrentTx() + ", rootTx: " + rootTx);
					
//					LOGGER.fine("TxS after notified TransactionEnd:");
//					printer.printTxs(LOGGER, depMgr.getTxs());
					
					return;
				} else{
					try {
						if (depMgr.isOndemandSetting()) {
							LOGGER.fine("----------------ondemandSyncMonitor.wait();consistency algorithm------------");
							ondemandSyncMonitor.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			doValid(txCtx, depMgr);
			
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
//						ConsistencyOperationType.NORMAL_ROOT_TX_END);
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, subComp,
//						CommProtocol.CONSISTENCY,
//						MsgType.DEPENDENCE_MSG, payload);
//			}
		}
	}
	
	private boolean doNormalRootTxEnd(String srcComp, String currentComp, String rootTx){
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl depMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(currentComp);
		TransactionContext txCtx;
		Iterator<Entry<String, TransactionContext>>  txIterator = depMgr.getTxs().entrySet().iterator();
		int existSubTxWithRoot = 0 ;
		while(txIterator.hasNext()){
			txCtx = txIterator.next().getValue();
			if(txCtx.getRootTx().equals(rootTx)){
				existSubTxWithRoot ++;
			}
		}
		
		txIterator = depMgr.getTxs().entrySet().iterator();
		while(txIterator.hasNext()){
			txCtx = txIterator.next().getValue();
//			if(txCtx.getRootTx().equals(rootTx) && txCtx.getParentComponent().equals(srcComp)){
			if(txCtx.getRootTx().equals(rootTx)){
//				txIterator.remove();
				existSubTxWithRoot --;
				if(existSubTxWithRoot == 0){
					//remove all the tx associated with the given rootTx
					Iterator<Entry<String, TransactionContext>> inIte;
					inIte = depMgr.getTxs().entrySet().iterator();
					while(inIte.hasNext()){
						TransactionContext inCtx = inIte.next().getValue();
						if(inCtx.getRootTx().equals(rootTx)){
							inIte.remove();
						}
					}
					txCtx.getTxDepMonitor().rootTxEnd(currentComp, rootTx);
				}
			}
		}
		
		Set<String> targetRef;
		targetRef = depMgr.getStaticDeps();
		for(String subComp : targetRef){
			String payload = ConsistencyPayloadCreator.createNormalRootTxEndPayload(currentComp, subComp, rootTx, ConsistencyOperationType.NORMAL_ROOT_TX_END);
			DepNotifyService depNotifyService = new DepNotifyServiceImpl();
			depNotifyService.synPost(currentComp, subComp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
		}
		
		return true;
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
		String hostComponent = txContext.getHostComponent();
		
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		Set<String> futureComponents = txContext.getFutureComponents();
		
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
// log
//			NodeManager nodeMgr = NodeManager.getInstance();
//			Printer printer = new Printer();
//			LOGGER.info("doVALID(TxEventType.TransactionStart):print deps:");
//			DynamicDepManager ddm = nodeMgr.getDynamicDepManager(hostComponent);
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
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
//				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), rootTx, ConsistencyOperationType.ACK_SUBTX_INIT, txContext.getParentTx(), txContext.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
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
					String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, ConsistencyOperationType.NOTIFY_FUTURE_REMOVE);
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
//				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), rootTx, ConsistencyOperationType.NOTIFY_SUBTX_END, txContext.getParentTx(), txContext.getCurrentTx());
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
				
				removeAllEdges(hostComponent, rootTx, dynamicDepMgr);

				// remove root tx id from isSetupDone
				isSetupDone.remove(currentTx);
			}
			
		} else {
			// up receiving FirstRequestService
			// if current tx is root tx, we need to start set up
			Scope scope = dynamicDepMgr.getScope();
			Set<String> targetRef = new HashSet<String>();
			if(scope != null){
				targetRef.addAll(scope.getSubComponents(hostComponent));
			} else{
				targetRef.addAll(dynamicDepMgr.getStaticDeps());
			}
			assert rootTx != null;
			if(rootTx.equals(currentTx) && (isSetupDone.get(rootTx) == null || !isSetupDone.get(rootTx))){
				Set<String> fDeps = txContext.getFutureComponents();
				Iterator<String> depIterator = fDeps.iterator();
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				while(depIterator.hasNext()){
					String targetComp = depIterator.next();
					if(!targetRef.contains(targetComp))
						continue;
					Dependence dep = new Dependence(FUTURE_DEP, currentTx, hostComponent, targetComp, null, null);
					outDepRegistry.addDependence(dep);
					String payload = ConsistencyPayloadCreator.createPayload(hostComponent, targetComp, currentTx, ConsistencyOperationType.NOTIFY_FUTURE_CREATE);
					depNotifyService.synPost(hostComponent, targetComp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
				}
				isSetupDone.put(rootTx, true);
			}
		}
	}

	/**
	 * during updating process, we still need to maintain the dependences
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	@Deprecated
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
		// sleep until current status become valid
		Object ondemandSyncMonitor = dynamicDepMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				if (dynamicDepMgr.isOndemandSetting()) {
					LOGGER.fine("----------------ondemandSyncMonitor.wait();consistency algorithm------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doValid(txContext, dynamicDepMgr);
		
//		while(dynamicDepMgr.isOndemandSetting()){
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		// after component status become valid, doValid(...)
		
	}

	/**
	 * notify sub component there is future dep should be established
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyFutureCreate(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		Dependence dep = new Dependence(FUTURE_DEP, rootTx, srcComp, targetComp, null, null);
		inDepRegistry.addDependence(dep);
		
		Scope scope = dynamicDepMgr.getScope();
		Set<String> targetRef = new HashSet<String>();
		if(scope != null){
			targetRef.addAll(scope.getSubComponents(targetComp));
		} else{
			targetRef.addAll(dynamicDepMgr.getStaticDeps());
		}
		
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//		Set<String> staticDeps = dynamicDepMgr.getStaticDeps();
		for(String str : targetRef){
			Dependence futureDep = new Dependence(FUTURE_DEP, rootTx, targetComp, str, null, null);
			outDepRegistry.addDependence(futureDep);
			String payload = ConsistencyPayloadCreator.createPayload(targetComp, str, rootTx, ConsistencyOperationType.NOTIFY_FUTURE_CREATE);
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
	private boolean doNotifyPastRemove(String srcComp, String targetComp, String rootTx) {
		LOGGER.info("doNotifyPastRemove " + srcComp + "-->" + targetComp + " " + rootTx);
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		inDepRegistry.removeDependence(PAST_DEP, rootTx, srcComp, targetComp);
		return removeAllEdges(targetComp, rootTx, dynamicDepMgr);
	}
	
	/**
	 * be notified that a past dep create
	 * add src--->target past dep
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifyPastCreate(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		Dependence dependence = new Dependence(PAST_DEP, rootTx, srcComp, targetComp, null, null);
		inDepRegistry.addDependence(dependence);
		
		Map<String, TransactionContext> txs = dynamicDepMgr.getTxs();
		Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet().iterator();
		// flag indicate that there will be no localTxs
		// whose rootTx is T
		boolean flag = false;
		while(iterator.hasNext()){
			TransactionContext tc = iterator.next().getValue();
			if(tc.getRootTx().equals(rootTx) && !tc.getEventType().equals(TxEventType.TransactionEnd) && !tc.isFakeTx()){
				flag = true;
				break;
			}
		}
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		if(!flag){
			inDepRegistry.removeDependence(FUTURE_DEP, rootTx, targetComp, targetComp);
			inDepRegistry.removeDependence(PAST_DEP, rootTx, targetComp, targetComp);
			outDepRegistry.removeDependence(FUTURE_DEP, rootTx, targetComp, targetComp);
			outDepRegistry.removeDependence(PAST_DEP, rootTx, targetComp, targetComp);
		}
		
		removeFutureEdges(dynamicDepMgr, targetComp, rootTx);
		
		return true;
	}
	
	/**
	 * be notified sub tx ends.
	 * @param srcComp
	 * @param targetComp
	 * @param rootTx
	 * @return
	 */
	private boolean doNotifySubTxEnd(String srcComp, String targetComp, String rootTx, String parentTx, String subTx) {
		LOGGER.fine(srcComp + "-->" + targetComp + " subTx:" + subTx + " rootTx:" + rootTx);
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		
		Object ondemandSyncMonitor = dynamicDepMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			//maintain tx
			Map<String, TransactionContext> allTxs = dynamicDepMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTx);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTx, srcComp);
			subTxStatuses.put(subTx, TxEventType.TransactionEnd);
			
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			Scope scope = dynamicDepMgr.getScope();
			if(scope != null && !scope.getSubComponents(targetComp).contains(srcComp)){
				return true;
			}
			
			DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
			Dependence dependence = new Dependence(PAST_DEP, rootTx, targetComp, srcComp, null, null);
			outDepRegistry.addDependence(dependence);
			
			// notify past dep create
			DepNotifyService depNotifyService = new DepNotifyServiceImpl();
			String payload = ConsistencyPayloadCreator.createPayload(targetComp, srcComp, rootTx, ConsistencyOperationType.NOTIFY_PAST_CREATE);
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
	private boolean doAckSubTxInit(String srcComp, String targetComp, String rootTx, String parentTxID, String subTxID) {
		LOGGER.fine(srcComp + "-->" + targetComp + " subTx:" + subTxID + " rootTx:" + rootTx);
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		
		Object ondemandSyncMonitor = dynamicDepMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			Map<String, TransactionContext> allTxs = dynamicDepMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTxID);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTxID, srcComp);
			subTxStatuses.put(subTxID, TxEventType.TransactionStart);
			
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			return removeFutureEdges(dynamicDepMgr, targetComp, rootTx, parentTxID, subTxID);
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
	private boolean doNotifyFutureRemove(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		
		inDepRegistry.removeDependence(FUTURE_DEP, rootTx, srcComp, targetComp);
		boolean result = removeFutureEdges(dynamicDepMgr, targetComp, rootTx);
		
		return result; 
	}
	
	private boolean doNotifyStartRemoteSubTx(String srcComp, String targetComp, String rootTx){
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
//		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		Set<Dependence>	rtInDeps = dynamicDepMgr.getRuntimeInDeps();
		Set<Dependence> rtOutDeps = dynamicDepMgr.getRuntimeDeps();
		
		Dependence lfe = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
				rootTx, targetComp, targetComp, null, null);
		Dependence lpe = new Dependence(VersionConsistencyImpl.PAST_DEP, 
				rootTx, targetComp, targetComp, null, null);
		
		rtInDeps.add(lfe);
		rtInDeps.add(lpe);
		rtOutDeps.add(lfe);
		rtOutDeps.add(lpe);
		
		dynamicDepMgr.getInDepRegistry().removeDependence(FUTURE_DEP, rootTx, srcComp, targetComp);
//		boolean result = removeFutureEdges(dynamicDepMgr, targetComp, rootTx);
		
		return true; 
	}
	
	private boolean doNotifyRemoteUpdateDone(String srComp, String hostComp){
		LOGGER.info(hostComp + " received notifyRemoteUpdateDone from " + srComp);
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		
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
			String payload = ConsistencyPayloadCreator.createRemoteUpdateIsDonePayload(hostComp, comp, ConsistencyOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.synPost(hostComp, comp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
		}
		
		//clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
		depMgr.setScope(null);
		
		depMgr.remoteDynamicUpdateIsDone();
		
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
	private boolean removeFutureEdges(DynamicDepManagerImpl dynamicDepMgr, String currentComp, String rootTx, String currentTxID, String subTxID){
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
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
			TransactionContext txContext = dynamicDepMgr.getTxs().get(currentTxID);
			TxDepMonitor txDepMonitor = txContext.getTxDepMonitor();
			
			for(Dependence dep : outFutureOneRoot){
				boolean isLastUse = txDepMonitor.isLastUse(currentTxID, dep.getTargetCompObjIdentifer(), currentComp);
				if(isLastUse){
					outDepRegistry.removeDependence(dep);
//					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), ConsistencyOperationType.NOTIFY_FUTURE_REMOVE);
//					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
//					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), ConsistencyOperationType.NOTIFY_START_REMOTE_SUB_TX);
//					depNotifyService.synPost(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				}else{
				}
			}
			
		}

		return true;
	}
	
	
	/**
	 * try to remove future dep when receive NOTIFY_FUTURE_REMOVE
	 * according the condition to decide whether need to remove the future dep
	 * @param dynamicDepMgr
	 * @param currentComp
	 * @param rootTx
	 * @return
	 */
	private boolean removeFutureEdges(DynamicDepManagerImpl dynamicDepMgr, String currentComp, String rootTx){
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
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
				Map<String, TransactionContext> localTxs = dynamicDepMgr.getTxs();
				Iterator<Entry<String, TransactionContext>> localTxsIterator = localTxs.entrySet().iterator();
				while(localTxsIterator.hasNext()){
					TransactionContext T = localTxsIterator.next().getValue();
					
					if(T.isFakeTx())
						continue;
					
					Set<String> fDeps = T.getFutureComponents();
					for(String fdep : fDeps){
						if(fdep.equals(dep.getTargetCompObjIdentifer()) && T.getRootTx().equals(rootTx)){
							willNotUseFlag = false;
							break;
						}
					}
					if(!willNotUseFlag){
						break;
					}
				}
				if(willNotUseFlag){
					Scope scope = dynamicDepMgr.getScope();
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
					String payload = ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), ConsistencyOperationType.NOTIFY_FUTURE_REMOVE);
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
				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, ConsistencyOperationType.NOTIFY_FUTURE_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.CONSISTENCY,  MsgType.DEPENDENCE_MSG, payload);
			} else if(dep.getRootTx().equals(rootTx) && dep.getType().equals(PAST_DEP)
					&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = ConsistencyPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, ConsistencyOperationType.NOTIFY_PAST_REMOVE);
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
		
		if(depMgr.getTxDepMonitor() != null){
			depMgr.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
//		} else if(!depMgr.getTxs().isEmpty()){
//			for(Entry<String,TransactionContext> entry : depMgr.getTxs().entrySet()){
//				TransactionContext txCtx = entry.getValue();
//				txCtx.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
//				break;
//			}
		} else{
			LOGGER.warning("failed to remove rootTx " + rootTx + ", due to fail to get TxDepMonitor");
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
			if(txCtx.getRootTx().equals(rootTx)){
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
						if(inCtx.getRootTx().equals(rootTx)){
							inIte.remove();
						}
					}
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
//		LOGGER.fine("in consisitency algorithm(allInDeps):" + allInDeps);
//		LOGGER.fine("in consisitency algorithm(oldrootTxs):" + oldRootTx);
		return oldRootTx;
	}

	@Override
	public boolean isReadyForUpdate(String compIdentifier) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		depMgr = nodeMgr.getDynamicDepManager(compIdentifier);
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
				Printer printer = new Printer();
				printer.printDeps(LOGGER, deps, "ALL:");
				freeFlag = false;
				break;
			}
			
		}
		return freeFlag;
	}

	@Override
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			Set<String> bufferOldVersionRootTxs, TransactionContext txContext,
			boolean isUpdateReqRCVD) {
		if( !isUpdateReqRCVD ){
			return false;
		}
		
		String rootTx = txContext.getRootTx();
//		if((algorithmOldVersionRootTxs!=null) 
//				&& ( !algorithmOldVersionRootTxs.contains(rootTx) && !bufferOldVersionRootTxs.contains(rootTx))){
		if ((algorithmOldVersionRootTxs != null)
				&& algorithmOldVersionRootTxs.contains(rootTx) ){
			LOGGER.info(txContext.getRootTx() + " not blocked, \n algorithm:" + algorithmOldVersionRootTxs + 
						"\n buffer: " + bufferOldVersionRootTxs);
			return false;
		} else{
			LOGGER.info(txContext.getRootTx() + " is blocked, \n algorithm:" + algorithmOldVersionRootTxs + 
					"\n buffer: " + bufferOldVersionRootTxs);
			return true;
		}
	}

	@Override
	public boolean updateIsDone(String hostComp) {
		//clear local status maintained for update
		isSetupDone.clear();

		//notify parent components that dynamic update is done
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl depMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(hostComp);
		
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
			LOGGER.info("Sending NOTIFY_REMOTE_UPDATE_DONE to " + comp);
			String payload = ConsistencyPayloadCreator.createRemoteUpdateIsDonePayload(hostComp, comp, ConsistencyOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.synPost(hostComp, comp, CommProtocol.CONSISTENCY, MsgType.DEPENDENCE_MSG, payload);
		}
		
		//clear local deps
		depMgr.getRuntimeDeps().clear();
		depMgr.getRuntimeInDeps().clear();
		depMgr.setScope(null);
		
		return true;
	}

	@Override
	public void start(String identifier) {
		
	}

	@Override
	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs) {
		Set<String> result = new HashSet<String>();
		
		Iterator<Entry<String, String>> iterator;
		iterator = oldRootTxs.entrySet().iterator();
		while(iterator.hasNext()){
			result.add(iterator.next().getValue());
		}
		
		return result;
	}
	
	@Override
	public String getAlgorithmRoot(String parentTx, String rootTx) {
		return rootTx;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx,
			String parentTx, String subTx) {
		if(subTxStatus.equals(TxEventType.TransactionStart))
			return doAckSubTxInit(subComp, curComp, rootTx, parentTx, subTx);
		else if(subTxStatus.equals(TxEventType.TransactionEnd))
			return doNotifySubTxEnd(subComp, curComp, rootTx, parentTx, subTx);
		else{
			LOGGER.warning("unexpected sub transaction status: " + subTxStatus + " for rootTx " + rootTx);
			return false;
		}
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		
		Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
		Set<Dependence> rtOutDeps = depMgr.getRuntimeDeps();
		
		if( !depMgr.getCompStatus().equals(CompStatus.NORMAL) ){
			Dependence lfe = new Dependence(FUTURE_DEP, rootTx, hostComp, hostComp, null, null);
			if(!rtInDeps.contains(lfe)){
				rtInDeps.add(lfe);
			}
			if (!rtOutDeps.contains(lfe)) {
				rtOutDeps.add(lfe);
			}
			
			Dependence lpe = new Dependence(PAST_DEP, rootTx, hostComp, hostComp, null, null);
			if(!rtInDeps.contains(lpe)){
				rtInDeps.add(lpe);
			}
			if (!rtOutDeps.contains(lpe)) {
				rtOutDeps.add(lpe);
			}
		}
		return true;
	}
	
}
