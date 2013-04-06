/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.runtime.DomainRegistry;

import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.datamodel.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.utils.ExecutionRecorder;

/**
 * It's used to monitor transaction status, maintain transaction context 
 * and possibly invoke related algorithm
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TxDepMonitorImpl implements TxDepMonitor {
	private Logger LOGGER = Logger.getLogger(TxDepMonitorImpl.class.getName());
	/** used to store the mapping between service name and component name*/
	private static Map<String, String> serviceToComp = new ConcurrentHashMap<String, String>();
	
	/**
	 * 
	 * @param TxEventType 
	 * @param curTxID current tx id
	 * @return
	 */
	public boolean notify(TxEventType et, String curTxID){
		LOGGER.fine("--------TxDepMonitor.notify(" + et.toString() + "," + curTxID + ")--------------");
		/*
		 * set eventType, futureC, pastC 
		 */
		Map<String, TransactionContext> TX_IDS = TxLifecycleManager.TX_IDS;
		LocalDynamicDependencesManager ddm = LocalDynamicDependencesManager.getInstance(curTxID);
		TransactionContext txContext = TX_IDS.get(curTxID);
		txContext.setEventType(et);
		
		txContext.setFutureComponents(convertServiceToComponent(ddm.getFuture(), txContext.getHostComponent()));
		txContext.setPastComponents(convertServiceToComponent(ddm.getPast(), txContext.getHostComponent()));
		txContext.setTxDepMonitor(this);
		/*
		 * use componentIdentifier to get specific DynamicDepManager
		 */
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(txContext.getHostComponent());
		
		//save a TxDepMonitor into DynamicDepManager
		if(dynamicDepMgr.getTxDepMonitor() == null){
			dynamicDepMgr.setTxDepMonitor(this);
		}
		
		boolean result = dynamicDepMgr.manageTx(txContext);
		// when be notified that a tx ends, remove it from TX_IDS.
		if(et.equals(TxEventType.TransactionEnd)){
//			TX_IDS.remove(txContext.getHostComponent());
			TX_IDS.remove(curTxID);
			
//			InterceptorCache interceptorCache = InterceptorCache.getInstance(txContext.getHostComponent());
//			interceptorCache.removeTxCtx(getThreadID());
			
			CompLifecycleManager compLcMgr;
			compLcMgr = CompLifecycleManager.getInstance(txContext.getHostComponent());
			
			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
					&& compLcMgr.isDynamicUpdateRqstRCVD()){
				compLcMgr.attemptToUpdate();
			}
		}
		return result; 
		
	}
	
	/**
	 * @param txStatus transaction status, i.e., start, running, end
	 * @param curTxID current tx id
	 * @param rootTxID root tx id
	 * @param rootComp root tx's host component name
	 * @param parentTxID parent tx id
	 * @param parentComp parent tx's host component name
	 * @param futureRef references that will be used in future
	 * @param pastRef references that have been used
	 * @return
	 */
	@Deprecated
	public boolean notify(String txStatus, String curTxID, String rootTxID, String rootComp, 
			String parentTxID, String parentComp, Set<String> futureRef, Set<String> pastRef){
		
		return true;
	}
	
	@Override
	public boolean isLastUse(String txID, String targetCompIdentifier, String hostComp) {
		LocalDynamicDependencesManager lddm;
		lddm = LocalDynamicDependencesManager.getInstance(txID);
		Set<String> fservices = lddm.getFuture();
		Set<String> tmpFutureServices = new ConcurrentSkipListSet<String>();
		tmpFutureServices.addAll(fservices);
		for(String fs : tmpFutureServices){
			if(!targetCompIdentifier.equals(convertServiceToComponent(fs, hostComp))){
				tmpFutureServices.remove(fs);
			}
		}
		
		boolean isLastUse = true;
		for (String fs : tmpFutureServices) {
			if(lddm.whetherUseInFuture(fs)){
				return false;
			}
		}
		
		return isLastUse;
	}

	private Set<String> convertServiceToComponent(Set<String> services, String hostComp){
		long enterTime = System.nanoTime();
		Set<String> comps = new HashSet<String>();
		
//		Iterator<Endpoint> endpointsIterator = endpoints.iterator(); 
		
		synchronized (serviceToComp) {
			if(serviceToComp.size() == 0){
				CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(hostComp);
				Node node = compLifeCycleMgr.getNode();
				DomainRegistry domainRegistry = ((NodeImpl)node).getDomainRegistry();
				Collection<Endpoint>  endpoints = domainRegistry.getEndpoints();
				for(Endpoint ep : endpoints){
					String URI = ep.getURI();
					int leftParIndex = URI.indexOf("(");
					int rightParIndex = URI.indexOf(")");
					int sharpIndex = URI.indexOf("#");
					String compName = URI.substring(0, sharpIndex);
					String serviceName = URI.substring(leftParIndex + 1,rightParIndex);
					
					serviceToComp.put(serviceName, compName);
				}
			}
		}
		
		// convert fdeps, pdeps from service to comps
		Iterator<String> iterator = services.iterator();
		while(iterator.hasNext()){
			/*
			 * since the services in the parameter are services with their package name,
			 * hence need to omit the package name first 
			 */
			String serviceName = iterator.next();
			serviceName = serviceName.substring(serviceName.lastIndexOf(".") + 1);
			comps.add(serviceToComp.get(serviceName + "/" + serviceName));
			
//			for(Endpoint ep : endpoints){
//				String URI = ep.getURI();
//				String serviceName = service.substring(service.lastIndexOf(".") + 1);
//				if(URI.contains(serviceName + "/" + serviceName)){
//					int index = URI.indexOf("#");
//					comps.add(URI.substring(0, index));
//				}
//			}
		}
		
		if(comps.size() != services.size()){
			LOGGER.fine("convert failure from service to component....\n" +
					comps + "\n" + services + "\n");
		}
		long leaveTime = System.nanoTime();
		LOGGER.fine(hostComp + " new convertServiceToComponent cost time:" + (leaveTime - enterTime) / 1000000.0);
		return comps;
	}

	public String convertServiceToComponent(String service, String hostComp){
		synchronized (serviceToComp) {
			if(serviceToComp.size() == 0){
				CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(hostComp);
				Node node = compLifeCycleMgr.getNode();
				DomainRegistry domainRegistry = ((NodeImpl)node).getDomainRegistry();
				Collection<Endpoint>  endpoints = domainRegistry.getEndpoints();
				for(Endpoint ep : endpoints){
					String URI = ep.getURI();
					int leftParIndex = URI.indexOf("(");
					int rightParIndex = URI.indexOf(")");
					int sharpIndex = URI.indexOf("#");
					String compName = URI.substring(0, sharpIndex);
					String serviceName = URI.substring(leftParIndex + 1,rightParIndex);
					
					serviceToComp.put(serviceName, compName);
				}
			}
		}
		String serviceName = service.substring(service.lastIndexOf(".") + 1);
		
		String compName = null;
		compName = serviceToComp.get(serviceName + "/" + serviceName);
		
		return compName;
	}

	@Override
	public void rootTxEnd(String hostComp, String rootTxId) {
		CompLifecycleManager compLcMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
//			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
//				&& compLcMgr.isDynamicUpdateRqstRCVD()
//				&& compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
			if(compLcMgr.isDynamicUpdateRqstRCVD() && compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
				compLcMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);

				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)
						&& compLcMgr.isDynamicUpdateRqstRCVD()) {
					compLcMgr.attemptToUpdate();
				}
			}
		}
		
//		ExecutionRecorder exeRecorder;
//		exeRecorder = ExecutionRecorder.getInstance(hostComp);
//		String completeAction = exeRecorder.getCompleteAction(rootTxId);
		
//		if(completeAction == null || completeAction.equals("null")){
//			if(TxLifecycleManager.getRootTx(hostComp, rootTxId)  != null)
//				completeAction = exeRecorder.getCompleteAction(TxLifecycleManager.getRootTx(hostComp, rootTxId));
//		}
//		if(completeAction != null){
//			LOGGER.info(completeAction);
//		}
//		
//		//when a root tx ends, remove it from TxLifecycleManager
//		if(TxLifecycleManager.getRootTx(hostComp, rootTxId) != null){
//			rootTxId = TxLifecycleManager.getRootTx(hostComp, rootTxId);
//		}
//		TxLifecycleManager.removeRootTx(hostComp, rootTxId);
		LOGGER.fine("In TxDepMonitorImpl, removed rootTxId " + rootTxId);
	}
	
	@Override
	public void rootTxEnd(String hostComp, String parentTxId, String rootTxId){
		CompLifecycleManager compLcMgr;
		NodeManager nodeManager = NodeManager.getInstance();
		
		DynamicDepManager dynamicDepMgr = nodeManager.getDynamicDepManager(hostComp);
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
		Object validToFreeSyncMonitor = dynamicDepMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
//			if(dynamicDepMgr.getCompStatus().equals(CompStatus.VALID) 
//				&& compLcMgr.isDynamicUpdateRqstRCVD()
//				&& compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
			if(compLcMgr.isDynamicUpdateRqstRCVD() && compLcMgr.getUpdateCtx().isOldRootTxsInitiated()){
//				if(TxLifecycleManager.getRootTx(rootTxId) != null){
//					rootTxId = TxLifecycleManager.getRootTx(rootTxId);
//				}
				compLcMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);
//				compLcMgr.getUpdateCtx().removeBufferOldRootTx(rootTxId);

				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)
						&& compLcMgr.isDynamicUpdateRqstRCVD()) {
					compLcMgr.attemptToUpdate();
				}
			}
		}
		
//		ExecutionRecorder exeRecorder;
//		exeRecorder = ExecutionRecorder.getInstance(hostComp);
//		String completeAction = exeRecorder.getCompleteAction(rootTxId);
//		if(completeAction == null || completeAction.equals("null")){
//			if(TxLifecycleManager.getRootTx(hostComp, rootTxId)  != null)
//				completeAction = exeRecorder.getCompleteAction(TxLifecycleManager.getRootTx(hostComp, rootTxId));
//		}
//		if(completeAction != null){
//			LOGGER.fine(completeAction);
//		}
//		
//		//when a root tx ends, remove it from TxLifecycleManager
//		TxLifecycleManager.removeRootTx(hostComp, parentTxId, rootTxId);
		LOGGER.fine("In TxDepMonitorImpl, removed rootTxId " + rootTxId);
	}
	
	/** return current thread ID. */
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	@Override
	public TxDepMonitor newInstance() {
		return new TxDepMonitorImpl();
	}

	@Override
	public boolean startRemoteSubTx(String subComp, String curComp,
			String rootTx, String parentTx, String subTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionStart, 
				subComp, curComp, rootTx, parentTx, subTx);
	}

	@Override
	public boolean endRemoteSubTx(String subComp, String curComp,
			String rootTx, String parentTx, String subTx) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(curComp);
		
		return depMgr.notifySubTxStatus(TxEventType.TransactionEnd, 
				subComp, curComp, rootTx, parentTx, subTx);
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
		NodeManager nodeManager = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeManager.getDynamicDepManager(hostComp);
		TransactionContext txCtx;
		
		txCtx = new TransactionContext();
		txCtx.setFakeTx(true);
		txCtx.setCurrentTx(fakeSubTx);
		txCtx.setHostComponent(hostComp);
		txCtx.setEventType(TxEventType.TransactionStart);
		txCtx.setFutureComponents(new HashSet<String>());
		txCtx.setPastComponents(new HashSet<String>());
		txCtx.setParentComponent(parentComp);
		txCtx.setParentTx(parentTx);
		txCtx.setRootTx(rootTx);
		txCtx.setRootComponent(rootComp);
		
		depMgr.getTxs().put(fakeSubTx, txCtx);
		
		return depMgr.initLocalSubTx(hostComp, fakeSubTx, rootTx, rootComp, parentTx, parentComp);
	}

	@Override
	public boolean endLocalSubTx(String hostComp, String fakeSubTx) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager depMgr = nodeMgr.getDynamicDepManager(hostComp);
		
		Object ondemandMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (ondemandMonitor) {
			depMgr.getTxs().remove(fakeSubTx);
		}
		return true;
	}
	
}
