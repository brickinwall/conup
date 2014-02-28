package cn.edu.nju.moon.conup.core.ondemand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.comm.api.peer.services.impl.OndemandDynDepSetupServiceImpl;
import cn.edu.nju.moon.conup.core.algorithm.QuiescenceImpl;
import cn.edu.nju.moon.conup.spi.datamodel.CommProtocol;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.MsgType;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetup;
import cn.edu.nju.moon.conup.spi.helper.OndemandSetupHelper;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.DepPayloadResolver;
import cn.edu.nju.moon.conup.spi.utils.DepPayload;
import cn.edu.nju.moon.conup.spi.utils.XMLUtil;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class QuiescenceOndemandSetupImpl implements OndemandSetup {
	private Logger LOGGER = Logger.getLogger(QuiescenceOndemandSetupImpl.class.getName());
	private OndemandSetupHelper ondemandHelper;
	
	private CompLifeCycleManager compLifeCycleMgr = null;
	private DynamicDepManager depMgr = null;

	/**
	 * components who send ondemand request to current component, when current component finish ondemand
	 * need to send confirm message to them(sub components)
	 * outer map's key is hostCompName
	 * inner map's key is subComponentName
	 */
	public static Map<String, Map<String, Boolean>> OndemandRequestStatus = new HashMap<String, Map<String, Boolean>>();
	/**
	 * components who depend on current component, when all parent components finish its ondemand
	 * they should send Confirm message to current component. 
	 * when current component receive all its parents' confirm message, it should change status from ondemand to valid
	 *  
	 * outer map's key is hostCompName
	 * inner map's key is parentComponentName
	 */
	public static Map<String, Map<String, Boolean>> ConfirmOndemandStatus = new HashMap<String, Map<String, Boolean>>();
	
	private boolean isOndemandDone = false;
	
	@Override
	public boolean ondemand(Scope scope) {
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
//		Scope scope = calcScope();
		if(scope == null) {
			scope = calcScope();
		}
		depMgr.setScope(scope);
		
//		DynamicDepManager ddm = ondemandHelper.getDynamicDepManager();
		assert scope != null;
		
		return reqOndemandSetup(hostComp, hostComp);
	}
	
	@Override
	public boolean ondemandSetup(String srcComp, String proctocol, String payload) {
		DepPayloadResolver payloadResolver;
		DepOperationType  operation;
		String curComp;//current component
		
		payloadResolver = new DepPayloadResolver(payload);
		operation = payloadResolver.getOperation();
		curComp = payloadResolver.getParameter(DepPayload.TARGET_COMPONENT);
		if(operation.equals(DepOperationType.REQ_ONDEMAND_SETUP)){
			String scopeString = payloadResolver.getParameter(DepPayload.SCOPE);
			if(scopeString != null && !scopeString.equals("") && !scopeString.equals("null")){
				Scope scope = Scope.inverse(scopeString);
				depMgr.setScope(scope);
			}
			reqOndemandSetup(curComp, srcComp);
		} else if(operation.equals(DepOperationType.CONFIRM_ONDEMAND_SETUP)){
			confirmOndemandSetup(srcComp, curComp);
		}
		
		return true;
	}

	public void setOndemandHelper(OndemandSetupHelper ondemandHelper) {
		this.ondemandHelper = ondemandHelper;
	}

	@Deprecated
	public boolean isOndemandDone() {
		return isOndemandDone;
	}

	@Override
	public void onDemandIsDone() {
		String hostComp = ondemandHelper.getCompObject().getIdentifier();
		OndemandRequestStatus.remove(hostComp);
		ConfirmOndemandStatus.remove(hostComp);
		// OndemandRequestStatus.clear();
		// ConfirmOndemandStatus.clear();
	}

	@Override
	public String getAlgorithmType() {
		return QuiescenceImpl.ALGORITHM_TYPE;
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
		scope = depMgr.getScope();
		
		//calculate target(sub) components
		if(scope == null)
			targetRef.addAll(ondemandHelper.getCompObject().getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
		
		//calculate parent components
		parentComps = depMgr.getStaticInDeps();
		
		//init OndemandRequestStatus
		Map<String, Boolean> reqStatus;
		if(OndemandRequestStatus.containsKey(currentComp)){
			reqStatus = OndemandRequestStatus.get(currentComp);
		} else{
			reqStatus = new HashMap<String, Boolean>();
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
			confirmStatus = new HashMap<String, Boolean>();
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
		if(compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)){
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
		String confirmOndemandStatusStr = "ConfirmOndemandStatus:";
		for(Entry<String, Boolean> entry : confirmStatus.entrySet()){
			confirmOndemandStatusStr += "\n\t" + entry.getKey() + ": " + entry.getValue();
		}
		LOGGER.fine(confirmOndemandStatusStr);
		
		//isConfirmedAll?
		boolean isConfirmedAll = true;
		for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
			isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
		}
		if(isConfirmedAll){
			//change current componentStatus to 'valid'
			LOGGER.fine("confirmOndemandSetup(...) from " + parentComp + 
					", and confirmed All, trying to change mode to valid");
			//TODO
//			ondemandHelper.getDynamicDepManager().ondemandSetupIsDone();
			
//			compLifeCycleMgr.ondemandSetupIsDone();
			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
			updateMgr.ondemandSetupIsDone();
			//send confirmOndemandSetup(...)
			sendConfirmOndemandSetup(currentComp);
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
		String ondemandRequestStatusStr = "OndemandRequestStatus:";
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

		// if received all
		if (isReceivedAll) {
			LOGGER.fine("Received reqOndemandSetup(...) from " + requestSrcComp);
			LOGGER.fine("Received all reqOndemandSetup(...)");
			LOGGER.fine("trying to change mode to ondemand");
			
			//change current componentStatus to 'ondemand'
//			compLifeCycleMgr.ondemandSetting();
			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
			updateMgr.ondemandSetting();
			//send reqOndemandSetup(...) to parent components
			sendReqOndemandSetup(parentComponents, currentComp);
			//onDemandSetUp
			Object ondemandSyncMonitor = compLifeCycleMgr.getCompObject().getOndemandSyncMonitor();
			synchronized (ondemandSyncMonitor) {
				if(compLifeCycleMgr.getCompStatus().equals(CompStatus.ONDEMAND)){
					//FOR TEST
					Map<String, TransactionContext> allTxs = depMgr.getTxs();
					Iterator<Entry<String, TransactionContext>> txIterator = allTxs.entrySet().iterator();
					String txStr = "";
					while(txIterator.hasNext()){
						TransactionContext txCtx = txIterator.next().getValue();
						txStr += txCtx.toString() + "\n";
					}
					LOGGER.fine("TxRegistry:\n" + txStr);
					
					LOGGER.fine("synchronizing for method onDemandSetUp() in VersionConsistencyOndemandSetupImpl..");
					onDemandSetUp();
				}
			}
			//isConfirmedAll?
			boolean isConfirmedAll = true;
			Map<String, Boolean> confirmStatus = ConfirmOndemandStatus.get(currentComp);
			for (Entry<String, Boolean> entry : confirmStatus.entrySet()) {
				isConfirmedAll = isConfirmedAll && (Boolean) entry.getValue();
			}
			
			//print ConfirmOndemandStatus
			String confirmOndemandStatusStr = "";
			for(Entry<String, Boolean> entry : confirmStatus.entrySet()){
				confirmOndemandStatusStr += "\t" + entry.getKey() + ": " + entry.getValue();
//				LOGGER.fine("\t" + entry.getKey() + ": " + entry.getValue());
			}
			LOGGER.fine(confirmOndemandStatusStr);
			
			if(isConfirmedAll){
				if(compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)){
					LOGGER.fine("Confirmed all, and component status is valid");
					return;
				}
				LOGGER.fine("Confirmed from all parent components in receivedReqOndemandSetup(...)");
				LOGGER.fine("trying to change mode to valid");
				
				//change current componentStatus to 'valid'
//				compLifeCycleMgr.ondemandSetupIsDone();
//				UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(currentComp);
				updateMgr.ondemandSetupIsDone();
				//send confirmOndemandSetup(...)
				sendConfirmOndemandSetup(currentComp);
			}
		}//END IF

	}
	
	private boolean sendReqOndemandSetup(
			Set<String> parentComps, String hostComp){
		
		// FOR TEST
		LOGGER.fine("current compStatus=ondemand, before send req ondemand to parent component.");
		
		String str = "sendReqOndemandSetup(...) to parent components:";
		for(String component : parentComps){
			str += "\n\t" + component;
		}
		LOGGER.fine(str);
		
		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;

		for(String parent : parentComps){
			payload = DepPayload.OPERATION_TYPE + ":" + DepOperationType.REQ_ONDEMAND_SETUP + "," +
					DepPayload.SRC_COMPONENT + ":" + hostComp + "," +
					DepPayload.TARGET_COMPONENT + ":" + parent + "," +
					DepPayload.SCOPE + ":" + depMgr.getScope().toString();
			ondemandComm.asynPost(hostComp, parent, CommProtocol.CONSISTENCY, MsgType.ONDEMAND_MSG, payload);
		}
		
		return true;
	}
	
	private void sendConfirmOndemandSetup(String hostComp){
		Set<String> targetRef;
		Scope scope;
		
		targetRef = new HashSet<String>();
		scope = depMgr.getScope();
		
		if(scope == null)
			targetRef.addAll(depMgr.getStaticDeps());
		else
			targetRef.addAll(scope.getSubComponents(hostComp));
		
		String str = "sendConfirmOndemandSetup(...) to sub components:";
		for(String component : targetRef){
			str += "\n\t" + component;
		}
		LOGGER.fine(str);
		
		OndemandDynDepSetupServiceImpl ondemandComm;
		ondemandComm = new OndemandDynDepSetupServiceImpl();
		String payload;
		for(String subComp : targetRef){
			payload = DepPayload.OPERATION_TYPE + ":" + DepOperationType.CONFIRM_ONDEMAND_SETUP + "," +
					DepPayload.SRC_COMPONENT + ":" + hostComp + "," +
					DepPayload.TARGET_COMPONENT + ":" + subComp;
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
	
	private Scope calcScope(){
		Scope scope = new Scope();
		
		XMLUtil xmlUtil = new XMLUtil();
		String compIdentifier = ondemandHelper.getCompObject().getIdentifier();
		
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
	
	private boolean onDemandSetUp(){
		return true;
	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		// TODO Auto-generated method stub
		
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
