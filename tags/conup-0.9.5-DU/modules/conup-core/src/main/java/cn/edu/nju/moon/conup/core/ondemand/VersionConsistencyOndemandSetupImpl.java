package cn.edu.nju.moon.conup.core.ondemand;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.impl.OndemandDynDepSetupServiceImpl;
import cn.edu.nju.moon.conup.core.algorithm.VersionConsistencyImpl;
import cn.edu.nju.moon.conup.core.utils.ConsistencyOndemandPayloadResolver;
import cn.edu.nju.moon.conup.core.utils.ConsistencyOperationType;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayload;
import cn.edu.nju.moon.conup.core.utils.ConsistencyPayloadCreator;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.PerformanceRecorder;
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
import cn.edu.nju.moon.conup.spi.utils.XMLUtil;


/**
 * 
 * The on-demand setup process of Version-consistency algorithm
 * @author nju
 *
 */
public class VersionConsistencyOndemandSetupImpl implements OndemandSetup {
	private final static Logger LOGGER = Logger.getLogger(VersionConsistencyOndemandSetupImpl.class.getName());
	
	/**
	 * components who send ondemand request to current component, when current component finish ondemand
	 * need to send confirm message to them(sub components)
	 * outer map's key is hostCompName
	 * inner map's key is subComponentName
	 */
	public static Map<String, Map<String, Boolean>> OndemandRequestStatus = new ConcurrentHashMap<String, Map<String, Boolean>>();
	/**
	 * components who depend on current component, when all parent components finish its ondemand
	 * they should send Confirm message to current component. 
	 * when current component receive all its parents' confirm message, it should change status from ondemand to valid
	 *  
	 * outer map's key is hostCompName
	 * inner map's key is parentComponentName
	 */
	public static Map<String, Map<String, Boolean>> ConfirmOndemandStatus = new ConcurrentHashMap<String, Map<String, Boolean>>();
	
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
		
		PerformanceRecorder.getInstance(hostComp).ondemandRqstReceived(System.nanoTime());
		
		DynamicDepManager ddm = ondemandHelper.getDynamicDepManager();
		if(ddm.getRuntimeInDeps().size() != 0){
			ddm.getRuntimeInDeps().clear();
		}
		if(ddm.getRuntimeDeps().size() != 0){
			ddm.getRuntimeDeps().clear();
		}
		assert scope != null;
		assert ddm.getRuntimeInDeps().size() == 0;
		assert ddm.getRuntimeDeps().size() == 0;
		
		return reqOndemandSetup(hostComp, hostComp);
	}
	
	@Override
	public boolean ondemandSetup(String srcComp, String proctocol, String payload) {
		ConsistencyOndemandPayloadResolver payloadResolver;
		ConsistencyOperationType  operation;
		String curComp;//current component
		
		payloadResolver = new ConsistencyOndemandPayloadResolver(payload);
		operation = payloadResolver.getOperation();
		curComp = payloadResolver.getParameter(ConsistencyPayload.TARGET_COMPONENT);
		if(operation.equals(ConsistencyOperationType.REQ_ONDEMAND_SETUP)){
			String scopeString = payloadResolver.getParameter(ConsistencyPayload.SCOPE);
			if(scopeString != null && !scopeString.equals("") && !scopeString.equals("null")){
				Scope scope = Scope.inverse(scopeString);
				ondemandHelper.getDynamicDepManager().setScope(scope);
			}
//			if(ondemandHelper.getDynamicDepManager().getScope() == null){
//				Scope scope = calcScope();
//				ondemandHelper.getDynamicDepManager().setScope(scope);
//			}
			reqOndemandSetup(curComp, srcComp);
		} else if(operation.equals(ConsistencyOperationType.CONFIRM_ONDEMAND_SETUP)){
			confirmOndemandSetup(srcComp, curComp);
		} else if(operation.equals(ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND)){
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					payloadResolver.getParameter(ConsistencyPayload.ROOT_TX), 
					srcComp, curComp, null, null);
			notifyFutureOndemand(dep);
		} else if(operation.equals(ConsistencyOperationType.NOTIFY_PAST_ONDEMAND)){
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP, 
					payloadResolver.getParameter(ConsistencyPayload.ROOT_TX), 
					srcComp, curComp, null, null);
			notifyPastOndemand(dep);
		} else if(operation.equals(ConsistencyOperationType.NOTIFY_SUB_FUTURE_ONDEMAND)){
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					payloadResolver.getParameter(ConsistencyPayload.ROOT_TX), 
					srcComp, curComp, null, null);
			notifySubFutureOndemand(dep);
		} else if(operation.equals(ConsistencyOperationType.NOTIFY_SUB_PAST_ONDEMAND)){
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP, 
					payloadResolver.getParameter(ConsistencyPayload.ROOT_TX), 
					srcComp, curComp, null, null);
			notifySubPastOndemand(dep);
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
		return VersionConsistencyImpl.ALGORITHM_TYPE;
	}
	
	/** 
	 * A reqOndemandSetup(...) is sent by current(host) component's sub-component
	 * If currentComponent.equals(requestSourceComponent), it means this is a 
	 * request from domain manager
	 * @param currentComp 
	 * @param current component's sub-component
	 * 
	 */
	private boolean reqOndemandSetup(String currentComp,
			String requestSrcComp) {
		//suspend all the threads that is initiated
		
		LOGGER.fine("**** in reqOndemandSetup(...):"+
			"\t" + "currentComponent=" + currentComp +
			"\t" + "requestSourceComponent=" + requestSrcComp);
		
		String hostComp = null;
		Set<String> targetRef = null;
		Set<String> parentComps = null;
		Scope scope;
		
		hostComp = currentComp;
		targetRef = new HashSet<String>();
		scope = ondemandHelper.getDynamicDepManager().getScope();
		
		//calculate target(sub) components
		if(scope == null)
			targetRef.addAll(ondemandHelper.getCompObject().getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
		
		//calculate parent components
		parentComps = ondemandHelper.getDynamicDepManager().getStaticInDeps();
		
		//init OndemandRequestStatus
		Map<String, Boolean> reqStatus;
		if(OndemandRequestStatus.containsKey(currentComp)){
			reqStatus = OndemandRequestStatus.get(currentComp);
		} else{
			reqStatus = new ConcurrentHashMap<String, Boolean>();
			OndemandRequestStatus.put(currentComp, reqStatus);
			
			for(String subComponent : targetRef){
				if(!reqStatus.containsKey(subComponent)){
					reqStatus.put(subComponent, false);
				}
			}
		}
		
		//init ConfirmOndemandStatus
		Map<String, Boolean> confirmStatus;
		if(ConfirmOndemandStatus.containsKey(currentComp)){
			confirmStatus = ConfirmOndemandStatus.get(currentComp);
		} else{
			confirmStatus = new ConcurrentHashMap<String, Boolean>();
			ConfirmOndemandStatus.put(currentComp, confirmStatus);
			
			for(String component : parentComps){
				if(!confirmStatus.containsKey(component)){
					confirmStatus.put(component, false);
				}
			}
		}
		
		//FOR TEST
		String targetRefs = new String();
		for(String component : targetRef){
			targetRefs += "\n\t" + component;
		}
		LOGGER.fine("**** " + hostComp + "'s targetRef:"
				+ targetRefs);
		String tmpParentComps = new String();
		for(String component : parentComps){
			tmpParentComps += "\n\t" + component;
		}
		LOGGER.fine("**** " + hostComp + "'s parents:"
				+ tmpParentComps);
		
		//wait for other reqOndemandSetup(...)
		receivedReqOndemandSetup(requestSrcComp, hostComp, parentComps);

		return true;
	}
	
	private boolean confirmOndemandSetup(String parentComp, 
			String currentComp) {
		LOGGER.fine("**** " + "confirmOndemandSetup(...) from " + parentComp);
		DynamicDepManager depMgr = ondemandHelper.getDynamicDepManager();
		if(depMgr.isValid()){
			LOGGER.fine("**** component status is valid, and return");
			return true;
		}
		
		// update current component's ConfirmOndemandStatus
		Map<String, Boolean> confirmStatus = ConfirmOndemandStatus.get(currentComp);
		assert confirmStatus != null;
		
		if(confirmStatus.containsKey(parentComp))
			confirmStatus.put(parentComp, true);
		else
			LOGGER.fine("Illegal status while confirmOndemandSetup(...)");
		
		//print ConfirmOndemandStatus
		String confirmOndemandStatusStr = "currentComp:" + currentComp + ",ConfirmOndemandStatus:";
		for(Entry<String, Boolean> entry : confirmStatus.entrySet()){
			confirmOndemandStatusStr += "\n\t" + entry.getKey() + ": " + entry.getValue();
		}
		LOGGER.fine(confirmOndemandStatusStr);
		
		//isConfirmedAll?
		boolean isConfirmedAll = true;
		synchronized (this) {
			for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
				isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
			}
			if (isConfirmedAll
					&& depMgr.getCompStatus().equals(CompStatus.ONDEMAND)) {
				// change current componentStatus to 'valid'
				LOGGER.fine("confirmOndemandSetup(...) from " + parentComp
						+ ", and confirmed All, trying to change mode to valid");
				ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
				// send confirmOndemandSetup(...)
				sendConfirmOndemandSetup(currentComp);
			}
		}
		
		return true;
	}

	private boolean onDemandSetUp() {
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		Map<String, TransactionContext> txs;
		Set<String> targetRef;
		Scope scope;
		depMgr = ondemandHelper.getDynamicDepManager();
		rtInDeps = depMgr.getRuntimeInDeps();
		rtOutDeps = depMgr.getRuntimeDeps();
		txs = depMgr.getTxs();
		
		LOGGER.fine("txs.size()=" + txs.size() + "\n" + txs);
		
		Set<Dependence> fDeps = new HashSet<Dependence>();
		Set<Dependence> pDeps = new HashSet<Dependence>();
		Set<Dependence> sDeps = new HashSet<Dependence>();
		String curComp = ondemandHelper.getCompObject().getIdentifier();
		
		targetRef = new HashSet<String>();
		scope = ondemandHelper.getDynamicDepManager().getScope();
		if(scope == null)
			targetRef.addAll(ondemandHelper.getDynamicDepManager().getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(curComp));
		
		Iterator<Entry<String, TransactionContext>> iterator = 
				txs.entrySet().iterator();
		while(iterator.hasNext()){
			TransactionContext txContext = 
					(TransactionContext)iterator.next().getValue();
			String rootTx = txContext.getRootTx();
			assert(txContext.getHostComponent().equals(curComp));
			String curTx = txContext.getCurrentTx();
			
			//in this case, it means current entry in interceptorCache is invalid
			if(rootTx == null || curComp == null){
				LOGGER.warning("Invalid data found while onDemandSetUp");
				continue;
			}
			
			// for the ended non-root txs, no need to create the lfe, lpe
			if(!rootTx.equals(curTx) && txContext.getEventType().equals(TxEventType.TransactionEnd)){
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
			
			//if current transaction is not a root transaction
			if(!rootTx.equals(curTx)){
				LOGGER.fine("current tx is " + curTx + ",and root is " + rootTx);
				continue;
			}
			
			if(txContext.isFakeTx())
				continue;
			
			//if current is root 
			LOGGER.fine("curTx + is a root tx");
			
			fDeps = getFDeps(curComp, rootTx);
			for(Dependence dep : fDeps){
				if( !targetRef.contains(dep.getTargetCompObjIdentifer())){
					continue;
				}
				dep.setType(VersionConsistencyImpl.FUTURE_DEP);
				dep.setRootTx(rootTx);
				
				String targetComp = dep.getTargetCompObjIdentifer();
				Map<String, TxEventType> subTxStatus = txContext.getSubTxStatuses();
				Map<String, String> subComps = txContext.getSubTxHostComps();
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
				boolean lastUseFlag = true;
				if(subFlag){
					boolean isLastuse = true;
					TxDepMonitor txDepMonitor = txContext.getTxDepMonitor();
					isLastuse = txDepMonitor.isLastUse(txContext.getCurrentTx(), targetComp, curComp);
					lastUseFlag = lastUseFlag && isLastuse;
					// at this moment, we can insure dep will not be used in the future, delete it from fDeps
					if(isLastuse)
						fDeps.remove(dep);
				} else{
					lastUseFlag = false;
				}
				
				
				// if subTx has already start, and also this time is not last access, we should create this future dep
				if(!rtOutDeps.contains(dep) && !lastUseFlag){
					//add to dependence registry
					rtOutDeps.add(dep);
					//notify future on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), 
							CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
							ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), 
									dep.getTargetCompObjIdentifer(), dep.getRootTx(), 
									ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND));
				}
			}
			
			pDeps = getPDeps(curComp, rootTx);
			for(Dependence dep : pDeps){
				if( !targetRef.contains(dep.getTargetCompObjIdentifer())){
					continue;
				}
				dep.setType(VersionConsistencyImpl.PAST_DEP);
				dep.setRootTx(rootTx);
				if(!rtOutDeps.contains(dep)){
					//add to dependence registry
					rtOutDeps.add(dep);
					//notify past on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), 
							CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
							ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), 
									dep.getTargetCompObjIdentifer(), dep.getRootTx(), 
									ConsistencyOperationType.NOTIFY_PAST_ONDEMAND));
				}
			}
			
			sDeps = getSDeps(curComp, rootTx);
			for(Dependence dep : sDeps){
				if( !targetRef.contains(dep.getTargetCompObjIdentifer())){
					continue;
				}
				dep.setRootTx(rootTx);
				dep.setType(VersionConsistencyImpl.FUTURE_DEP);
				if(!fDeps.contains(dep)){
					//notify sub future on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), 
							CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
							ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), 
									dep.getTargetCompObjIdentifer(), dep.getRootTx(), 
									ConsistencyOperationType.NOTIFY_SUB_FUTURE_ONDEMAND));
				}
				
				dep.setType(VersionConsistencyImpl.PAST_DEP);
				if(!pDeps.contains(dep)){
					//notify sub past on-demand
					OndemandDynDepSetupServiceImpl ondemandComm;
					ondemandComm = new OndemandDynDepSetupServiceImpl();
					ondemandComm.synPost(curComp, dep.getTargetCompObjIdentifer(), 
							CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
							ConsistencyPayloadCreator.createPayload(dep.getSrcCompObjIdentifier(), 
									dep.getTargetCompObjIdentifer(), dep.getRootTx(), 
									ConsistencyOperationType.NOTIFY_SUB_PAST_ONDEMAND));
				}
				
			}
			
		}//END WHILE
		
		return true;
	}

	/**
	 * 
	 * @param dep a dependence that another component points to current component
	 * @return
	 */
	private boolean notifyFutureOndemand(Dependence dep) {
		LOGGER.fine("notifyFutureOndemand(Dependence dep) with " + dep.toString());
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		
		String curComp;
		String rootTx;
		Set<String> targetRef;
		Scope scope;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		rtInDeps = depMgr.getRuntimeInDeps();
		rtOutDeps = depMgr.getRuntimeDeps();
		
		curComp = dep.getTargetCompObjIdentifer();
		rootTx = dep.getRootTx();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();;
		
		if(scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(curComp));
		
		//add to in dependence registry
		rtInDeps.add(dep);
		
		for(String subComp : targetRef){
			Dependence futureDep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					rootTx, curComp, subComp, null, null);
			if(!rtOutDeps.contains(futureDep)){
				rtOutDeps.add(futureDep);
				//notify future on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, subComp, 
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, subComp, rootTx, 
								ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND));
			}
		}//END FOR
		
		return true;
	}

	/**
	 * 
	 * @param dep a dependence that another component depends on current component
	 * @return
	 */
	private boolean notifyPastOndemand(Dependence dep) {
		LOGGER.fine("notifyPastOndemand(Dependence dep) with " + dep.toString());
		DynamicDepManager depMgr;
		Set<Dependence> rtInDeps;
		Set<Dependence> rtOutDeps;
		
		String curComp;
		String rootTx;
		Set<String> targetRef;
		Scope scope;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		rtInDeps = depMgr.getRuntimeInDeps();
		rtOutDeps = depMgr.getRuntimeDeps();
		
		curComp = dep.getTargetCompObjIdentifer();
		rootTx = dep.getRootTx();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
		
		if(scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(curComp));
		
		rtInDeps.add(dep);
		
		for(String subComp : targetRef){
			Dependence pastDep = new Dependence(VersionConsistencyImpl.PAST_DEP, 
					rootTx, curComp, subComp, null, null);
			if(!rtOutDeps.contains(pastDep)){
				rtOutDeps.add(pastDep);
				//notify past on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, subComp, 
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, subComp, rootTx, 
								ConsistencyOperationType.NOTIFY_PAST_ONDEMAND));
			}
		}//END FOR
		
		return true;
	}

	/**
	 * 
	 * @param dep a dependence that another component depends on current component
	 * @return
	 */
	private boolean notifySubFutureOndemand(Dependence dep) {
		LOGGER.fine("notifySubFutureOndemand(Dependence dep) with " + dep.toString());
		DynamicDepManager depMgr;
		Set<Dependence> rtOutDeps;
		
		String curComp;
		String rootTx;
		Set<String> targetRef;
		Scope scope;
		String subTx = null;
		Set<Dependence> fDeps = new HashSet<Dependence>();
		Set<Dependence> sDeps = new HashSet<Dependence>();
		
		depMgr = ondemandHelper.getDynamicDepManager();
		rtOutDeps = depMgr.getRuntimeDeps();
		
		curComp = dep.getTargetCompObjIdentifer();
		rootTx = dep.getRootTx();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
		subTx = getHostSubTx(rootTx);
		
		if(scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(curComp));
		
		fDeps = getFDeps(curComp, subTx);
		
		LOGGER.fine("fDeps:" + fDeps);
		for(Dependence ose : fDeps){
			if( !targetRef.contains(ose.getTargetCompObjIdentifer())){
				continue;
			}
			Dependence futureDep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					rootTx, curComp, ose.getTargetCompObjIdentifer(), null, null);
			if(!rtOutDeps.contains(futureDep)){
				rtOutDeps.add(futureDep);
				//notify future on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, ose.getTargetCompObjIdentifer(), 
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, ose.getTargetCompObjIdentifer(), rootTx, 
								ConsistencyOperationType.NOTIFY_FUTURE_ONDEMAND));
				
			}
		}
		
		sDeps = getSDeps(curComp, subTx);
		LOGGER.fine("sDeps:" + sDeps);
		for(Dependence ose : sDeps){
			if( !targetRef.contains(ose.getTargetCompObjIdentifer())){
				continue;
			}
			assert ose.getRootTx() != null;
			if(!fDeps.contains(ose)){
				//notify sub future on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, ose.getTargetCompObjIdentifer(),
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, ose.getTargetCompObjIdentifer(), rootTx, 
								ConsistencyOperationType.NOTIFY_SUB_FUTURE_ONDEMAND));
			}
		}
		
		return true;
	}

	private boolean notifySubPastOndemand(Dependence dep) {
		LOGGER.fine("notifySubPastOndemand(Dependence dep) with " + dep.toString());
		DynamicDepManager depMgr;
		Set<Dependence> rtOutDeps;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		rtOutDeps = depMgr.getRuntimeDeps();
		
		String curComp;
		String rootTx;
		Set<String> targetRef;
		Scope scope;
		String subTx = null;
		Set<Dependence> pDeps = new HashSet<Dependence>();
		Set<Dependence> sDeps = new HashSet<Dependence>();
		
		curComp = dep.getTargetCompObjIdentifer();
		rootTx = dep.getRootTx();
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
		subTx = getHostSubTx(rootTx);
		
		if(scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(curComp));
		
		pDeps = getPDeps(curComp, subTx);
		
		for(Dependence ose : pDeps){
			if( !targetRef.contains(ose.getTargetCompObjIdentifer())){
				continue;
			}
			Dependence pastDep = new Dependence(VersionConsistencyImpl.PAST_DEP, 
					rootTx, curComp, ose.getTargetCompObjIdentifer(), null, null);
			if(!rtOutDeps.contains(pastDep)){
				rtOutDeps.add(pastDep);
				//notify future on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, ose.getTargetCompObjIdentifer(), 
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, ose.getTargetCompObjIdentifer(), rootTx, 
								ConsistencyOperationType.NOTIFY_PAST_ONDEMAND));
				
			}
		}
		
		sDeps = getSDeps(curComp, subTx);
		for(Dependence ose : sDeps){
			if( !targetRef.contains(ose.getTargetCompObjIdentifer())){
				continue;
			}
			
			assert ose.getRootTx() != null;
			if(!pDeps.contains(ose)){
				//notify sub future on-demand
				OndemandDynDepSetupServiceImpl ondemandComm;
				ondemandComm = new OndemandDynDepSetupServiceImpl();
				ondemandComm.synPost(curComp, ose.getTargetCompObjIdentifer(),
						CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, 
						ConsistencyPayloadCreator.createPayload(curComp, ose.getTargetCompObjIdentifer(), rootTx, 
								ConsistencyOperationType.NOTIFY_SUB_PAST_ONDEMAND));
			}
		}

		return true;
	}
	
	private void receivedReqOndemandSetup(
			String requestSrcComp, String currentComp, Set<String> parentComponents){
		// update current component's OndemandRequestStatus
		Map<String, Boolean> reqStatus = OndemandRequestStatus.get(currentComp);
		if(reqStatus.containsKey(requestSrcComp))
			reqStatus.put(requestSrcComp, true);
		else
			LOGGER.fine("OndemandRequestStatus doesn't contain " + requestSrcComp);
		
		//print OndemandRequestStatus
		String ondemandRequestStatusStr = "currentComp: " + currentComp + ", OndemandRequestStatus:";
		for(Entry<String, Boolean> entry : reqStatus.entrySet()){
			ondemandRequestStatusStr += "\n\t" + entry.getKey() + ": " + entry.getValue();
		}
		LOGGER.fine(ondemandRequestStatusStr);

		/*
		 * To judge whether current component has received reqOndemandSetup(...)
		 * from every in-scope outgoing static edge
		 */
		boolean isReceivedAll = true;
		for (Entry<String, Boolean> entry : reqStatus.entrySet()) {
			isReceivedAll = isReceivedAll && (Boolean) entry.getValue();
		}
		synchronized (this) {
			// if received all
			if (isReceivedAll && ondemandHelper.getDynamicDepManager().getCompStatus().equals(CompStatus.NORMAL)) {
				LOGGER.fine("Received reqOndemandSetup(...) from "
						+ requestSrcComp);
				LOGGER.fine("Received all reqOndemandSetup(...)");
				LOGGER.fine("trying to change mode to ondemand");

				DynamicDepManager ddm = ondemandHelper.getDynamicDepManager();
				if (ddm.getRuntimeInDeps().size() != 0) {
					ddm.getRuntimeInDeps().clear();
				}
				if (ddm.getRuntimeDeps().size() != 0) {
					ddm.getRuntimeDeps().clear();
				}
				assert ddm.getRuntimeInDeps().size() == 0;
				assert ddm.getRuntimeDeps().size() == 0;

				// change current componentStatus to 'ondemand'
				ondemandHelper.getDynamicDepManager().ondemandSetting();
				// send reqOndemandSetup(...) to parent components
				sendReqOndemandSetup(parentComponents, currentComp);
				// onDemandSetUp
				Object ondemandSyncMonitor = ondemandHelper
						.getDynamicDepManager().getOndemandSyncMonitor();
				synchronized (ondemandSyncMonitor) {
					if (ondemandHelper.getDynamicDepManager().getCompStatus()
							.equals(CompStatus.ONDEMAND)) {
						// FOR TEST
						Map<String, TransactionContext> allTxs = ondemandHelper
								.getDynamicDepManager().getTxs();
						Iterator<Entry<String, TransactionContext>> txIterator = allTxs
								.entrySet().iterator();
						String txStr = "";
						while (txIterator.hasNext()) {
							TransactionContext txCtx = txIterator.next()
									.getValue();
							txStr += txCtx.toString() + "\n";
						}
						LOGGER.fine("TxRegistry:\n" + txStr);

						LOGGER.fine("synchronizing for method onDemandSetUp() in VersionConsistencyOndemandSetupImpl..");
						onDemandSetUp();
					}
				}
				// isConfirmedAll?
				boolean isConfirmedAll = true;
				Map<String, Boolean> confirmStatus = ConfirmOndemandStatus
						.get(currentComp);
				if (confirmStatus == null) {
					System.out.println("currentComp: " + currentComp + ", requestSrcComp:" + requestSrcComp + ", compStatus:"
							+ ondemandHelper.getDynamicDepManager().getCompStatus() + ", confirmStatus:" + confirmStatus);
				}
				assert confirmStatus != null;
				for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
					isConfirmedAll = isConfirmedAll	&& (Boolean) entry.getValue();
				}

				// print ConfirmOndemandStatus
				String confirmOndemandStatusStr = "currentComp:" + currentComp
						+ " confirmOndemandStatusStr:";
				for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
					confirmOndemandStatusStr += "\t" + entry.getKey() + ": "
							+ entry.getValue();
				}
				LOGGER.fine(confirmOndemandStatusStr);

				if (isConfirmedAll) {
					if (ondemandHelper.getDynamicDepManager().isValid()) {
						LOGGER.fine("Confirmed all, and component status is valid");
						return;
					}
					LOGGER.fine("Confirmed from all parent components in receivedReqOndemandSetup(...)");
					LOGGER.fine("trying to change mode to valid");

					// change current componentStatus to 'valid'
					ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
					// send confirmOndemandSetup(...)
					sendConfirmOndemandSetup(currentComp);
				}
			}// END IF
		}

	}
	
	private boolean sendReqOndemandSetup(
			Set<String> parentComps, String hostComp){
		
		// FOR TEST
		LOGGER.fine("current compStatus=ondemand, before send req ondemand to parent component.");
		
		String str = "currentComp:" + hostComp + ", sendReqOndemandSetup(...) to parent components:";
		for(String component : parentComps){
			str += "\n\t" + component;
		}
		LOGGER.fine(str);
		
		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;

		for(String parent : parentComps){
			payload = ConsistencyPayload.OPERATION_TYPE + ":" + ConsistencyOperationType.REQ_ONDEMAND_SETUP + "," +
					ConsistencyPayload.SRC_COMPONENT + ":" + hostComp + "," +
					ConsistencyPayload.TARGET_COMPONENT + ":" + parent + "," +
					ConsistencyPayload.SCOPE + ":" + ondemandHelper.getDynamicDepManager().getScope().toString();
			ondemandComm.asynPost(hostComp, parent, CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, payload);
		}
		
		return true;
	}
	
	private void sendConfirmOndemandSetup(String hostComp){
		Set<String> targetRef;
		Scope scope;
		
		targetRef = new HashSet<String>();
		scope = ondemandHelper.getDynamicDepManager().getScope();
		
		if(scope == null)
			targetRef.addAll(ondemandHelper.getDynamicDepManager().getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
		
		String str = "sendConfirmOndemandSetup(...) to sub components:";
//		LOGGER.fine("sendConfirmOndemandSetup(...) to sub components:");
		for(String component : targetRef){
			str += "\n\t" + component;
		}
		LOGGER.fine(str);
		
		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;
		for(String subComp : targetRef){
			payload = ConsistencyPayload.OPERATION_TYPE + ":" + ConsistencyOperationType.CONFIRM_ONDEMAND_SETUP + "," +
					ConsistencyPayload.SRC_COMPONENT + ":" + hostComp + "," +
					ConsistencyPayload.TARGET_COMPONENT + ":" + subComp;
			ondemandComm.asynPost(hostComp, subComp, CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, payload);
		}
		
		//ondemand setup is done
		isOndemandDone = true;
		
		//print dependences and txs
//		Printer printer = new Printer();
//		printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeDeps(), "outDeps");
//		printer.printDeps(ondemandHelper.getDynamicDepManager().getRuntimeInDeps(), "inDeps");
//		printer.printTxs(ondemandHelper.getDynamicDepManager().getTxs());

	}
	
	/**
	 * 
	 * @param curComp
	 * @param txID transaction ID (ATTENTION: txID maybe a sub-tx)
	 * @return
	 */
	private Set<Dependence> getFDeps(String curComp, String txID){
		Set<Dependence> result = new ConcurrentSkipListSet<Dependence>();
		Set<String> futureC = new HashSet<String>();
		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();
		
		String rootTx = null;
		
		if(txID != null){
			//read transaction dependencies from TransactionRegistry
			Iterator<Entry<String, TransactionContext>> txIterator;
			txIterator = txs.entrySet().iterator();
			TransactionContext ctx;
			while (txIterator.hasNext()) {
				ctx = txIterator.next().getValue();
				if (ctx.getRootTx().equals(txID) 
						|| ctx.getCurrentTx().equals(txID)) {
					if(ctx.getFutureComponents() != null
							|| ctx.getFutureComponents().size()!=0){
						rootTx = ctx.getRootTx();
						futureC.addAll(ctx.getFutureComponents());
//					break;
					}
				}
			}// END WHILE
		} else{	// the txID has not started on local component
			LOGGER.fine("no local subTx running...");
			Scope scope = depMgr.getScope();
			if(scope == null){
				futureC.addAll(depMgr.getStaticDeps());
			} else{
				futureC.addAll(scope.getSubComponents(curComp));
			}
		}
		
		for(String component : futureC){
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					rootTx, curComp, component, null, null);
			result.add(dep);
		}//END FOR
		
		//FOR TEST
//		LOGGER.fine("In getFDeps(...), size=" + result.size());
		StringBuffer strBuffer = new StringBuffer();
		for(Dependence dep : result){
			strBuffer.append("\n" + dep.toString());
		}
		LOGGER.fine("In getFDeps(...), size=" + result.size() + ", for root=" + rootTx + strBuffer.toString());
		
		return result;
	}
	
	private Set<Dependence> getPDeps(String curComp, String txID){
		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> pastC = new HashSet<String>();
		
		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();
		
		String rootTx = null;
		
		//read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getRootTx().equals(txID) 
				|| ctx.getCurrentTx().equals(txID)) {
					if(ctx.getPastComponents() != null
						|| ctx.getPastComponents().size()!=0){
						rootTx = ctx.getRootTx();
						pastC.addAll(ctx.getPastComponents());
//						break;
					}
//				}
			}
		}// END WHILE
		
		for(String component : pastC){
			Dependence dep = new Dependence(VersionConsistencyImpl.PAST_DEP, rootTx, curComp, component, null, null);
			result.add(dep);
		}//END FOR
		
		//FOR TEST
		
//		LOGGER.fine("In getPArcs(...), size=" + result.size());
		
		StringBuffer strBuffer = new StringBuffer();
		for(Dependence dep : result){
			strBuffer.append("\n" + dep.toString());
		}
		LOGGER.fine("In getPDeps(...), size=" + result.size() + ", for root=" + rootTx + strBuffer.toString());
		
		return result;
	}
	
	private Set<Dependence> getSDeps(String hostComponent, String transactionID){
		Set<Dependence> result = new HashSet<Dependence>();
		Set<String> ongoingC = new HashSet<String>();
		
		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();
		
		String rootTx = null;
		
		//read transaction dependencies from TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		Iterator<Entry<String, TxEventType>> subTxStatusIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		while (txIterator.hasNext()) {
			ctx = txIterator.next().getValue();
			if (ctx.getRootTx().equals(transactionID) 
				|| ctx.getCurrentTx().equals(transactionID)) {
				rootTx = ctx.getRootTx();
				if(ctx.getSubTxHostComps() == null){
					continue;
				}
				subTxStatusIterator = ctx.getSubTxStatuses().entrySet().iterator();
				Entry<String, TxEventType> subTxStatusEntry;
				TxEventType subTxStatus;
				String subTxID;
				while(subTxStatusIterator.hasNext()){
					subTxStatusEntry = subTxStatusIterator.next();
					subTxID = subTxStatusEntry.getKey();
					subTxStatus = subTxStatusEntry.getValue();
					//TODO i'm not quite sure whether the following is correct or not
					if(!subTxStatus.equals(TxEventType.TransactionEnd)){
						ongoingC.add(ctx.getSubTxHostComps().get(subTxID));
					}
				}//END WHILE
				
			}//END IF
		}// END WHILE
		
		for(String component : ongoingC){
			Dependence dep = new Dependence(VersionConsistencyImpl.FUTURE_DEP, 
					rootTx, hostComponent, component, null, null);
			result.add(dep);
		}//END FOR
		
		//FOR TEST
		StringBuffer strBuffer = new StringBuffer();
		for(Dependence dep : result){
			strBuffer.append("\n" + dep.toString());
		}
		LOGGER.fine("In getSDeps(...), size=" + result.size() + ", for root=" + rootTx + strBuffer.toString());
		
		return result;
	}
	
	private String getHostSubTx(String rootTx){
		DynamicDepManager depMgr;
		Map<String, TransactionContext> txs;
		
		depMgr = ondemandHelper.getDynamicDepManager();
		txs = depMgr.getTxs();
		
//		Printer printer = new Printer();
//		LOGGER.info("Txs before getHostSubTx for rootTx: " + rootTx);
//		printer.printTxs(LOGGER, txs);
		
		String subTx = null;
		
		//query TransactionRegistry
		Iterator<Entry<String, TransactionContext>> txIterator;
		txIterator = txs.entrySet().iterator();
		TransactionContext ctx;
		String currentTx = null;
		Entry<String, TransactionContext> entry;
		while (txIterator.hasNext()) {
			entry = txIterator.next();
			currentTx = entry.getKey();
			ctx = entry.getValue();

			if(ctx.isFakeTx())
				continue;
			
			assert(ctx.getCurrentTx().equals(currentTx));
			if (ctx.getRootTx().equals(rootTx) 
				&& !ctx.getEventType().equals(TxEventType.TransactionEnd)) {
				subTx = currentTx;
			}
		}// END WHILE
		
		LOGGER.fine("getHostSubTransaction(" + rootTx + ")=" + subTx);
		return subTx;
	}
	
	/**
	 * calc all involved components with target component 
	 * @return
	 */
	public Scope calcScope(){
		Scope scope = new Scope();
		
		XMLUtil xmlUtil = new XMLUtil();
		String compIdentifier = ondemandHelper.getCompObject().getIdentifier();
		
//		Set<String> scopeComps = xmlUtil.getParents(compIdentifier);
		Set<String> scopeComps = new HashSet<String>();
		
		Queue<String>  queue= new LinkedBlockingQueue<String>();
		queue.add(compIdentifier);

		while(!queue.isEmpty()){
			String compInQueue = queue.poll();
			Set<String> parents = xmlUtil.getParents(compInQueue);
			queue.addAll(parents);
			scopeComps.addAll(parents);
		}
		scopeComps.add(compIdentifier);
		
		for (Iterator<String> iterator = scopeComps.iterator(); iterator.hasNext();) {
			String compName = (String) iterator.next();
			Set<String> subs = xmlUtil.getChildren(compName);
			for (String string : subs) {
				if(!scopeComps.contains(string))
					subs.remove(string);
			}
			scope.addComponent(compName, xmlUtil.getParents(compName), subs);
			
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
		PerformanceRecorder.getInstance(hostComp).ondemandIsDone(System.nanoTime());
//		OndemandRequestStatus.clear();
//		ConfirmOndemandStatus.clear();
	}
	
}
