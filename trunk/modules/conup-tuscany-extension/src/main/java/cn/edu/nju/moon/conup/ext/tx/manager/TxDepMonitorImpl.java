/**
 * 
 */
package cn.edu.nju.moon.conup.ext.tx.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.runtime.DomainRegistry;

import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
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
		
		boolean result = dynamicDepMgr.manageTx(txContext);
		// when be notified that a tx ends, remove it from TX_IDS.
		if(et.equals(TxEventType.TransactionEnd)){
			TX_IDS.remove(txContext.getHostComponent());
			
			InterceptorCache interceptorCache = InterceptorCache.getInstance(txContext.getHostComponent());
			interceptorCache.removeTxCtx(getThreadID());
			
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
			if(!targetCompIdentifier.equals(convertServiceToComp(fs, hostComp))){
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
		Set<String> comps = new HashSet<String>();
		
		CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(hostComp);
		Node node = compLifeCycleMgr.getNode();
		DomainRegistry domainRegistry = ((NodeImpl)node).getDomainRegistry();
		Collection<Endpoint>  endpoints = domainRegistry.getEndpoints();
//		Iterator<Endpoint> endpointsIterator = endpoints.iterator(); 
		
		// convert fdeps, pdeps from service to comps
		Iterator<String> iterator = services.iterator();
		while(iterator.hasNext()){
			String service = iterator.next();
			for(Endpoint ep : endpoints){
				String URI = ep.getURI();
				String serviceName = service.substring(service.lastIndexOf(".") + 1);
				if(URI.contains(serviceName)){
					int index = URI.indexOf("#");
					comps.add(URI.substring(0, index));
				}
			}
//			while(endpointsIterator.hasNext()){
//				Endpoint ep = endpointsIterator.next();
//				String URI = ep.getURI();
//				if(URI.contains(service)){
//					int index = URI.indexOf("#");
//					comps.add(URI.substring(0, index));
//				}
//			}
		}
		
		if(comps.size() != services.size()){
			LOGGER.warning("convert failure from service to component....\n" +
					comps + "\n" + services + "\n");
		}
		
		return comps;
	}

	private String convertServiceToComp(String service, String hostComp){
		CompLifecycleManager compLifeCycleMgr = CompLifecycleManager.getInstance(hostComp);
		Node node = compLifeCycleMgr.getNode();
		DomainRegistry domainRegistry = ((NodeImpl)node).getDomainRegistry();
		Collection<Endpoint>  endpoints = domainRegistry.getEndpoints();
		Iterator<Endpoint> endpointsIterator = endpoints.iterator(); 
		String serviceName = service.substring(service.lastIndexOf(".") + 1);
		
		while (endpointsIterator.hasNext()) {
			Endpoint endpoint = (Endpoint) endpointsIterator.next();
			String URI = endpoint.getURI();
			if(URI.contains(serviceName)){
				int index = URI.indexOf("#");
				return URI.substring(0, index);
			}
		}
		
		return null;
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
//				if(TxLifecycleManager.getRootTx(rootTxId) != null){
//					rootTxId = TxLifecycleManager.getRootTx(rootTxId);
//				}
				compLcMgr.getUpdateCtx().removeAlgorithmOldRootTx(rootTxId);
				compLcMgr.getUpdateCtx().removeBufferOldRootTx(rootTxId);

				LOGGER.fine("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)
						&& compLcMgr.isDynamicUpdateRqstRCVD()) {
					compLcMgr.attemptToUpdate();
				}
			}
		}
		
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(hostComp);
		String completeAction = exeRecorder.getCompleteAction(rootTxId);
		if(completeAction == null || completeAction.equals("null")){
			if(TxLifecycleManager.getRootTx(rootTxId)  != null)
				completeAction = exeRecorder.getCompleteAction(TxLifecycleManager.getRootTx(rootTxId));
		}
		if(completeAction != null){
			LOGGER.info(completeAction);
		}
		
		//when a root tx ends, remove it from TxLifecycleManager
		if(TxLifecycleManager.getRootTx(rootTxId) != null){
			rootTxId = TxLifecycleManager.getRootTx(rootTxId);
		}
		TxLifecycleManager.removeRootTx(rootTxId);
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
				compLcMgr.getUpdateCtx().removeBufferOldRootTx(rootTxId);

				LOGGER.info("removeOldRootTx(ALG&&BUFFER) txID:" + rootTxId);

				if (dynamicDepMgr.getCompStatus().equals(CompStatus.VALID)
						&& compLcMgr.isDynamicUpdateRqstRCVD()) {
					compLcMgr.attemptToUpdate();
				}
			}
		}
		
		ExecutionRecorder exeRecorder;
		exeRecorder = ExecutionRecorder.getInstance(hostComp);
		String completeAction = exeRecorder.getCompleteAction(rootTxId);
		if(completeAction == null || completeAction.equals("null")){
			if(TxLifecycleManager.getRootTx(rootTxId)  != null)
				completeAction = exeRecorder.getCompleteAction(TxLifecycleManager.getRootTx(rootTxId));
		}
		if(completeAction != null){
			LOGGER.info(completeAction);
		}
		
		//when a root tx ends, remove it from TxLifecycleManager
//		if(TxLifecycleManager.getRootTx(rootTxId) != null){
//			rootTxId = TxLifecycleManager.getRootTx(rootTxId);
//		}
		TxLifecycleManager.removeRootTx(parentTxId, rootTxId);
		LOGGER.fine("In TxDepMonitorImpl, removed rootTxId " + rootTxId);
	}
	
	/** return current thread ID. */
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}
	
}
