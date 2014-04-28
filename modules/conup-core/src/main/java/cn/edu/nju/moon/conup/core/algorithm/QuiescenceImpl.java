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
import cn.edu.nju.moon.conup.core.utils.QuiescencePayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.datamodel.UpdateOperationType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class QuiescenceImpl implements Algorithm {
	private Logger LOGGER = Logger.getLogger(QuiescenceImpl.class.getName());
	/** dependence type is static dependent */
	public final static String STATIC_DEP = "STATIC_DEP";
	/** represent quiescence algorithm */
	public final static String ALGORITHM_TYPE = "QUIESCENCE_ALGORITHM";
	/** Has the component received passive request from sub-component */
	private boolean isPassivateRCVD = false;
	
	private Set<String> REQS = new ConcurrentSkipListSet<String>();
	
	private Map<String, Boolean> DEPS = new ConcurrentHashMap<String, Boolean>();
	
	/**
	 * A node is passivated if it does not initiate sub-transactions on any depended
	 * node any more. Without dynamic information, this can only be achieved by 
	 * (1) it is locally inactive (STOP_INITIATING_ROOT_TX&&isALL_LOCAL_ROOT_TX_ENDED()) and
	 * (2) all statically depending nodes are passivated. 
	 */
	private boolean PASSIVATED = false;

	/** is current component the target component for dynamic update */
//	public boolean amItheOneToBeQuieted = false;

	@Override
	public void manageDependence(TransactionContext txContext, DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		CompStatus compStatus = compLifeCycleMgr.getCompStatus();
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, compLifeCycleMgr, depMgr);
			break;
		case VALID:
			doValid(txContext, depMgr);
			break;
		case ONDEMAND:
			doOndemand(txContext, compLifeCycleMgr, depMgr);
			break;
		case FREE:
			doFree(txContext, compLifeCycleMgr, depMgr);
			break;
		default:
//			doValid(txContext);
			throw new RuntimeException("Quiescence algorithm cannot execute a transaction while " + txContext.getHostComponent() + " component status is " + compStatus);
		}

	}

	@Override
	public boolean manageDependence(DepOperationType operationType, Map<String, String> params, 
			DynamicDepManager depMgr,
			CompLifeCycleManager compLifeCycleMgr) {
		boolean result = false;
		
		String srcComp = params.get("srcComp");
		String hostComp = params.get("targetComp");
		
//		Printer printer = new Printer();
		
		assert srcComp!=null;
		assert hostComp!=null;
		
		switch(operationType){
		case ACK_SUBTX_INIT:
			LOGGER.warning("deprecated notification ACK_SUBTX_INIT");
//			parentTx = payloadResolver.getParameter(PayloadType.PARENT_TX);
//			subTx = payloadResolver.getParameter(PayloadType.SUB_TX);
//			rootTx = payloadResolver.getParameter(PayloadType.ROOT_TX);
//			result = doAckSubtxInit(srcComp, hostComp, rootTx, parentTx, subTx);
			break;
		case NOTIFY_SUBTX_END:
			LOGGER.warning("deprecated notification NOTIFY_SUBTX_END");
//			parentTx = payloadResolver.getParameter(PayloadType.PARENT_TX);
//			subTx = payloadResolver.getParameter(PayloadType.SUB_TX);
//			rootTx = payloadResolver.getParameter(PayloadType.ROOT_TX);
//			result = doNotifySubTxEnd(srcComp, hostComp, rootTx, parentTx, subTx);
			break;
		case REQ_PASSIVATE:
			result = doReqPassivate(srcComp, hostComp, depMgr);
			break;
		case ACK_PASSIVATE:
			result = doAckPassivate(srcComp, hostComp, depMgr);
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			result = doNotifyRemoteUpdateDone(srcComp, hostComp);
			break;
		case NOTIFY_ROOT_TX_END:
			LOGGER.info("deprecated notification: NOTIFY_ROOT_TX_END");
//			initDynamicDepMgr(hostComp);
//			LOGGER.fine("before process NOTIFY_ROOT_TX_END:");
//			printer.printTxs(depMgr.getTxs());
			
//			rootTx = payloadResolver.getParameter(PayloadType.ROOT_TX);
//			result = doNotifyRootTxEnd(srcComp, hostComp, rootTx);
			
//			LOGGER.fine("after process NOTIFY_ROOT_TX_END:");
//			printer.printTxs(depMgr.getTxs());
			break;
		default:
			throw new RuntimeException("Undefined operation type: " + operationType);
		}
		
		return result;
	}
	
	/**
	 * current component status is ondemand, suspend current execution until 
	 * status becomes to valid
	 * @param txContext
	 * @param dynamicDepMgr
	 */
	private void doOndemand(TransactionContext txContext, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		// sleep until current status become valid
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
					LOGGER.fine("----------------ondemandSyncMonitor.wait();Quiescence algorithm------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doValid(txContext, depMgr);
		
	}
	
	private void doNormal(TransactionContext txCtx, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr){
		if (txCtx.getEventType().equals(TxEventType.TransactionEnd)) {
			String hostComp;
			String rootTx;

			hostComp = txCtx.getHostComponent();
			rootTx = txCtx.getRootTx();
			if(rootTx.equals(txCtx.getCurrentTx()))
				LOGGER.fine("rootTx " + rootTx + " on " + hostComp + " ends.");
			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				if(compLifeCycleMgr.getCompStatus().equals(CompStatus.NORMAL)){
					depMgr.getTxs().remove(txCtx.getCurrentTx());
					depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, rootTx);
					LOGGER.fine("removed tx from TxRegistry and TxDepMonitor, local tx: " + txCtx.getCurrentTx() + ", rootTx: " + rootTx);
					
					return;
				} else{
					try {
						if (compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
							LOGGER.fine("----------------ondemandSyncMonitor.wait();quiesence algorithm------------");
							ondemandSyncMonitor.wait();
							doValid(txCtx, depMgr);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void doValid(TransactionContext txCtx, DynamicDepManager depMgr) {
		TxEventType txEventType = txCtx.getEventType();
		String hostComp = txCtx.getHostComponent();
		String rootTx = txCtx.getRootTx();
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
//			if(!txCtx.getCurrentTx().equals(txCtx.getRootTx())){
//				String payload = QuiescencePayloadCreator.createPayload(hostComp, txCtx.getParentComponent(), txCtx.getRootTx(), OperationType.ACK_SUBTX_INIT, txCtx.getParentTx(), txCtx.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, txCtx.getParentComponent(), CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
//			}
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			if(rootTx.equals(txCtx.getCurrentTx()))
				LOGGER.fine("rootTx " + rootTx + " on " + hostComp + " ends.");
			if(!txCtx.getCurrentTx().equals(txCtx.getRootTx())){
				depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, rootTx);
				depMgr.getTxs().remove(txCtx.getCurrentTx());
			} else{
				depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, rootTx);
				depMgr.getTxs().remove(txCtx.getCurrentTx());
				// check passive when a root tx is end
				LOGGER.fine("root tx " + rootTx + " end, checkPassiveAndAck...");
				
				LOGGER.fine("before checkPassiveAndAck:");
//				Printer printer = new Printer();
//				initDynamicDepMgr(hostComp);
//				Map<String, TransactionContext> txs = depMgr.getTxs();
//				printer.printTxs(txs);
				checkPassiveAndAck(txCtx.getHostComponent(), depMgr);
				
				LOGGER.fine("after checkPassiveAndAck:");
//				printer.printTxs(txs);
			}
		} else {
			return;
		}
		
//		Printer printer = new Printer();
//		printer.printTxs(depMgr.getTxs());
	}

	private void doFree(TransactionContext txContext, CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		Object updatingMonitor = compLifeCycleMgr.getCompObject().getUpdatingSyncMonitor();
		synchronized (updatingMonitor) {
			try {
				if (compLifeCycleMgr.getCompStatus().equals(CompStatus.FREE)) {
					updatingMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// TODO is here a doNormal() operation required?
		doNormal(txContext, compLifeCycleMgr, depMgr);
	}

	private boolean doAckSubtxInit(String srcComp, String hostComp, String rootTx, String parentTxID, String subTxID, DynamicDepManager depMgr){
		
		Map<String, TransactionContext> allTxs = depMgr.getTxs();
		TransactionContext txCtx;
		txCtx = allTxs.get(parentTxID);
		assert(txCtx!=null);
		Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
		Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
		subTxHostComps.put(subTxID, srcComp);
		subTxStatuses.put(subTxID, TxEventType.TransactionStart);
		
		return true;
	}
	
	private boolean doNotifySubTxEnd(String srcComp, String hostComp, String rootTx, String parentTx, String subTx, DynamicDepManager depMgr){
		
		//maintain tx
		Map<String, TransactionContext> allTxs = depMgr.getTxs();
		TransactionContext txCtx;
		txCtx = allTxs.get(parentTx);
		assert(txCtx!=null);
		Map<String, String> subTxHostComps = txCtx.getSubTxHostComps();
		Map<String, TxEventType> subTxStatuses = txCtx.getSubTxStatuses();
		subTxHostComps.put(subTx, srcComp);
		subTxStatuses.put(subTx, TxEventType.TransactionEnd);
		
		return true;
		
	}
	
	/**
	 * process when receive passivate message
	 * @param srComp
	 * @param hostComp
	 * @return
	 */
	private boolean doReqPassivate(String srcComp, String hostComp, DynamicDepManager depMgr){
		LOGGER.info(hostComp + " received reqPassivate from " + srcComp);
		if(PASSIVATED){
			if(!srcComp.equals(hostComp)){
				ackPassivate(hostComp, srcComp);
			}
		} else{
			if(!srcComp.equals(hostComp)){
				REQS.add(srcComp);
				setPassivateRCVD(true);
				Set<String> parentComps = depMgr.getStaticInDeps();
				for (String parent : parentComps) {
					DEPS.put(parent, false);
					sendReqPassivate(hostComp, parent);
				}
//
				LOGGER.fine("before checkPassiveAndAck:");
//				Printer printer = new Printer();
//				initDynamicDepMgr(hostComp);
//				Map<String, TransactionContext> txs = depMgr.getTxs();
//				printer.printTxs(txs);
				
				checkPassiveAndAck(hostComp, depMgr);
				
				LOGGER.fine("after checkPassiveAndAck:");
//				printer.printTxs(txs);
				
			} else{
				setPassivateRCVD(true);
				Set<String> parentComps = depMgr.getStaticInDeps();
				for (String parent : parentComps) {
					DEPS.put(parent, false);
					sendReqPassivate(hostComp, parent);
				}
				checkPassiveAndAck(hostComp, depMgr);
			}
		}
		
		return true;
	}
	
	/**
	 * when state become passive, send ack to srcComp
	 * @param hostComp
	 * @param targetComp
	 */
	private void ackPassivate(String hostComp, String targetComp) {
		LOGGER.fine(hostComp + " ackPassivate to " + targetComp);
		LOGGER.fine(hostComp + "  " + "isPassivateRCVD:" + isPassivateRCVD + "  isPASSIVATED:" + PASSIVATED);
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		String payload = QuiescencePayloadCreator.createPayload(hostComp, targetComp, DepOperationType.ACK_PASSIVATE);
		depNotifyService.asynPost(hostComp, targetComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
	}

	/**
	 * 
	 * @param hostComp
	 */
	private void checkPassiveAndAck(String hostComp, DynamicDepManager depMgr) {
		if( !isPassivateRCVD)
			return;
		
		Iterator<Entry<String, Boolean>> iterator = DEPS.entrySet().iterator();
		boolean allACKRCVD = true;
		while(iterator.hasNext()){
			Entry<String, Boolean> entry = iterator.next();
			allACKRCVD = allACKRCVD && entry.getValue();
			if(!allACKRCVD)
				break;
		}
		if(allACKRCVD){
			Map<String, TransactionContext> txs = depMgr.getTxs();
			Iterator<Entry<String, TransactionContext>> txsIterator = txs.entrySet().iterator();
			boolean bePassive = true;
			while(txsIterator.hasNext()){
				Entry<String, TransactionContext> entry = txsIterator.next();
				if(!entry.getValue().getEventType().equals(TxEventType.TransactionEnd) ){
//					&& !entry.getValue().isFakeTx()){
					LOGGER.info("not become passive, because rootTx " + entry.getValue().getRootTx() + " running on " + hostComp);
					bePassive = false;
					break;
				}
			}
			if(bePassive){
				PASSIVATED = true;
				LOGGER.info("**** passive has achieved for component: " + hostComp + "***********");
//				Printer printer = new Printer();
//				LOGGER.info("TxRegistry on " + hostComp + " when it achievd passive");
//				printer.printTxs(LOGGER, depMgr.getTxs());
				
				// confirm all reqPassive
//				Iterator<String> reqsIter = REQS.iterator();
//				while(reqsIter.hasNext()){
//					String reqComp = reqsIter.next();
//					reqsIter.remove();
//					ackPassivate(hostComp, reqComp);
//				}
				for (String reqComp : REQS) {
					ackPassivate(hostComp, reqComp);
				}
				
				if(depMgr.getCompObject().isTargetComp()){
					LOGGER.info("**** QUIESCENCE has achieved for component: " + hostComp + "***********");
//					compLifeCycleMgr.achieveFree();
					assert hostComp.equals(depMgr.getCompObject().getIdentifier());
					UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(hostComp);
					updateMgr.achieveFree();
				}
			}
		}
	}

	/**
	 * send passivate request
	 * @param hostComp
	 * @param targetComp
	 */
	private void sendReqPassivate(String hostComp, String targetComp) {
		LOGGER.info(hostComp + " sendReqPassivate to " + targetComp);
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		String payload = QuiescencePayloadCreator.createPayload(hostComp, targetComp, DepOperationType.REQ_PASSIVATE);
		depNotifyService.asynPost(hostComp, targetComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
	}

	private boolean doAckPassivate(String srcComp, String hostComp, DynamicDepManager depMgr){
		LOGGER.info("receive ack passivate " + srcComp + "------->" + hostComp);
		DEPS.put(srcComp, true);
		
//
		LOGGER.fine("before checkPassiveAndAck:");
//		Printer printer = new Printer();
//		Map<String, TransactionContext> txs = depMgr.getTxs();
//		printer.printTxs(txs);
		
		checkPassiveAndAck(hostComp, depMgr);
		LOGGER.fine("after checkPassiveAndAck:");
//		printer.printTxs(txs);
		
		return true;
	}
	
	/**
	 * 
	 * @param srComp
	 * @param hostComp
	 * @return
	 */
	private boolean doNotifyRemoteUpdateDone(String srComp, String hostComp){
		LOGGER.info(hostComp + " received notifyRemoteUpdateDone from " + srComp);
		
		//notify parent components that remote dynamic update is done
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for(String comp : DEPS.keySet()){
			String payload = QuiescencePayloadCreator.createPayload(hostComp, comp, DepOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.synPost(hostComp, comp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
		}
		
		// clean up
		DEPS.clear();
		REQS.clear();
		isPassivateRCVD = false;
		PASSIVATED = false;
		
//		compLifeCycleMgr.remoteDynamicUpdateIsDone();
		UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(hostComp);
		updateMgr.remoteDynamicUpdateIsDone();
		return true;
	}

	@Override
	public String getAlgorithmType() {
		return QuiescenceImpl.ALGORITHM_TYPE;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps) {
		return new HashSet<String>();
	}

	@Override
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD, DynamicDepManager depMgr) {	

//		boolean isRootComp = txContext.getHostComponent().equals(
//				txContext.getRootComponent());
//		boolean isRootComp = txContext.getHostComponent().equals(
//				depMgr.getCompObject().getIdentifier());
		
		boolean isRootComp = (txContext.getRootTx()==null);
		
		LOGGER.fine(txContext.getHostComponent() + "  " + "isUpdateReqRCVD:" + isUpdateReqRCVD + "  isPassivateRCVD:" + isPassivateRCVD + "  isPASSIVATED:" + PASSIVATED + "  isRootComp:" + isRootComp);
//		LOGGER.fine("txCtx:\n" + txContext);
		
		if (!isUpdateReqRCVD) {
			if (!isPassivateRCVD) {
				return false;
			} else {
				if (isRootComp)
					return true;
				else
					return false;
			}
		} else { // received update request
			if (isRootComp) {
				return true;
			} else {
				return false;
			}
		}

	}

	public boolean isPassivateRCVD() {
		return isPassivateRCVD;
	}

	public void setPassivateRCVD(boolean isPassivateRCVD) {
		this.isPassivateRCVD = isPassivateRCVD;
	}
	
	@Override
	public void initiate(String hostComp, DynamicDepManager depMgr) {
		doReqPassivate(hostComp, hostComp, depMgr);
	}

	@Override
	public boolean readyForUpdate(String compIdentifier,
			DynamicDepManager depMgr) {
		CompLifeCycleManager compLifeCycleMgr = NodeManager.getInstance().getCompLifecycleManager(compIdentifier);
		return compLifeCycleMgr.getCompStatus().equals(CompStatus.FREE);
	}

	@Override
	public boolean updateIsDone(String hostComp, DynamicDepManager depMgr) {
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for(String comp : DEPS.keySet()){
			String payload = QuiescencePayloadCreator.createPayload(hostComp, comp, DepOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.synPost(hostComp, comp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
		}
		
		// clean up
		DEPS.clear();
		REQS.clear();
		isPassivateRCVD = false;
		PASSIVATED = false;
		
//		depNotifyService.asynPost(hostComp, "Coordination", CommProtocol.QUIESCENCE, 
//				MsgType.EXPERIMENT_MSG, UpdateContextPayloadCreator.createPayload(
//				UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP));
		return true;
	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr,
			String proxyRootTxId) {
		
		String subComp = invocationCtx.getSubComp();
		String curComp = invocationCtx.getParentComp();
		String rootTx = invocationCtx.getRootTx();
		String parentTx = invocationCtx.getParentTx();
		String subTx = invocationCtx.getSubTx();
		if(subTxStatus.equals(TxEventType.TransactionStart))
			return doAckSubtxInit(subComp, curComp, rootTx, parentTx, subTx, depMgr);
		else if(subTxStatus.equals(TxEventType.TransactionEnd))
			return doNotifySubTxEnd(subComp, curComp, rootTx, parentTx, subTx, depMgr);
		else{
			LOGGER.warning("unexpected sub transaction status: " + subTxStatus + " for rootTx " + rootTx);
			return false;
		}
	}

}
