package cn.edu.nju.moon.conup.core.ondemand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.impl.OndemandDynDepSetupServiceImpl;
import cn.edu.nju.moon.conup.core.algorithm.TranquillityImpl;
import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.OperationType;
import cn.edu.nju.moon.conup.spi.utils.PayloadResolver;
import cn.edu.nju.moon.conup.spi.utils.PayloadType;
import cn.edu.nju.moon.conup.spi.utils.Printer;
import cn.edu.nju.moon.conup.spi.utils.XMLUtil;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class TranquillityOndemandSetupImpl implements OndemandSetup {
	private final static Logger LOGGER = Logger.getLogger(TranquillityOndemandSetupImpl.class.getName());

	/**
	 * outer map's key is hostCompName
	 * inner map's key is subComponentName
	 */
	public static Map<String, Map<String, Boolean>> OndemandRequestStatus = new HashMap<String, Map<String, Boolean>>();
	/**
	 * outer map's key is hostCompName
	 * inner map's key is parentComponentName
	 */
	public static Map<String, Map<String, Boolean>> ConfirmOndemandStatus = new HashMap<String, Map<String, Boolean>>();

	private OndemandSetupHelper ondemandHelper;
	private CompLifeCycleManager compLifeCycleMgr = null;
	private DynamicDepManager depMgr = null;

	private boolean isOndemandDone;

	private TxDepRegistry txDepRegistry = null;

	@Override
	public boolean ondemand() {
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
		Scope scope = calcScope();
		depMgr.setScope(scope);
		
		if(depMgr.getRuntimeInDeps().size() != 0){
			depMgr.getRuntimeInDeps().clear();
		}
		if(depMgr.getRuntimeDeps().size() != 0){
			depMgr.getRuntimeDeps().clear();
		}
		LOGGER.fine("ondemand()....");
		Printer printer = new Printer();
		printer.printTxs(LOGGER, depMgr.getTxs());
		
		assert scope != null;
		assert depMgr.getRuntimeInDeps().size() == 0;
		assert depMgr.getRuntimeDeps().size() == 0;
		
		return reqOndemandSetup(hostComp, hostComp);
	}

	@Override
	public boolean ondemandSetup(String srcComp, String proctocol,
			String payload) {
		PayloadResolver payloadResolver;
		OperationType operation;
		String curComp;// current component

		payloadResolver = new PayloadResolver(payload);
		operation = payloadResolver.getOperation();
		curComp = payloadResolver.getParameter(PayloadType.TARGET_COMPONENT);
		if (operation.equals(OperationType.REQ_ONDEMAND_SETUP)) {
			String scopeString = payloadResolver.getParameter(PayloadType.SCOPE);
			if (scopeString != null && !scopeString.equals("")
					&& !scopeString.equals("null")) {
				Scope scope = Scope.inverse(scopeString);
				depMgr.setScope(scope);
			}
			if (depMgr.getScope() == null) {
				Scope scope = calcScope();
				depMgr.setScope(scope);
			}
			reqOndemandSetup(curComp, srcComp);
		} else if (operation.equals(OperationType.CONFIRM_ONDEMAND_SETUP)) {
			confirmOndemandSetup(srcComp, curComp);
		} else if (operation.equals(OperationType.NOTIFY_FUTURE_ONDEMAND)) {
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP,
					payloadResolver.getParameter(PayloadType.ROOT_TX),
					srcComp, curComp, null, null);
			notifyFutureOndemand(dep);
		} else if (operation.equals(OperationType.NOTIFY_PAST_ONDEMAND)) {
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP,
					payloadResolver.getParameter(PayloadType.ROOT_TX),
					srcComp, curComp, null, null);
			notifyPastOndemand(dep);
		}
		
		assert depMgr.getScope() != null;
		LOGGER.fine("Scope:\n\t" + depMgr.getScope());
		return true;
	}

	public void setOndemandHelper(OndemandSetupHelper ondemandHelper) {
		this.ondemandHelper = ondemandHelper;
	}

	public boolean isOndemandDone() {
		return isOndemandDone;
	}

	@Override
	public String getAlgorithmType() {
		return TranquillityImpl.ALGORITHM_TYPE;
	}

	/**
	 * A reqOndemandSetup(...) is sent by current(host) component's
	 * sub-component If currentComponent.equals(requestSourceComponent), it
	 * means this is a request from domain manager
	 * 
	 * @param currentComp
	 * @param current
	 *            component's sub-component
	 * 
	 */
	public boolean reqOndemandSetup(String currentComp, String requestSrcComp) {
		// suspend all the threads that is initiated

		LOGGER.fine("**** in reqOndemandSetup(...):" + "\t"
				+ "currentComponent=" + currentComp + "\t"
				+ "requestSourceComponent=" + requestSrcComp);

		String hostComp = null;
		Set<String> parentComps = new HashSet<String>();
		Scope scope;

		hostComp = currentComp;
		
		//change component status to ONDEMAND
//		compLifeCycleMgr.ondemandSetting();
		UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
		updateMgr.ondemandSetting();

		//in this case, it means that current component is the target component for dynamic update
		if (currentComp.equals(requestSrcComp)) {
			scope = depMgr.getScope();
			if (scope == null) {
				parentComps.addAll(ondemandHelper.getCompObject().getStaticInDeps());
			} else {
				parentComps.addAll(scope.getParentComponents(hostComp));
			}

			// init ConfirmOndemandStatus
			Map<String, Boolean> confirmStatus;
			if(ConfirmOndemandStatus.containsKey(currentComp)){
				confirmStatus = ConfirmOndemandStatus.get(currentComp);
			} else{
				confirmStatus = new HashMap<String, Boolean>();
				ConfirmOndemandStatus.put(currentComp, confirmStatus);
			}
			for (String component : parentComps) {
				if( !confirmStatus.containsKey(component)){
					confirmStatus.put(component, false);
				}
//				if (!ConfirmOndemandStatus.containsKey(component))
//					ConfirmOndemandStatus.put(component, false);
			}
			sendReqOndemandSetup(parentComps, currentComp);

		} else{	//current component is not the target component
			ConfirmOndemandStatus.put(currentComp, new HashMap<String, Boolean>());
		}

		//onDemandSetUp
		Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)){
				LOGGER.fine("synchronizing for method onDemandSetUp() in TranquillityOndemandSetupImpl..");
				onDemandSetUp(currentComp, requestSrcComp);
			}
		}
//		onDemandSetUp(currentComp, requestSrcComp);

		// isConfirmedAll?
		Map<String, Boolean> confirmStatus = ConfirmOndemandStatus.get(currentComp);
		boolean isConfirmedAll = true;
		for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}

		// print ConfirmOndemandStatus
		LOGGER.fine("ConfirmOndemandStatus:");
		for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
			LOGGER.fine("\t" + entry.getKey() + ": " + entry.getValue());
		}

		if (isConfirmedAll) {
			if (compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)) {
				LOGGER.fine("Confirmed all, and component status is valid");
				return true;
			}
			LOGGER.fine("Confirmed from all parent components in reqOndemandSetup(...)");
			LOGGER.fine("trying to change mode to valid");
			
//			Printer printer = new Printer();
//			printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeInDeps(), "in");
//			printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeDeps(), "out");
			
			// change current componentStatus to 'valid'
			//TODO dd
//			ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
//			compLifeCycleMgr.ondemandSetupIsDone();
//			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
			updateMgr.ondemandSetupIsDone();
			// send confirmOndemandSetup(...)
			if(!currentComp.equals(requestSrcComp))
				sendConfirmOndemandSetup(currentComp, requestSrcComp);
		}
		
		return true;
	}

	/**
	 * send ondemand confirm to reqSrcComp
	 * @param currentComp
	 * @param requestSrcComp
	 */
	private void sendConfirmOndemandSetup(String currentComp,
			String requestSrcComp) {
		LOGGER.fine("sendConfirmOndemandSetup(...) to sub components:\n\t" + requestSrcComp);

		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;
		payload = PayloadType.OPERATION_TYPE + ":"
				+ OperationType.CONFIRM_ONDEMAND_SETUP + ","
				+ PayloadType.SRC_COMPONENT + ":" + currentComp + ","
				+ PayloadType.TARGET_COMPONENT + ":" + requestSrcComp;
		ondemandComm.asynPost(currentComp, requestSrcComp,
				CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, payload);

		// ondemand setup is done
		isOndemandDone = true;
	}

	private boolean onDemandSetUp(String currentComp, String requestSrcComp) {

		Scope scope;
		String hostComp;
	
		hostComp = ondemandHelper.getCompObject().getIdentifier();
		scope = depMgr.getScope();
	
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		Map<String, TransactionContext> txs;
		rtInDeps = depMgr.getRuntimeInDeps();
		rtOutDeps = depMgr.getRuntimeDeps();
		txs = depMgr.getTxs();

		Set<Dependence> fDeps = new HashSet<Dependence>();
		Set<Dependence> pDeps = new HashSet<Dependence>();
		String curComp = ondemandHelper.getCompObject().getIdentifier();

		Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet().iterator();
		while (iterator.hasNext()) {
			TransactionContext txCtx = (TransactionContext) iterator.next().getValue();
			String rootTx = null;
//			String parentTx = null;
			String curTx = null;
			if (scope.getParentComponents(hostComp) == null
					|| scope.getParentComponents(hostComp).size() == 0) {
				if(txCtx.isFakeTx())
					continue;
				rootTx = txCtx.getCurrentTx();
			} else{
				rootTx = txCtx.getParentTx();
			}
			assert (txCtx.getHostComponent().equals(curComp));
			curTx = txCtx.getCurrentTx();
//			parentTx = txCtx.getParentTx();
			// in this case, it means current entry in interceptorCache is
			// invalid
			if (rootTx == null || curComp == null) {
				LOGGER.warning("Invalid data found while onDemandSetUp");
				continue;
			}
			// for the ended non-root txs, no need to create the lfe, lpe
			if(!rootTx.equals(curTx) && txCtx.getEventType().equals(TxEventType.TransactionEnd)){
				continue;
			}

			Dependence lfe = new Dependence(VersionConsistencyImpl.FUTURE_DEP,
					rootTx, curComp, curComp, null, null);

			Dependence lpe = new Dependence(VersionConsistencyImpl.PAST_DEP,
					rootTx, curComp, curComp, null, null);

			rtInDeps.add(lfe);
			rtInDeps.add(lpe);
			rtOutDeps.add(lfe);
			rtOutDeps.add(lpe);

			// if current transaction is not a root transaction
			if (!rootTx.equals(curTx)) {
//				LOGGER.fine("current tx is " + curTx + ",and root is " + rootTx);
				LOGGER.fine("current tx is " + curTx + ",and root is " + rootTx);
				continue;
			}
			
			if(txCtx.isFakeTx())
				continue;

			// if current is root
			LOGGER.fine(curTx + " is a root tx");
			
			fDeps = getFDeps(curComp, rootTx);
			LOGGER.fine("fDeps:");
			for (Dependence dep : fDeps) {
				if(!scope.getSubComponents(curComp).contains(dep.getTargetCompObjIdentifer())){
					continue;
				}
				dep.setType(VersionConsistencyImpl.FUTURE_DEP);
				dep.setRootTx(rootTx);
				
				String targetComp = dep.getTargetCompObjIdentifer();
				Map<String, TxEventType> subTxStatus = txCtx.getSubTxStatuses();
				Map<String, String> subComps = txCtx.getSubTxHostComps();
				String subTxID = null;
				Iterator<Entry<String, String>> subCompsIterator = subComps.entrySet().iterator();
				boolean subFlag = false;
				while(subCompsIterator.hasNext()){
					Entry<String, String> entry = subCompsIterator.next();
					if(entry.getValue().equals(targetComp)){
						subTxID = entry.getKey();
						if(!subTxStatus.get(subTxID).equals(TxEventType.TransactionEnd)){
							subFlag = true;
							break;
						}
					}
				}
				boolean flag = true;
				if(subFlag){
					boolean isLastuse = true;
//					TxDepMonitor txDepMonitor = txCtx.getTxDepMonitor();
					TxDepMonitor txDepMonitor = NodeManager.getInstance().getTxDepMonitor(curComp);
					isLastuse = txDepMonitor.isLastUse(txCtx.getCurrentTx(), targetComp, curComp);
					flag = flag && isLastuse;
				} else{
					flag = false;
				}
				
				if (!rtOutDeps.contains(dep) && dep.getTargetCompObjIdentifer().equals(requestSrcComp) && !flag) {
					// add to dependence registry
					rtOutDeps.add(dep);
					// notify future on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, 
							TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), OperationType.NOTIFY_FUTURE_ONDEMAND));
				}
			}

			pDeps = getPDeps(curComp, rootTx);
			LOGGER.fine("pArcs:");
			for (Dependence dep : pDeps) {
				if(!scope.getSubComponents(curComp).contains(dep.getTargetCompObjIdentifer())){
					continue;
				}
				dep.setType(VersionConsistencyImpl.PAST_DEP);
				dep.setRootTx(rootTx);
				if (!rtOutDeps.contains(dep)
						&& dep.getTargetCompObjIdentifer().equals(
								requestSrcComp)) {
					// add to dependence registry
					rtOutDeps.add(dep);
					// notify past on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, 
							TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), OperationType.NOTIFY_PAST_ONDEMAND));
				}
			}
		}// END WHILE

		return true;
	}

	@Deprecated
	public boolean onDemandSetUp() {
		Set<String> targetRef;
		Scope scope;
		String hostComp;
	
		hostComp = ondemandHelper.getCompObject().getIdentifier();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
	
		if (scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
	
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		Map<String, TransactionContext> txs;
		rtInDeps = depMgr.getRuntimeInDeps();
		rtOutDeps = depMgr.getRuntimeDeps();
		txs = depMgr.getTxs();
	
		Set<Dependence> fDeps = new HashSet<Dependence>();
		Set<Dependence> pDeps = new HashSet<Dependence>();
		// Set<Dependence> sDeps = new HashSet<Dependence>();
		String curComp = ondemandHelper.getCompObject().getIdentifier();
	
		Iterator<Entry<String, TransactionContext>> iterator = txs.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			TransactionContext txCtx = (TransactionContext) iterator.next()
					.getValue();
			String rootTx = null;
			String parentTx = null;
			String curTx = null;
			if (scope.getParentComponents(hostComp) == null
					|| scope.getParentComponents(hostComp).size() == 0) {
				rootTx = txCtx.getCurrentTx();
			}
			assert (txCtx.getHostComponent().equals(curComp));
			curTx = txCtx.getCurrentTx();
			parentTx = txCtx.getParentTx();
	
			// in this case, it means current entry in interceptorCache is
			// invalid
			// ???
			if (rootTx == null || curComp == null) {
				LOGGER.warning("Invalid data found while onDemandSetUp");
				continue;
			}
	
			Dependence lfe = new Dependence(VersionConsistencyImpl.FUTURE_DEP,
					parentTx, curComp, curComp, null, null);
	
			Dependence lpe = new Dependence(VersionConsistencyImpl.PAST_DEP,
					parentTx, curComp, curComp, null, null);
	
			rtInDeps.add(lfe);
			rtInDeps.add(lpe);
			rtOutDeps.add(lfe);
			rtOutDeps.add(lpe);
	
			// if current transaction is not a root transaction
			if (!rootTx.equals(curTx)) {
				LOGGER.fine("current tx is " + curTx + ",and root is "
						+ rootTx);
				continue;
			}
	
			// if current is root
			LOGGER.fine(curTx + " is a root tx");
	
			fDeps = getFDeps(curComp, rootTx);
			LOGGER.fine("fDeps:");
			for (Dependence dep : fDeps) {
				dep.setType(VersionConsistencyImpl.FUTURE_DEP);
				dep.setRootTx(rootTx);
				if (!rtOutDeps.contains(dep)) {
					// add to dependence registry
					rtOutDeps.add(dep);
					// notify future on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(),
							CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, 
							TranquillityPayloadCreator.createPayload(
									dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(),	dep.getRootTx(),
									OperationType.NOTIFY_FUTURE_ONDEMAND));
				}
			}
	
			pDeps = getPDeps(curComp, rootTx);
			LOGGER.fine("pArcs:");
			for (Dependence dep : pDeps) {
				dep.setType(VersionConsistencyImpl.PAST_DEP);
				dep.setRootTx(rootTx);
				if (!rtOutDeps.contains(dep)) {
					// add to dependence registry
					rtOutDeps.add(dep);
					// notify past on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), 
							CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, 
							TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(),
									dep.getTargetCompObjIdentifer(), dep.getRootTx(),
									OperationType.NOTIFY_PAST_ONDEMAND));
				}
			}
	
		}// END WHILE
	
		return true;
	}

	/**
	 * parent send confirm Ondemand msg to sub 
	 * @param parentComp
	 * @param currentComp
	 * @return
	 */
	public boolean confirmOndemandSetup(String parentComp, String currentComp) {
		LOGGER.fine("**** " + "confirmOndemandSetup(...) from " + parentComp);
		if (compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)) {
			LOGGER.fine("**** component status is valid, and return");
			return true;
		}

		// update current component's ConfirmOndemandStatus
		Map<String, Boolean> confirmStatus = ConfirmOndemandStatus.get(currentComp);
		if (confirmStatus.containsKey(parentComp))
			confirmStatus.put(parentComp, true);
		else
			LOGGER.fine("Illegal status while confirmOndemandSetup(...)");
		
//		if (ConfirmOndemandStatus.containsKey(parentComp))
//			ConfirmOndemandStatus.put(parentComp, true);
//		else
//			LOGGER.info("Illegal status while confirmOndemandSetup(...)");

		// print ConfirmOndemandStatus
		LOGGER.fine("ConfirmOndemandStatus:");
		for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
			LOGGER.fine("\t" + entry.getKey() + ": " + entry.getValue());
		}

		// isConfirmedAll?
		boolean isConfirmedAll = true;
		for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}
		if (isConfirmedAll) {
			// change current componentStatus to 'valid'
			LOGGER.fine("confirmOndemandSetup(...) from " + parentComp
					+ ", and confirmed All, trying to change mode to valid");
			// TODO
			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
			updateMgr.ondemandSetupIsDone();
//			compLifeCycleMgr.ondemandSetupIsDone();
//			ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
			// send confirmOndemandSetup(...) no need to send confirm!!(because ondemand only concerns two components)
//			sendConfirmOndemandSetup(currentComp);
		}

		return true;
	}

	/**
	 * 
	 * @param dep
	 *            a dependence that another component points to current
	 *            component
	 * @return
	 */
	public boolean notifyFutureOndemand(Dependence dep) {
		LOGGER.fine("notifyFutureOndemand(Dependence dep) with " + dep.toString());
		Set<Dependence> rtInDeps;

		rtInDeps = depMgr.getRuntimeInDeps();

		// add to in dependence registry
		rtInDeps.add(dep);

		return true;
	}

	/**
	 * a dependence that another component depends on current component
	 * @param dep
	 * @return
	 */
	public boolean notifyPastOndemand(Dependence dep) {
		LOGGER.fine("notifyPastOndemand(Arc arc) with " + dep.toString());
		Set<Dependence> rtInDeps;

		rtInDeps = depMgr.getRuntimeInDeps();

		rtInDeps.add(dep);
		return true;
	}


	private boolean sendReqOndemandSetup(Set<String> parentComps,
			String hostComp) {
		LOGGER.fine("sendReqOndemandSetup(...) to parent components:");
		for (String component : parentComps)
			LOGGER.fine("\t" + component);

		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;

		for (String parent : parentComps) {
			Scope scope = new Scope();
			Set<String> parentSet = new HashSet<String>();
			Set<String> subSet = new HashSet<String>();
			Set<String> targetSet = new HashSet<String>();
			targetSet.add(hostComp);
			subSet.add(hostComp);
			scope.addComponent(parent, parentSet, subSet);
			
			parentSet.clear();
			subSet.clear();
			parentSet.add(parent);
			scope.addComponent(hostComp, parentSet, subSet);
			scope.setTarget(targetSet);
			payload = PayloadType.OPERATION_TYPE + ":"
					+ OperationType.REQ_ONDEMAND_SETUP + ","
					+ PayloadType.SRC_COMPONENT + ":" + hostComp + ","
					+ PayloadType.TARGET_COMPONENT + ":" + parent + ","
					+ PayloadType.SCOPE + ":" + scope.toString();
			ondemandComm.asynPost(hostComp, parent, CommProtocol.TRANQUILLITY,
					MsgType.ONDEMAND_MSG, payload);
		}

		return true;
	}

	/**
	 * transaction ID (ATTENTION: txID maybe a sub-tx)
	 * @param curComp
	 * @param txID
	 * @return
	 */
	private Set<Dependence> getFDeps(String curComp, String txID) {
		Set<String> targetRef;
		Scope scope;
		String hostComp;

		hostComp = ondemandHelper.getCompObject().getIdentifier();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();

		if (scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));

		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> futureC = new HashSet<String>();
		Map<String, TransactionContext> txs;

		txs = depMgr.getTxs();

		String rootTx = null;

		// read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getCurrentTx().equals(txID)) {
//				if (ctx.getFutureComponents() != null
//						|| ctx.getFutureComponents().size() != 0) {
				if (txDepRegistry.getLocalDep(txID).getFutureComponents() != null
						|| txDepRegistry.getLocalDep(txID).getFutureComponents().size() != 0) {
					rootTx = ctx.getCurrentTx();
//					for (String comp : ctx.getFutureComponents()) {
					for (String comp : txDepRegistry.getLocalDep(txID).getFutureComponents()) {
						if (targetRef.contains(comp)) {
							futureC.add(comp);
						}
					}
				}
			}
		}// END WHILE

		for (String component : futureC) {
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP,
					rootTx, curComp, component, null, null);
			result.add(dep);
		}// END FOR

		// FOR TEST
		LOGGER.fine("In getFDeps(...), size=" + result.size());
		for (Dependence dep : result) {
			LOGGER.fine("\t" + dep.toString());
		}

		return result;
	}

	private Set<Dependence> getPDeps(String curComp, String txID) {
		Set<String> targetRef;
		Scope scope;
		String hostComp;

		hostComp = ondemandHelper.getCompObject().getIdentifier();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();

		if (scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));

		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> pastC = new HashSet<String>();

		Map<String, TransactionContext> txs;

		txs = depMgr.getTxs();

		String rootTx = null;

		// read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getCurrentTx().equals(txID)) {
//				if (ctx.getPastComponents() != null
//						|| ctx.getPastComponents().size() != 0) {
				if (txDepRegistry.getLocalDep(txID).getPastComponents() != null
						|| txDepRegistry.getLocalDep(txID).getPastComponents().size() != 0) {	
					rootTx = ctx.getCurrentTx();
					for (String comp : txDepRegistry.getLocalDep(txID).getPastComponents()) {
						if (targetRef.contains(comp)) {
							pastC.add(comp);
						}
					}
				}
			}
		}// END WHILE

		for (String component : pastC) {
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP,
					rootTx, curComp, component, null, null);
			result.add(dep);
		}// END FOR

		// FOR TEST

		LOGGER.fine("In getPArcs(...), size=" + result.size());
		for (Dependence dep : result) {
			LOGGER.fine("\t" + dep.toString());
		}

		return result;
	}


	/**
	 * calc all involved components with target component
	 * @return
	 */
	public Scope calcScope() {
		Scope scope = new Scope();

		XMLUtil xmlUtil = new XMLUtil();
		String compIdentifier = ondemandHelper.getCompObject().getIdentifier();

		Set<String> scopeComps = xmlUtil.getParents(compIdentifier);

		scope.addComponent(compIdentifier, scopeComps, new HashSet<String>());

		Set<String> parentSet = null;
		Set<String> subSet = null;
		for (String parent : scopeComps) {
			parentSet = new HashSet<String>();
			subSet = new HashSet<String>();
			subSet.add(compIdentifier);
			scope.addComponent(parent, parentSet, subSet);
		}

		Set<String> targetComps = new HashSet<String>();
		targetComps.add(ondemandHelper.getCompObject().getIdentifier());
		scope.setTarget(targetComps);

		return scope;
	}

	@Override
	public void onDemandIsDone() {
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
		OndemandRequestStatus.remove(hostComp);
		ConfirmOndemandStatus.remove(hostComp);
//		OndemandRequestStatus.clear();
//		ConfirmOndemandStatus.clear();
	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		this.txDepRegistry  = txDepRegistry;
	}

	@Override
	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr) {
		this.compLifeCycleMgr = compLifeCycleMgr;
	}

	@Override
	public void setDepMgr(DynamicDepManager depMgr) {
		this.depMgr = depMgr;
	}

}
