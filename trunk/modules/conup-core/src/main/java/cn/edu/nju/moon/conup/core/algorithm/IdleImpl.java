package cn.edu.nju.moon.conup.core.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.DepNotifyService;
import cn.edu.nju.moon.conup.comm.api.peer.services.impl.DepNotifyServiceImpl;
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
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.UpdateContextPayloadCreator;

public class IdleImpl implements Algorithm {
	private Logger LOGGER = Logger.getLogger(IdleImpl.class.getName());
	/** represent Trivial algorithm */
	public final static String ALGORITHM_TYPE = "IDLE_ALGORITHM";

	@Override
	public void manageDependence(TransactionContext txContext,
			DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		CompStatus compStatus = compLifeCycleMgr.getCompStatus();
		switch (compStatus) {
		case NORMAL:
			doNormal(txContext, compLifeCycleMgr, depMgr);
			break;
		case VALID:
			doNormal(txContext, compLifeCycleMgr, depMgr);
			break;
		case FREE:
			doFree(txContext, compLifeCycleMgr, depMgr);
			break;
		default:
//			doValid(txContext);
			throw new RuntimeException("IDLE algorithm cannot execute a transaction while " + txContext.getHostComponent() + " component status is " + compStatus);
		}
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
				depMgr.getTxs().remove(txCtx.getCurrentTx());
				depMgr.getTxLifecycleMgr().rootTxEnd(hostComp, rootTx);
				LOGGER.fine("removed tx from TxRegistry and TxDepMonitor, local tx: "
						+ txCtx.getCurrentTx() + ", rootTx: " + rootTx);

				return;
			}
		}
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

	@Override
	public boolean manageDependence(DepOperationType operationType,
			Map<String, String> params, DynamicDepManager depMgr,
			CompLifeCycleManager compLifeCycleMgr) {
		return true;
	}

	@Override
	public boolean readyForUpdate(String compIdentifier,
			DynamicDepManager depMgr) {
		Map<String, TransactionContext> txs = depMgr.getTxs();
		if(txs.size() == 0){
			return true;
		} else {
			Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet().iterator();
			while(iterator.hasNext()){
				TransactionContext txCtx = iterator.next().getValue();
				if(!txCtx.isFakeTx() && !txCtx.getEventType().equals(TxEventType.TransactionEnd)){
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps) {
		return new HashSet<String>();
	}

	@Override
	public String getAlgorithmType() {
		return ALGORITHM_TYPE;
	}

	@Override
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD,
			DynamicDepManager depMgr) {
		return false;
	}

	@Override
	public boolean updateIsDone(String hostComp, DynamicDepManager depMgr) {
		// add for experiment
		DepNotifyService depNotifyService = new DepNotifyServiceImpl();
		depNotifyService.asynPost(hostComp, "Coordination", CommProtocol.CONSISTENCY, 
				MsgType.EXPERIMENT_MSG, UpdateContextPayloadCreator.createPayload(
				UpdateOperationType.NOTIFY_UPDATE_IS_DONE_EXP));
		return false;
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

	@Override
	public void initiate(String hostComp, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub

	}

}
