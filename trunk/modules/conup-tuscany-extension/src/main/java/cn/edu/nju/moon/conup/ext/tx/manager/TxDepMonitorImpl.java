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

import cn.edu.nju.moon.conup.ext.comp.manager.CompLifecycleManagerImpl;
import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxDep;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * It's used to monitor transaction status, maintain transaction context 
 * and possibly invoke related algorithm
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TxDepMonitorImpl implements TxDepMonitor {
	private static final Logger LOGGER = Logger.getLogger(TxDepMonitorImpl.class.getName());
	/** used to store the mapping between service name and component name*/
	private static Map<String, String> serviceToComp = new ConcurrentHashMap<String, String>();
	/** used to store the deps during the tx running*/
	private TxDepRegistry txDepRegistry = new TxDepRegistry();
	
	//	private String compIdentifier = null;
	private ComponentObject compObject = null;
	
	public TxDepMonitorImpl(ComponentObject compObject){
		this.compObject = compObject;
	}
	
//	public TxDepMonitorImpl(String compIdentifier){
//		this.compIdentifier = compIdentifier;
//	}
	
	/**
	 * 
	 * @param TxEventType 
	 * @param curTxID current tx id
	 * @return
	 */
	public boolean notify(TxEventType et, String curTxID){
		LOGGER.fine("--------TxDepMonitor.notify(" + et.toString() + "," + curTxID + ")--------------");
		NodeManager nodeMgr = NodeManager.getInstance();
		String compIdentifier = compObject.getIdentifier();
		TxLifecycleManager txLifecycleMgr = nodeMgr.getTxLifecycleManager(compIdentifier);
		TransactionRegistry txRegistry = txLifecycleMgr.getTxRegistry();
		
		LocalDynamicDependencesManager ddm = LocalDynamicDependencesManager.getInstance(curTxID);
		TransactionContext txContext = txRegistry.getTransactionContext(curTxID);
		txContext.setEventType(et);
		
		/*
		 * set eventType, futureC, pastC 
		 */
		TxDep txDep = new TxDep(convertServiceToComponent(ddm.getFuture(), txContext.getHostComponent()), convertServiceToComponent(ddm.getPast(), txContext.getHostComponent()));
		txDepRegistry.addLocalDep(curTxID, txDep);
//		txContext.setFutureComponents(convertServiceToComponent(ddm.getFuture(), txContext.getHostComponent()));
//		txContext.setPastComponents(convertServiceToComponent(ddm.getPast(), txContext.getHostComponent()));
		/*
		 * use componentIdentifier to get specific DynamicDepManager
		 */
		
		DynamicDepManager dynamicDepMgr = nodeMgr.getDynamicDepManager(txContext.getHostComponent());
		boolean result = dynamicDepMgr.manageTx(txContext);
		// when be notified that a tx ends, remove it from TxRegistry.
		if(et.equals(TxEventType.TransactionEnd)){
			txRegistry.removeTransactionContext(curTxID);
			txDepRegistry.removeLocalDep(curTxID);
			
			InterceptorCache interceptorCache = InterceptorCache.getInstance(txContext.getHostComponent());
			interceptorCache.removeTxCtx(getThreadID());
			
			CompLifeCycleManager compLifeCycleMgr = nodeMgr.getCompLifecycleManager(compIdentifier);
			UpdateManager updateMgr = nodeMgr.getUpdateManageer(compIdentifier);
			if(compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID)
					&& updateMgr.isDynamicUpdateRqstRCVD()){
				updateMgr.attemptToUpdate();
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
//				CompLifecycleManagerImpl compLifeCycleMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(hostComp);
//				Node node = compLifeCycleMgr.getNode();
				Node node = (Node) NodeManager.getInstance().getTuscanyNode();
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

	@Override
	public String convertServiceToComponent(String service, String hostComp){
		synchronized (serviceToComp) {
			if(serviceToComp.size() == 0){
//				CompLifecycleManagerImpl compLifeCycleMgr = (CompLifecycleManagerImpl) CompLifecycleManagerImpl.getInstance(hostComp);
//				Node node = compLifeCycleMgr.getNode();
				Node node = (Node) NodeManager.getInstance().getTuscanyNode();
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

	public TxDepRegistry getTxDepRegistry() {
		return txDepRegistry;
	}

	/** return current thread ID. */
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

}
