package cn.edu.nju.moon.conup.core.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.comm.api.peer.services.impl.DepNotifyServiceImpl;
import cn.edu.nju.moon.conup.core.utils.QuiescenceOperationType;
import cn.edu.nju.moon.conup.core.utils.QuiescencePayload;
import cn.edu.nju.moon.conup.core.utils.QuiescencePayloadCreator;
import cn.edu.nju.moon.conup.core.utils.QuiescencePayloadResolver;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;

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
	private DynamicDepManager depMgr = null;
	
	private Set<String> REQS = new HashSet<String>();
	
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
	public void manageDependence(TransactionContext txContext) {
		// TODO Auto-generated method stub
		initDynamicDepMgr(txContext.getHostComponent());
		CompStatus compStatus = depMgr.getCompStatus();
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext);
		case VALID:
			doValid(txContext);
			break;
		case Free:
			doFree(txContext);
			break;
		default:
//			doValid(txContext);
			throw new RuntimeException("Quiescence algorithm cannot execute a transaction while component status is " + compStatus);
		}

	}

	private void doFree(TransactionContext txContext) {
		initDynamicDepMgr(txContext.getHostComponent());
		Object updatingMonitor = depMgr.getUpdatingSyncMonitor();
		synchronized(updatingMonitor){
			try {
				if (depMgr.getCompStatus().equals(CompStatus.Free)) {
					updatingMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//TODO is here a doNormal() operation required?
		doNormal(txContext);
	}

	@Override
	public boolean manageDependence(String payload) {
		boolean result = false;
		QuiescencePayloadResolver payloadResolver;
		QuiescenceOperationType operationType;
		String srcComp;
		String hostComp;
		String rootTx;
		String parentTx;
		String subTx;
		
		payloadResolver = new QuiescencePayloadResolver(payload);
		operationType = payloadResolver.getOperation();
		srcComp = payloadResolver.getParameter(QuiescencePayload.SRC_COMPONENT);
		hostComp = payloadResolver.getParameter(QuiescencePayload.TARGET_COMPONENT);
		
		Printer printer = new Printer();
		
		assert srcComp!=null;
		assert hostComp!=null;
		
		switch(operationType){
		case ACK_SUBTX_INIT:
			LOGGER.warning("deprecated notification ACK_SUBTX_INIT");
//			parentTx = payloadResolver.getParameter(QuiescencePayload.PARENT_TX);
//			subTx = payloadResolver.getParameter(QuiescencePayload.SUB_TX);
//			rootTx = payloadResolver.getParameter(QuiescencePayload.ROOT_TX);
//			result = doAckSubtxInit(srcComp, hostComp, rootTx, parentTx, subTx);
			break;
		case NOTIFY_SUBTX_END:
			LOGGER.warning("deprecated notification NOTIFY_SUBTX_END");
//			parentTx = payloadResolver.getParameter(QuiescencePayload.PARENT_TX);
//			subTx = payloadResolver.getParameter(QuiescencePayload.SUB_TX);
//			rootTx = payloadResolver.getParameter(QuiescencePayload.ROOT_TX);
//			result = doNotifySubTxEnd(srcComp, hostComp, rootTx, parentTx, subTx);
			break;
		case REQ_PASSIVATE:
			result = doReqPassivate(srcComp, hostComp);
			break;
		case ACK_PASSIVATE:
			result = doAckPassivate(srcComp, hostComp);
			break;
		case NOTIFY_REMOTE_UPDATE_DONE:
			result = doNotifyRemoteUpdateDone(srcComp, hostComp);
			break;
		case NOTIFY_ROOT_TX_END:
			LOGGER.info("deprecated notification: NOTIFY_ROOT_TX_END");
//			initDynamicDepMgr(hostComp);
//			LOGGER.fine("before process NOTIFY_ROOT_TX_END:");
//			printer.printTxs(depMgr.getTxs());
			
//			rootTx = payloadResolver.getParameter(QuiescencePayload.ROOT_TX);
//			result = doNotifyRootTxEnd(srcComp, hostComp, rootTx);
			
//			LOGGER.fine("after process NOTIFY_ROOT_TX_END:");
//			printer.printTxs(depMgr.getTxs());
			break;
		default:
			throw new RuntimeException("Undefined operation type: " + operationType);
		}
		
		return result;
	}
	
	private void doNormal(TransactionContext txContext){
		initDynamicDepMgr(txContext.getHostComponent());
		depMgr.ondemandSetupIsDone();
		doValid(txContext);
	}

	private void doValid(TransactionContext txCtx) {
		TxEventType txEventType = txCtx.getEventType();
		String hostComp = txCtx.getHostComponent();
		String rootTx = txCtx.getRootTx();
		initDynamicDepMgr(txCtx.getHostComponent());
		
		if (txEventType.equals(TxEventType.TransactionStart)) {
			if(!txCtx.getCurrentTx().equals(txCtx.getRootTx())){
//				String payload = QuiescencePayloadCreator.createPayload(hostComp, txCtx.getParentComponent(), txCtx.getRootTx(), QuiescenceOperationType.ACK_SUBTX_INIT, txCtx.getParentTx(), txCtx.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, txCtx.getParentComponent(), CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
			}
		} else if (txEventType.equals(TxEventType.TransactionEnd)) {
			
			if(!txCtx.getCurrentTx().equals(txCtx.getRootTx())){
//				String payload = QuiescencePayloadCreator.createPayload(
//						hostComp, txCtx.getParentComponent(),
//						txCtx.getRootTx(),
//						QuiescenceOperationType.NOTIFY_SUBTX_END,
//						txCtx.getParentTx(), txCtx.getCurrentTx());
//				DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//				depNotifyService.synPost(hostComp, txCtx.getParentComponent(), CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
			} else{
				Iterator<Entry<String, TransactionContext>> txIterator;
				txIterator = depMgr.getTxs().entrySet().iterator();
				while(txIterator.hasNext()){
					TransactionContext tmpTxCtx;
					tmpTxCtx = txIterator.next().getValue();
					if(tmpTxCtx.getRootTx().equals(rootTx) && !tmpTxCtx.isFakeTx()){
						txIterator.remove();
						txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
					}
				}
				Set<String> targetRef;
				targetRef = depMgr.getStaticDeps();
//				for(String subComp : targetRef){
//					String payload = QuiescencePayloadCreator.createRootTxEndPayload(hostComp, subComp, rootTx, QuiescenceOperationType.NOTIFY_ROOT_TX_END);
//					DepNotifyService depNotifyService = new DepNotifyServiceImpl();
//					depNotifyService.synPost(hostComp, subComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
//				}
				// check passive when a root tx is end
				LOGGER.fine("root tx " + rootTx + " end, checkPassiveAndAck...");
				
				LOGGER.fine("before checkPassiveAndAck:");
				Printer printer = new Printer();
				initDynamicDepMgr(hostComp);
				Map<String, TransactionContext> txs = depMgr.getTxs();
//				printer.printTxs(txs);
				checkPassiveAndAck(txCtx.getHostComponent());
				
				LOGGER.fine("after checkPassiveAndAck:");
//				printer.printTxs(txs);
			}
		} else {
			return;
		}
		
		Printer printer = new Printer();
//		printer.printTxs(depMgr.getTxs());
	}

	private boolean doNotifyRootTxEnd(String srcComp, String hostComp, String rootTx) {
		TransactionContext txCtx;
		initDynamicDepMgr(hostComp);
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
			if(txCtx.getRootTx().equals(rootTx) && txCtx.getParentComponent().equals(srcComp)){
				txIterator.remove();
				existSubTxWithRoot --;
				if(existSubTxWithRoot == 0)
					txCtx.getTxDepMonitor().rootTxEnd(hostComp, rootTx);
			}
		}
		
		Set<String> targetRef;
		targetRef = depMgr.getStaticDeps();
		for(String subComp : targetRef){
			String payload = QuiescencePayloadCreator.createRootTxEndPayload(hostComp, subComp, rootTx, QuiescenceOperationType.NOTIFY_ROOT_TX_END);
			DepNotifyService depNotifyService = new DepNotifyServiceImpl();
			depNotifyService.synPost(hostComp, subComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
		}
		
		return true;
	}

	private boolean doAckSubtxInit(String srcComp, String hostComp, String rootTx, String parentTxID, String subTxID){
		
		initDynamicDepMgr(hostComp);
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
	
	private boolean doNotifySubTxEnd(String srcComp, String hostComp, String rootTx, String parentTx, String subTx){
		
		//maintain tx
		initDynamicDepMgr(hostComp);
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
	private boolean doReqPassivate(String srcComp, String hostComp){
		LOGGER.info(hostComp + " received reqPassivate from " + srcComp);
		initDynamicDepMgr(hostComp);
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
				Printer printer = new Printer();
				initDynamicDepMgr(hostComp);
				Map<String, TransactionContext> txs = depMgr.getTxs();
//				printer.printTxs(txs);
				
				checkPassiveAndAck(hostComp);
				
				LOGGER.fine("after checkPassiveAndAck:");
//				printer.printTxs(txs);
				
			} else{
				setPassivateRCVD(true);
				Set<String> parentComps = depMgr.getStaticInDeps();
				for (String parent : parentComps) {
					DEPS.put(parent, false);
					sendReqPassivate(hostComp, parent);
				}
				checkPassiveAndAck(hostComp);
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
		LOGGER.info(hostComp + " ackPassivate to " + targetComp);
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		String payload = QuiescencePayloadCreator.createPayload(hostComp, targetComp, QuiescenceOperationType.ACK_PASSIVATE);
		depNotifyService.asynPost(hostComp, targetComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
	}

	/**
	 * 
	 * @param hostComp
	 */
	private void checkPassiveAndAck(String hostComp) {
		Iterator<Entry<String, Boolean>> iterator = DEPS.entrySet().iterator();
		boolean allACKRCVD = true;
		while(iterator.hasNext()){
			Entry<String, Boolean> entry = iterator.next();
			allACKRCVD = allACKRCVD && entry.getValue();
			if(!allACKRCVD)
				break;
		}
		initDynamicDepMgr(hostComp);
		if(allACKRCVD){
			Map<String, TransactionContext> txs = depMgr.getTxs();
			Iterator<Entry<String, TransactionContext>> txsIterator = txs.entrySet().iterator();
			boolean bePassive = true;
			while(txsIterator.hasNext()){
				Entry<String, TransactionContext> entry = txsIterator.next();
				if(!entry.getValue().getEventType().equals(TxEventType.TransactionEnd) 
					&& !entry.getValue().isFakeTx()){
					bePassive = false;
					break;
				}
			}
			if(bePassive){
				PASSIVATED = true;
				LOGGER.info("**** passive has achieved for component: " + hostComp + "***********");
				// confirm all reqPassive
				for (String reqComp : REQS) {
					ackPassivate(hostComp, reqComp);
				}
				
				if(depMgr.isUpdateRequiredComp()){
					LOGGER.info("**** QUIESCENCE has achieved for component: " + hostComp + "***********");
					depMgr.achievedFree();
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
		String payload = QuiescencePayloadCreator.createPayload(hostComp, targetComp, QuiescenceOperationType.REQ_PASSIVATE);
		depNotifyService.asynPost(hostComp, targetComp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
	}

	private boolean doAckPassivate(String srcComp, String hostComp){
		LOGGER.fine("receive ack passivate " + srcComp + "------->" + hostComp);
		DEPS.put(srcComp, true);
		
//
		LOGGER.fine("before checkPassiveAndAck:");
		Printer printer = new Printer();
		initDynamicDepMgr(hostComp);
		Map<String, TransactionContext> txs = depMgr.getTxs();
//		printer.printTxs(txs);
		
		checkPassiveAndAck(hostComp);
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
		LOGGER.fine(hostComp + " received notifyRemoteUpdateDone from " + srComp);
		initDynamicDepMgr(hostComp);
		depMgr.remoteDynamicUpdateIsDone();
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
	public boolean isReadyForUpdate(String compIdentifier) {
		initDynamicDepMgr(compIdentifier);
		return depMgr.getCompStatus().equals(CompStatus.Free);
	}

	@Override
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			Set<String> bufferOldVersionRootTxs, TransactionContext txContext,
			boolean isUpdateReqRCVD) {

		boolean isRootComp = txContext.getHostComponent().equals(
				txContext.getRootComponent());

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
	
	private void initDynamicDepMgr(String compIdentifier){
		if(this.depMgr == null)
			this.depMgr = NodeManager.getInstance().getDynamicDepManager(compIdentifier);
	}

	@Override
	public boolean updateIsDone(String hostComp) {
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		for(String comp : DEPS.keySet()){
			String payload = QuiescencePayloadCreator.createPayload(hostComp, comp, QuiescenceOperationType.NOTIFY_REMOTE_UPDATE_DONE);
			depNotifyService.asynPost(hostComp, comp, CommProtocol.QUIESCENCE, MsgType.DEPENDENCE_MSG, payload);
		}
		
		// clean up
		DEPS.clear();
		REQS.clear();
		isPassivateRCVD = false;
		PASSIVATED = false;
		return true;
	}

	@Override
	public void start(String hostComp) {
		doReqPassivate(hostComp, hostComp);
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
			return doAckSubtxInit(subComp, curComp, rootTx, parentTx, subTx);
		else if(subTxStatus.equals(TxEventType.TransactionEnd))
			return doNotifySubTxEnd(subComp, curComp, rootTx, parentTx, subTx);
		else{
			LOGGER.warning("unexpected sub transaction status: " + subTxStatus + " for rootTx " + rootTx);
			return false;
		}
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
		// TODO Auto-generated method stub
		return true;
	}

}
