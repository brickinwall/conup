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
import cn.edu.nju.moon.conup.core.TransactionRegistry;
import cn.edu.nju.moon.conup.core.manager.impl.DynamicDepManagerImpl;
import cn.edu.nju.moon.conup.core.utils.ConsistencyOperationType;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayloadCreator;
import cn.edu.nju.moon.conup.core.utils.TranquillityOndemandPayloadResolver;
import cn.edu.nju.moon.conup.core.utils.TranquillityOperationType;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayload;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayloadCreator;
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
	public void manageDependence(TransactionContext txContext) {
		
		LOGGER.fine("TranquillityImpl.manageDependence(...)");
		String hostComponent = txContext.getHostComponent();
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(hostComponent);
		CompStatus compStatus = dynamicDepMgr.getCompStatus();
		
		assert dynamicDepMgr != null;
		
		assert compStatus != null;
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
		
		TranquillityOndemandPayloadResolver payloadResolver = new TranquillityOndemandPayloadResolver(payload);
		String srcComp = payloadResolver.getParameter(TranquillityPayload.SRC_COMPONENT);
		String targetComp = payloadResolver.getParameter(TranquillityPayload.TARGET_COMPONENT);
		String rootTx = payloadResolver.getParameter(TranquillityPayload.ROOT_TX);
		TranquillityOperationType operationTypeInfo = payloadResolver.getOperation();
		
//		String[] payloadInfos = payload.split(",");
//		String srcComp = payloadInfos[0].split(":")[1];
//		String targetComp = payloadInfos[1].split(":")[1];
//		String rootTx = payloadInfos[2].split(":")[1];
//		String operationTypeInfo = payloadInfos[3].split(":")[1];
//		TranquillityOperationType eventType = null;
		
		DynamicDepManager ddm = nodeMgr.getDynamicDepManager(targetComp);
		
//		if(operationTypeInfo != null){
//			eventType = Enum.valueOf(TranquillityOperationType.class, operationTypeInfo);
//		}
		assert operationTypeInfo != null;
		
		switch(operationTypeInfo){
		case NOTIFY_FUTURE_CREATE:
//			LOGGER.info("before process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyFutureCreate(srcComp, targetComp, rootTx);
			
//			LOGGER.info("after process NOTIFY_FUTURE_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_FUTURE_REMOVE:
//			LOGGER.info("before process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyFutureRemove(srcComp, targetComp, rootTx);
			
//			LOGGER.info("after process NOTIFY_FUTURE_REMOVE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			break;
		case ACK_SUBTX_INIT:
//			LOGGER.info("before process ACK_SUBTX_INIT:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
//			String parentTxID = payloadInfos[4].split(":")[1];
//			String subTxID = payloadInfos[5].split(":")[1];
			String parentTxID = payloadResolver.getParameter(TranquillityPayload.PARENT_TX);
			String subTxID = payloadResolver.getParameter(TranquillityPayload.SUB_TX);
			manageDepResult = doAckSubTxInit(srcComp, targetComp, rootTx, parentTxID, subTxID);
			
//			LOGGER.info("after process ACK_SUBTX_INIT:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_SUBTX_END:
//			LOGGER.info("before process NOTIFY_SUBTX_END:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
//			String subTxEndParentTxID = payloadInfos[4].split(":")[1];
//			String subTxEndSubTxID = payloadInfos[5].split(":")[1];
			String subTxEndParentTxID = payloadResolver.getParameter(TranquillityPayload.PARENT_TX);
			String subTxEndSubTxID = payloadResolver.getParameter(TranquillityPayload.SUB_TX);
			manageDepResult = doNotifySubTxEnd(srcComp, targetComp, rootTx, subTxEndParentTxID, subTxEndSubTxID);
			
//			LOGGER.info("after process NOTIFY_SUBTX_END:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_PAST_CREATE:
//			LOGGER.info("before process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
			manageDepResult = doNotifyPastCreate(srcComp, targetComp, rootTx);
			
//			LOGGER.info("after process NOTIFY_PAST_CREATE:");
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			break;
		case NOTIFY_PAST_REMOVE:
			manageDepResult = doNotifyPastRemove(srcComp, targetComp, rootTx);
			break;
		case NORMAL_ROOT_TX_END:
			TranquillityOndemandPayloadResolver resolver;
			resolver = new TranquillityOndemandPayloadResolver(payload);
			manageDepResult = doNormalRootTxEnd(resolver.getParameter(TranquillityPayload.SRC_COMPONENT),
					resolver.getParameter(TranquillityPayload.TARGET_COMPONENT),
					resolver.getParameter(TranquillityPayload.ROOT_TX));
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			manageDepResult = doNotifyRemoteUpdateDone(srcComp, targetComp);
			break;
		}
		return manageDepResult;
	}
	
	private void doNormal(TransactionContext txCtx, DynamicDepManagerImpl depMgr){
		//if current tx is not a root tx
		if (!txCtx.getCurrentTx().equals(txCtx.getParentTx())) {
			String hostComp = txCtx.getHostComponent();
			if (txCtx.getEventType().equals(TxEventType.TransactionStart)) {
				String payload = TranquillityPayloadCreator.createPayload(
						hostComp, txCtx.getParentComponent(),
						txCtx.getParentTx(),
						TranquillityOperationType.ACK_SUBTX_INIT,
						txCtx.getParentTx(), txCtx.getCurrentTx());
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComp, txCtx.getParentComponent(),
						VersionConsistencyImpl.ALGORITHM_TYPE,
						MsgType.DEPENDENCE_MSG, payload);
			} else if (txCtx.getEventType().equals(TxEventType.TransactionEnd)) {
				String payload = TranquillityPayloadCreator.createPayload(
						hostComp, txCtx.getParentComponent(),
						txCtx.getParentTx(),
						TranquillityOperationType.NOTIFY_SUBTX_END,
						txCtx.getParentTx(), txCtx.getCurrentTx());
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComp, txCtx.getParentComponent(),
						CommProtocol.TRANQUILLITY,
						MsgType.DEPENDENCE_MSG, payload);
			}
		}
		//if a root tx ends, it should recursively notify its sub-components
		if(txCtx.getEventType().equals(TxEventType.TransactionEnd)){
			String hostComp;
			String rootTx;
			Set<String> targetRef;
			
			hostComp = txCtx.getHostComponent();
			rootTx = txCtx.getParentTx();
			
			targetRef = new HashSet<String>();
			targetRef.addAll(txCtx.getFutureComponents());
			targetRef.addAll(txCtx.getPastComponents());
			
			//remove local root tx, just useful to portal
			if(txCtx.getCurrentTx().equals(txCtx.getParentTx())){
				depMgr.getTxs().remove(rootTx);
//				txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
				//TODO Bug comes here: the third parameter is extremely confusing.
				txCtx.getTxDepMonitor().rootTxEnd(hostComp, txCtx.getParentTx(), txCtx.getRootTx());
			}
			
			for(String subComp : targetRef){
				String payload = TranquillityPayloadCreator.createNormalRootTxEndPayload(hostComp, subComp, txCtx.getCurrentTx(), TranquillityOperationType.NORMAL_ROOT_TX_END);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComp, subComp, CommProtocol.TRANQUILLITY, MsgType.DEPENDENCE_MSG, payload);
			}
		}
		
	}
	
	private boolean doNormalRootTxEnd(String srcComp, String currentComp, String rootTx){
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl depMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(currentComp);
		TransactionContext txCtx = null;
		
		Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			Iterator<Entry<String, TransactionContext>>  txIterator = depMgr.getTxs().entrySet().iterator();
			while(txIterator.hasNext()){
				txCtx = txIterator.next().getValue();
				if(txCtx.getParentTx().equals(rootTx) ){
					txIterator.remove();
				}
			}
			if(txCtx != null){
				//TODO Bug comes here: the third parameter is extremely confusing.
				txCtx.getTxDepMonitor().rootTxEnd(currentComp, rootTx, rootTx);
			}
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
		String parentTx = txContext.getParentTx();
		String currentTx = txContext.getCurrentTx();
		String hostComponent = txContext.getHostComponent();
		
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
		Set<String> futureComponents = txContext.getFutureComponents();
		
		Set<String> targetRef = null;
//		Set<String> parentComps = null;
		Scope scope;
		
		targetRef = new HashSet<String>();
		scope = dynamicDepMgr.getScope();
		String rootTx = null;
		
		//calculate target(sub) components
		if(scope == null){
			targetRef.addAll(dynamicDepMgr.getStaticDeps());
		}
		else{
			targetRef.addAll(scope.getSubComponents(hostComponent));
		}
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
			Set<String> parentComps;
			if(scope == null){
				parentComps = dynamicDepMgr.getStaticInDeps();
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
			
			NodeManager nodeMgr = NodeManager.getInstance();
			Printer printer = new Printer();
//			LOGGER.info("doVALID(TxEventType.TransactionStart):print deps:");
			DynamicDepManager ddm = nodeMgr.getDynamicDepManager(hostComponent);
//			printer.printDeps(ddm.getRuntimeInDeps(), "In");
//			printer.printDeps(ddm.getRuntimeDeps(), "Out");
			
			/* ACK_SUBTX_INIT
			 * current transaction is not root
			 * notify parent that a new sub-tx start
			 */
			if( !parentTx.equals(currentTx) ) {
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), parentTx, TranquillityOperationType.ACK_SUBTX_INIT, txContext.getParentTx(), txContext.getCurrentTx());
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
						String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), currentTx, TranquillityOperationType.NOTIFY_FUTURE_REMOVE);
						DepNotifyService depNotifyService = new DepNotifyServiceImpl();
						depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
					}
				}
			}
			
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			/**
			 * if currentTx is not root, need to notify parent sub_tx_end
			 * else if currentTx is root, start cleanup
			 */
			boolean isTarget = scope.isTarget(hostComponent);
			if(isTarget){
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, txContext.getParentComponent(), parentTx, TranquillityOperationType.NOTIFY_SUBTX_END, txContext.getParentTx(), txContext.getCurrentTx());
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, txContext.getParentComponent(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
			} else{
				inDepRegistry.removeDependence(FUTURE_DEP, currentTx, hostComponent, hostComponent);
				inDepRegistry.removeDependence(PAST_DEP, currentTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(FUTURE_DEP, currentTx, hostComponent, hostComponent);
				outDepRegistry.removeDependence(PAST_DEP, currentTx, hostComponent, hostComponent);
				removeAllEdges(hostComponent, currentTx, dynamicDepMgr);
			}
			
		} else {
			Set<String> fDeps = txContext.getFutureComponents();
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
								.createPayload(	hostComponent, targetComp, currentTx, TranquillityOperationType.NOTIFY_FUTURE_CREATE);
						depNotifyService.synPost(hostComponent, targetComp, CommProtocol.TRANQUILLITY, MsgType.DEPENDENCE_MSG, payload);
					}
				}
			}

		}
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
					System.out
							.println("----------------ondemandSyncMonitor.wait();tranquillity algorithm------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// after component status become valid, doValid(...)
		doValid(txContext, dynamicDepMgr);
		
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
	private boolean doNotifyFutureCreate(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
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
	private boolean doNotifyPastRemove(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl depMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = depMgr.getInDepRegistry();
		inDepRegistry.removeDependence(PAST_DEP, rootTx, srcComp, targetComp);
		LOGGER.fine(PAST_DEP + " rootTx:" + rootTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
		
		//remove tx
		TransactionContext txCtx = null;
		for (Entry<String, TransactionContext> entry : depMgr.getTxs().entrySet()) {
			txCtx = entry.getValue();
			if (txCtx.getParentTx().equals(rootTx)) {
				depMgr.getTxs().remove(entry.getKey());
			}
		}
		if(txCtx != null){
//			txCtx.getTxDepMonitor().rootTxEnd(targetComp, rootTx);
			//TODO Bug comes here: the third parameter is extremely confusing.
			txCtx.getTxDepMonitor().rootTxEnd(targetComp, rootTx, rootTx);
		}else {
			LOGGER.fine("txCtx == null");
		}
		
		
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
	private boolean doNotifyPastCreate(String srcComp, String targetComp, String rootTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		DependenceRegistry inDepRegistry = dynamicDepMgr.getInDepRegistry();
		Dependence dependence = new Dependence(PAST_DEP, rootTx, srcComp, targetComp, null, null);
		LOGGER.fine(PAST_DEP + " create rootTx:" + rootTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
		inDepRegistry.addDependence(dependence);
		
		Map<String, TransactionContext> txs = dynamicDepMgr.getTxs();
		Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet().iterator();
		// flag indicate that there will be no localTxs
		// whose rootTx is T
		boolean flag = false;
		while(iterator.hasNext()){
			TransactionContext tc = iterator.next().getValue();
			if(tc.getParentTx().equals(rootTx) && !tc.getEventType().equals(TxEventType.TransactionEnd)){
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
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl dynamicDepMgr = (DynamicDepManagerImpl) nodeManager.getDynamicDepManager(targetComp);
		
		Object ondemandSyncMonitor = dynamicDepMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			LOGGER.fine("doNotifySubTxEnd rootTx:" + rootTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
			//maintain tx
			Map<String, TransactionContext> allTxs = dynamicDepMgr.getTxs();
			TransactionContext txCtx;
			txCtx = allTxs.get(parentTx);
			assert(txCtx!=null);
			Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
			Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
			subTxHostComps.put(subTx, srcComp);
			subTxStatuses.put(subTx, TxEventType.TransactionEnd);
//			txCtx.getPastComponents().add(srcComp);
			
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.NORMAL))
				return true;
			
			Scope scope = dynamicDepMgr.getScope();
			if(scope!=null && !scope.getSubComponents(targetComp).contains(srcComp)){
				return true;
			}
			
			Set<String> subComps;
			if(scope == null){
				subComps = dynamicDepMgr.getStaticDeps();
			} else{
				subComps = scope.getSubComponents(targetComp);
			}
			
			if(subComps.contains(srcComp)){
				DependenceRegistry outDepRegistry = dynamicDepMgr.getOutDepRegistry();
				Dependence dependence = new Dependence(PAST_DEP, rootTx, targetComp, srcComp, null, null);
				outDepRegistry.addDependence(dependence);
				
				// notify past dep create
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				String payload = TranquillityPayloadCreator.createPayload(targetComp, srcComp, rootTx, TranquillityOperationType.NOTIFY_PAST_CREATE);
				depNotifyService.synPost(targetComp, srcComp, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
				
				if(txCtx.getTxDepMonitor().isLastUse(txCtx.getCurrentTx(), srcComp, txCtx.getHostComponent())){
					payload = TranquillityPayloadCreator.createPayload(targetComp, srcComp, rootTx, TranquillityOperationType.NOTIFY_PAST_REMOVE);
					depNotifyService.synPost(targetComp, srcComp, ALGORITHM_TYPE, MsgType.DEPENDENCE_MSG, payload);
					outDepRegistry.removeDependence(dependence);
				}
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
	private boolean doAckSubTxInit(String srcComp, String targetComp, String rootTx, String parentTxID, String subTxID) {
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
		
		LOGGER.fine(FUTURE_DEP + " remove rootTx:" + rootTx + " srcComp:" + srcComp + " targetComp:" +targetComp);
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
	private boolean doNotifyRemoteUpdateDone(String srComp, String hostComp){
		LOGGER.info(hostComp + " received notifyRemoteUpdateDone from " + srComp);
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		
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
		
//		Dependence outFutureOneRoot = null;
		Iterator<Dependence> iterator = outFutureDeps.iterator();
		while(iterator.hasNext()){
			Dependence dep = iterator.next(); 
			if(dep.getRootTx().equals(rootTx) && !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				outFutureOneRoot.add(dep);
//				outFutureOneRoot = dep;
//				break;
			}
		}
		
//		if(outFutureOneRoot == null){
//			throw new RuntimeException("outFutureOneRoot should not be null");
//		}
		
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
		
//		boolean willNotUseFlag = true;
		
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		
		if(!inFutureFlag){
//			TransactionContext txContext = TransactionRegistry.getInstance().getTransactionContext(currentTxID);
			TransactionContext txContext = dynamicDepMgr.getTxs().get(currentTxID);
			TxDepMonitor txDepMonitor = txContext.getTxDepMonitor();
			
			for(Dependence dep : outFutureOneRoot){
				boolean isLastUse = txDepMonitor.isLastUse(currentTxID, dep.getTargetCompObjIdentifer(), currentComp);
				if(isLastUse){
					outDepRegistry.removeDependence(dep);
					String payload = TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), TranquillityOperationType.NOTIFY_FUTURE_REMOVE);
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

		TransactionContext curTxCtx = depMgr.getTxs().get(rootTx);
		Map<String, String> subTxHostComps = curTxCtx.getSubTxHostComps();
		Iterator<Entry<String, String>> subCompsIterator = subTxHostComps
				.entrySet().iterator();
		while (subCompsIterator.hasNext()) {
			Entry<String, String> subCompEntry = subCompsIterator.next();
			String subComp = subCompEntry.getValue();
			Dependence dep = new Dependence(PAST_DEP, rootTx, hostComponent,
					subComp, null, null);
			if (!rtOutDeps.contains(dep)) {
				String payload = TranquillityPayloadCreator
						.createNormalRootTxEndPayload(hostComponent, subComp,
								rootTx,
								TranquillityOperationType.NORMAL_ROOT_TX_END);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, subComp,
						CommProtocol.TRANQUILLITY, MsgType.DEPENDENCE_MSG,
						payload);
			}
		}
		
		//remove deps
		for(Dependence dep : rtOutDeps){
			if(dep.getRootTx().equals(rootTx) && dep.getType().equals(FUTURE_DEP)
				&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, TranquillityOperationType.NOTIFY_FUTURE_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
			} else if(dep.getRootTx().equals(rootTx) && dep.getType().equals(PAST_DEP)
					&& !dep.getSrcCompObjIdentifier().equals(dep.getTargetCompObjIdentifer())){
				String payload = TranquillityPayloadCreator.createPayload(hostComponent, dep.getTargetCompObjIdentifer(), rootTx, TranquillityOperationType.NOTIFY_PAST_REMOVE);
				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
				depNotifyService.synPost(hostComponent, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY,  MsgType.DEPENDENCE_MSG, payload);
				LOGGER.fine("removeAllEdges:PAST_DEP:rootTx:" + rootTx + ":srcHost:" + hostComponent + ":targetHost:" + dep.getTargetCompObjIdentifer());
			}
			
			if(dep.getRootTx().equals(rootTx)){
				rtOutDeps.remove(dep);
			}
		}
		
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		parentComps = scope.getParentComponents(hostComponent);
		if(parentComps == null || parentComps.size() == 0 ){
			//remove tx
			for (Entry<String, TransactionContext> entry : depMgr.getTxs().entrySet()) {
				TransactionContext txCtx = entry.getValue();
//				if (txCtx.getParentTx().equals(rootTx)) {
				if(txCtx.getCurrentTx().equals(rootTx)){
					txCtx.getTxDepMonitor().rootTxEnd(hostComponent, txCtx.getParentTx(), txCtx.getRootTx());
					depMgr.getTxs().remove(entry.getKey());
				}
			}
		}
		
		//remove tx
//		for (Entry<String, TransactionContext> entry : depMgr.getTxs().entrySet()) {
//			TransactionContext txCtx = entry.getValue();
////			if (txCtx.getParentTx().equals(rootTx)) {
//			if(txCtx.getCurrentTx().equals(rootTx)){
////				txCtx.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
////				depMgr.getTxs().remove(entry.getKey());
//				//if there are any in deps marked with rootTx, the rootTx id should not be removed
//				Set<Dependence> rtInDeps = depMgr.getRuntimeInDeps();
//				boolean isPastDepExist = false;
//				for(Dependence dep : rtInDeps){
//					if(dep.getRootTx().equals(rootTx) ){
//						isPastDepExist = true;
//						break;
//					}
//				}
//				if( !isPastDepExist ){
//					txCtx.getTxDepMonitor().rootTxEnd(hostComponent, rootTx);
//					//remove all the tx associated with the given rootTx
//					Iterator<Entry<String, TransactionContext>> inIte;
//					inIte = depMgr.getTxs().entrySet().iterator();
//					while(inIte.hasNext()){
//						TransactionContext inCtx = inIte.next().getValue();
//						if(inCtx.getParentTx().equals(rootTx)){
//							inIte.remove();
//						}
//					}
//				}
//			}
//		}
		
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
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			Set<String> bufferOldVersionRootTxs, TransactionContext txContext,
			boolean isUpdateReqRCVD) {
		if( !isUpdateReqRCVD ){
			return false;
		}
		
		String rootTx = txContext.getParentTx();
		if((algorithmOldVersionRootTxs!=null) 
				&& (algorithmOldVersionRootTxs.contains(rootTx) || bufferOldVersionRootTxs.contains(rootTx))){
			return false;
		} else{
			return true;
		}
	}

	@Override
	public boolean updateIsDone(String hostComp) {
		// clear local status maintained for update
		isSetupDone.clear();

		// notify parent components that dynamic update is done
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManagerImpl depMgr = (DynamicDepManagerImpl) nodeManager
				.getDynamicDepManager(hostComp);

		// notify parent components that remote dynamic update is done
		Scope scope = depMgr.getScope();
		Set<String> parentComps;
		if (scope != null) {
			parentComps = scope.getParentComponents(hostComp);
		} else {
			parentComps = depMgr.getCompObject().getStaticInDeps();
		}
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for (String comp : parentComps) {
			LOGGER.info("Sending NOTIFY_REMOTE_UPDATE_DONE to " + comp);
			String payload = TranquillityPayloadCreator
					.createRemoteUpdateIsDonePayload(hostComp, comp,
							TranquillityOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.synPost(hostComp, comp, CommProtocol.CONSISTENCY,
					MsgType.DEPENDENCE_MSG, payload);
		}

		// clear local deps
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
			result.add(iterator.next().getKey());
		}
		
		return result;
	}
	
	@Override
	public String getAlgorithmRoot(String parentTx, String rootTx) {
		return parentTx;
	}

}
