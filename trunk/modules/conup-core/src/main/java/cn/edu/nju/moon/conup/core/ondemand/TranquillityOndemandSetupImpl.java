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
import cn.edu.nju.moon.conup.core.utils.TranquillityOndemandPayloadResolver;
import cn.edu.nju.moon.conup.core.utils.TranquillityOperationType;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayload;
import cn.edu.nju.moon.conup.core.utils.TranquillityPayloadCreator;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;
import cn.edu.nju.moon.conup.spi.utils.XMLUtil;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class TranquillityOndemandSetupImpl implements OndemandSetup {
	private final static Logger LOGGER = Logger.getLogger(TranquillityOndemandSetupImpl.class.getName());

	public static Map<String, Boolean> OndemandRequestStatus = new HashMap<String, Boolean>();
	public static Map<String, Boolean> ConfirmOndemandStatus = new HashMap<String, Boolean>();

	public static Logger getLogger() {
		return LOGGER;
	}

	private OndemandSetupHelper ondemandHelper;

	private boolean isOndemandDone;

	@Override
	public boolean ondemand() {
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
		Scope scope = calcScope();
		ondemandHelper.getDynamicDepManager().setScope(scope);
		return reqOndemandSetup(hostComp, hostComp);
	}

	@Override
	public boolean ondemandSetup(String srcComp, String proctocol,
			String payload) {
		TranquillityOndemandPayloadResolver payloadResolver;
		TranquillityOperationType operation;
		String curComp;// current component

		payloadResolver = new TranquillityOndemandPayloadResolver(payload);
		operation = payloadResolver.getOperation();
		curComp = payloadResolver.getParameter(TranquillityPayload.TARGET_COMPONENT);
		if (operation.equals(TranquillityOperationType.REQ_ONDEMAND_SETUP)) {
			String scopeString = payloadResolver.getParameter(TranquillityPayload.SCOPE);
			if (scopeString != null && !scopeString.equals("")
					&& !scopeString.equals("null")) {
				Scope scope = Scope.inverse(scopeString);
				ondemandHelper.getDynamicDepManager().setScope(scope);
			}
			if (ondemandHelper.getDynamicDepManager().getScope() == null) {
				Scope scope = calcScope();
				ondemandHelper.getDynamicDepManager().setScope(scope);
			}
			reqOndemandSetup(curComp, srcComp);
		} else if (operation.equals(TranquillityOperationType.CONFIRM_ONDEMAND_SETUP)) {
			confirmOndemandSetup(srcComp, curComp);
		} else if (operation.equals(TranquillityOperationType.NOTIFY_FUTURE_ONDEMAND)) {
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP,
					payloadResolver.getParameter(TranquillityPayload.ROOT_TX),
					srcComp, curComp, null, null);
			notifyFutureOndemand(dep);
		} else if (operation.equals(TranquillityOperationType.NOTIFY_PAST_ONDEMAND)) {
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP,
					payloadResolver.getParameter(TranquillityPayload.ROOT_TX),
					srcComp, curComp, null, null);
			notifyPastOndemand(dep);
		}
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

		LOGGER.info("**** in reqOndemandSetup(...):" + "\t"
				+ "currentComponent=" + currentComp + "\t"
				+ "requestSourceComponent=" + requestSrcComp);

		String hostComp = null;
		Set<String> parentComps = new HashSet<String>();
		Scope scope;

		hostComp = currentComp;
		
		//change component status to ONDEMAND
		ondemandHelper.getDynamicDepManager().setCompStatus(CompStatus.ONDEMAND);

		//in this case, it means that current component is the target component for dynamic update
		if (currentComp.equals(requestSrcComp)) {
			scope = ondemandHelper.getDynamicDepManager().getScope();
			if (scope == null) {
				parentComps.addAll(ondemandHelper.getCompObject().getStaticInDeps());
			} else {
				parentComps.addAll(scope.getParentComponents(hostComp));
			}

			// init ConfirmOndemandStatus
			for (String component : parentComps) {
				if (!ConfirmOndemandStatus.containsKey(component))
					ConfirmOndemandStatus.put(component, false);
			}
			sendReqOndemandSetup(parentComps, currentComp);

		}

		//onDemandSetUp
		Object ondemandSyncMonitor = ondemandHelper.getDynamicDepManager().getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			if(ondemandHelper.getDynamicDepManager().isOndemandSetting()){
				LOGGER.fine("synchronizing for method onDemandSetUp() in TranquillityOndemandSetupImpl..");
				onDemandSetUp(currentComp, requestSrcComp);
			}
		}
//		onDemandSetUp(currentComp, requestSrcComp);

		// isConfirmedAll?
		boolean isConfirmedAll = true;
		for (Entry<String, Boolean> entry : ConfirmOndemandStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}

		// print ConfirmOndemandStatus
		LOGGER.fine("ConfirmOndemandStatus:");
		for (Entry<String, Boolean> entry : ConfirmOndemandStatus.entrySet()) {
			LOGGER.fine("\t" + entry.getKey() + ": " + entry.getValue());
		}

		if (isConfirmedAll) {
			if (ondemandHelper.getDynamicDepManager().isValid()) {
				LOGGER.fine("Confirmed all, and component status is valid");
				return true;
			}
			LOGGER.fine("Confirmed from all parent components in reqOndemandSetup(...)");
			LOGGER.fine("trying to change mode to valid");
			
			Printer printer = new Printer();
			printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeInDeps(), "in");
			printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeDeps(), "out");
			
			// change current componentStatus to 'valid'
			ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
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
		payload = TranquillityPayload.OPERATION_TYPE + ":"
				+ TranquillityOperationType.CONFIRM_ONDEMAND_SETUP + ","
				+ TranquillityPayload.SRC_COMPONENT + ":" + currentComp + ","
				+ TranquillityPayload.TARGET_COMPONENT + ":" + requestSrcComp;
		ondemandComm.asynPost(currentComp, requestSrcComp,
				CommProtocol.TRANQUILLITY, MsgType.ONDEMAND_MSG, payload);

		// ondemand setup is done
		isOndemandDone = true;
	}

	private boolean onDemandSetUp(String currentComp, String requestSrcComp) {

		Scope scope;
		String hostComp;
	
		hostComp = ondemandHelper.getCompObject().getIdentifier();
		scope = ondemandHelper.getDynamicDepManager().getScope();
	
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		Map<String, TransactionContext> txs;
		depMgr = ondemandHelper.getDynamicDepManager();
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
			if(txCtx.getEventType().equals(TxEventType.TransactionEnd)){
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

			// if current is root
//			LOGGER.fine(curTx + " is a root tx");
			LOGGER.fine(curTx + " is a root tx");

			
			fDeps = getFDeps(curComp, rootTx);
			LOGGER.fine("fDeps:");
			for (Dependence dep : fDeps) {
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
					TxDepMonitor txDepMonitor = txCtx.getTxDepMonitor();
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
							TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), TranquillityOperationType.NOTIFY_FUTURE_ONDEMAND));
				}
			}

			pDeps = getPDeps(curComp, rootTx);
			LOGGER.fine("pArcs:");
			for (Dependence dep : pDeps) {
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
							TranquillityPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), dep.getTargetCompObjIdentifer(), dep.getRootTx(), TranquillityOperationType.NOTIFY_PAST_ONDEMAND));
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
		scope = ondemandHelper.getDynamicDepManager().getScope();
	
		if (scope == null)
			targetRef.addAll(ondemandHelper.getDynamicDepManager()
					.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
	
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		Map<String, TransactionContext> txs;
		depMgr = ondemandHelper.getDynamicDepManager();
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
									TranquillityOperationType.NOTIFY_FUTURE_ONDEMAND));
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
									TranquillityOperationType.NOTIFY_PAST_ONDEMAND));
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
		LOGGER.info("**** " + "confirmOndemandSetup(...) from " + parentComp);
		if (ondemandHelper.getDynamicDepManager().isValid()) {
			LOGGER.info("**** component status is valid, and return");
			return true;
		}

		// update current component's ConfirmOndemandStatus
		if (ConfirmOndemandStatus.containsKey(parentComp))
			ConfirmOndemandStatus.put(parentComp, true);
		else
			LOGGER.info("Illegal status while confirmOndemandSetup(...)");

		// print ConfirmOndemandStatus
		LOGGER.fine("ConfirmOndemandStatus:");
		for (Entry<String, Boolean> entry : ConfirmOndemandStatus.entrySet()) {
			LOGGER.fine("\t" + entry.getKey() + ": " + entry.getValue());
		}

		// isConfirmedAll?
		boolean isConfirmedAll = true;
		for (Entry<String, Boolean> entry : ConfirmOndemandStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}
		if (isConfirmedAll) {
			// change current componentStatus to 'valid'
			LOGGER.info("confirmOndemandSetup(...) from " + parentComp
					+ ", and confirmed All, trying to change mode to valid");
			ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
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
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;

		depMgr = ondemandHelper.getDynamicDepManager();
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
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;

		depMgr = ondemandHelper.getDynamicDepManager();
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
			scope.setTarget(targetSet);
			payload = TranquillityPayload.OPERATION_TYPE + ":"
					+ TranquillityOperationType.REQ_ONDEMAND_SETUP + ","
					+ TranquillityPayload.SRC_COMPONENT + ":" + hostComp + ","
					+ TranquillityPayload.TARGET_COMPONENT + ":" + parent + ","
					+ TranquillityPayload.SCOPE + ":" + scope.toString();
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
		scope = ondemandHelper.getDynamicDepManager().getScope();

		if (scope == null)
			targetRef.addAll(ondemandHelper.getDynamicDepManager()
					.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));

		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> futureC = new HashSet<String>();
		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;

		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();

		String rootTx = null;

		// read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getCurrentTx().equals(txID)) {
				if (ctx.getFutureComponents() != null
						|| ctx.getFutureComponents().size() != 0) {
					rootTx = ctx.getCurrentTx();
					for (String comp : ctx.getFutureComponents()) {
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
		scope = ondemandHelper.getDynamicDepManager().getScope();

		if (scope == null)
			targetRef.addAll(ondemandHelper.getDynamicDepManager()
					.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));

		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> pastC = new HashSet<String>();

		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;

		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();

		String rootTx = null;

		// read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getCurrentTx().equals(txID)) {
				if (ctx.getPastComponents() != null
						|| ctx.getPastComponents().size() != 0) {
					rootTx = ctx.getCurrentTx();
					for (String comp : ctx.getPastComponents()) {
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
		OndemandRequestStatus.clear();
		ConfirmOndemandStatus.clear();
	}

}
