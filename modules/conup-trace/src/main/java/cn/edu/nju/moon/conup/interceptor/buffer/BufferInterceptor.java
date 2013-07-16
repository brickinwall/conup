package cn.edu.nju.moon.conup.interceptor.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.BufferEventType;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.RequestObject;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.pubsub.Observer;
import cn.edu.nju.moon.conup.spi.pubsub.Subject;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;
/**
 * BufferInterceptor is used to block message during the ondemand-setup, achieve free and update 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 *
 */
//@SuppressWarnings("unused")
public class BufferInterceptor implements Interceptor, Observer {
	private final static Logger LOGGER = Logger.getLogger(BufferInterceptor.class.getName());
	private static final String SUB_TX = "SUB_TX";
	private static String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";
	
	private PolicySubject subject;
	private Operation operation;
	private String phase;
	private NodeManager nodeMgr;
	private DynamicDepManager depMgr;
	private CompLifecycleManager clMgr;
	private TxDepMonitor txDepMonitor;
	private TxLifecycleManager txLifecycleMgr;
	private UpdateManager updateMgr;
	
	private BufferEventType bufferEventType = BufferEventType.NOTHING;
	
	public BufferInterceptor(PolicySubject subject, Operation operation,
			String phase, NodeManager nodeMgr, DynamicDepManager depMgr,
			CompLifecycleManager clMgr, TxDepMonitor txDepMonitor, 
			TxLifecycleManager txLifecycleMgr) {
		this.subject = subject;
		this.operation = operation;
		this.phase = phase;
		this.nodeMgr = nodeMgr;
		this.depMgr = depMgr;
		this.clMgr = clMgr;
		this.txDepMonitor = txDepMonitor;
		this.txLifecycleMgr = txLifecycleMgr;
		
		this.updateMgr = nodeMgr.getUpdateManageer(txLifecycleMgr.getCompIdentifier());
		// register to DDM
//		observable.addObserver(this);
		
		init();
	}
	
	@Override
	public Message invoke(Message msg) {
		// here we only block message at service phase
		if (phase.equals(Phase.SERVICE_POLICY)) {
			String hostComp;
			hostComp = getComponent().getName();
			String threadID;
			InterceptorCache cache = InterceptorCache.getInstance(hostComp);
			threadID = getThreadID();
			TransactionContext txCtx = cache.getTxCtx(threadID);
			
			Map<String, Object> msgHeaders = msg.getHeaders();
			Object subTx = msgHeaders.get(SUB_TX);
			switch(bufferEventType){
			case ONDEMAND:
				System.out.println("in buffer interceptor invoke():" + bufferEventType);
				ondemandSetup(msg);
				break;
			case VALIDTOFREE:
				System.out.println("in buffer interceptor invoke():" + bufferEventType);
				validToFree(msg);
				break;
			case WAITFORREMOTEUPDATE:
				System.out.println("in buffer interceptor invoke():" + bufferEventType);
				waitRemoteUpdate(msg);
				break;
			case EXEUPDATE:
				System.out.println("in buffer interceptor invoke():" + bufferEventType);
				exeUpdate();
//				break;
			default:
				if(txCtx.getRootTx() != null){
					assert txCtx.getParentTx() != null;
					assert txCtx.getParentComponent() != null;
					assert subTx != null;
					txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
				}
				break;
			}
			
			
//			if(ondemandInterceptor.invoke(msg) != null){
//				return msg;
//			}
//			
//			if(waitRemoteUpdateInterceptor.invoke(msg) != null){
//				return msg;
//			}
//			
//			validToFreeInterceptor.invoke(msg);
//			
//			freeToUpdate();
//			
//			// the invoked transaction is not a root transaction
//			if(txCtx.getRootTx() != null){
//				assert txCtx.getParentTx() != null;
//				assert txCtx.getParentComponent() != null;
//				assert subTx != null;
//				txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
//			}
		}// END IF(SERVICE_POLICY)
		return msg;
	}
	
	public void freeze(){
		
	}
	
	// waiting during on-demand setup
	private Message ondemandSetup(Message msg) {
		Object syncMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (syncMonitor) {
			try {
				if(depMgr.getCompStatus().equals(CompStatus.ONDEMAND)){
					LOGGER.info("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier() + depMgr.getCompStatus() +" thread suspended to wait for ondemand done--------------------------");
					syncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LOGGER.info("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier() + depMgr.getCompStatus() + " thread recover from wait for ondemand done--------------------------");
		}
		
		String hostComp;
		hostComp = getComponent().getName();
		String threadID;
		InterceptorCache cache = InterceptorCache.getInstance(hostComp);
		threadID = getThreadID();
		TransactionContext txCtx = cache.getTxCtx(threadID);
		
		Map<String, Object> msgHeaders = msg.getHeaders();
		Object subTx = msgHeaders.get(SUB_TX);
		
		if(txCtx.getRootTx() != null){
			assert txCtx.getParentTx() != null;
			assert txCtx.getParentComponent() != null;
			assert subTx != null;
			txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
		}
		return null;
	}

	// wait for remote target component finish update
	private Message waitRemoteUpdate(Message msg) {
		String hostComp = getComponent().getName();
		String threadID = getThreadID();
		InterceptorCache cache = InterceptorCache.getInstance(hostComp);
		TransactionContext txCtx = cache.getTxCtx(threadID);
		
		Map<String, Object> msgHeaders = msg.getHeaders();
		Object subTx = msgHeaders.get(SUB_TX);
		
		String freenessConf = depMgr.getCompObject().getFreenessConf();
		FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
		assert freeness!= null;
		assert txCtx != null;
		
		// haven't received update request yet
		Object waitingRemoteCompUpdateDoneMonitor = depMgr.getWaitingRemoteCompUpdateDoneMonitor();
		synchronized (waitingRemoteCompUpdateDoneMonitor) {
			if( freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, false)){
				try {
					LOGGER.info("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier() + depMgr.getCompStatus() + " thread suspended to wait for remote update done--------------------------");
					waitingRemoteCompUpdateDoneMonitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LOGGER.info("ThreadID=" + getThreadID() + " " +depMgr.getCompObject().getIdentifier() + depMgr.getCompStatus() + " thread suspended to recover from remote update done--------------------------");
			}
			// the invoked transaction is not a root transaction
			if(txCtx.getRootTx() != null){
				assert txCtx.getParentTx() != null;
				assert txCtx.getParentComponent() != null;
				assert subTx != null;
				txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
			}
			return msg;
		}
	}

	// try to be ready for update
	private void validToFree(Message msg) {
		String hostComp = getComponent().getName();
		String threadID = getThreadID();
		InterceptorCache cache = InterceptorCache.getInstance(hostComp);
		TransactionContext txCtx = cache.getTxCtx(threadID);
		
		String freenessConf = depMgr.getCompObject().getFreenessConf();
		FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
		Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			// calculate old version root txs
			if (!updateMgr.getUpdateCtx().isOldRootTxsInitiated()) {
				updateMgr.initOldRootTxs();
				Printer printer = new Printer();
				printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
			}
			if (!freeness.isReadyForUpdate(hostComp)) {
				Class<?> compClass = freeness.achieveFreeness(
				txCtx.getRootTx(), txCtx.getRootComponent(),
				txCtx.getParentComponent(), txCtx.getCurrentTx(), hostComp);
				if (compClass != null) {
					addBufferMsgBody(msg, compClass);
				}
			}
			if (freeness.isReadyForUpdate(hostComp)) {
				depMgr.achievedFree();
			} else if (freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, true)) {
				LOGGER.info("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + "----------------validToFreeSyncMonitor.wait();buffer------------root:"	+ txCtx.getRootTx() + ",parent:" + txCtx.getParentTx());
				Printer printer = new Printer();
				printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
				
				try {
					validToFreeSyncMonitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LOGGER.info("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + "----------------validToFreeSyncMonitor.recover()");
			} else {
			}
		}
		
		Map<String, Object> msgHeaders = msg.getHeaders();
		Object subTx = msgHeaders.get(SUB_TX);
		// the invoked transaction is not a root transaction
		if(txCtx.getRootTx() != null){
			assert txCtx.getParentTx() != null;
			assert txCtx.getParentComponent() != null;
			assert subTx != null;
			txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
		}
	}

	// if ready for update, execute update
	private void exeUpdate() {
		Object updatingSyncMonitor = depMgr.getUpdatingSyncMonitor();
		synchronized (updatingSyncMonitor) {
			if (depMgr.getCompStatus().equals(CompStatus.Free)) {
				LOGGER.fine("ThreadID=" + getThreadID() + "compStatus=" + depMgr.getCompStatus() + ", in buffer updatingSyncMonitor, is Free for update now, try to execute update...");
				updateMgr.executeUpdate();
				updateMgr.cleanupUpdate();
			}
		}
	}

	private void addBufferMsgBody(Message msg, Class<?> compClass) {
		String className = compClass.getName();
		List<Object> originalMsgBody;
		List<Object> copyOfMsgBody = new ArrayList<Object>();
		originalMsgBody = Arrays.asList((Object [])msg.getBody());
		copyOfMsgBody.addAll(originalMsgBody);
		copyOfMsgBody.add(COMP_CLASS_OBJ_IDENTIFIER + ":" + className);
		copyOfMsgBody.add(compClass);
		msg.setBody((Object [])copyOfMsgBody.toArray());
	}
	
	private Component getComponent(){
		if (subject instanceof Endpoint) {
			Endpoint endpoint = (Endpoint) subject;
			return endpoint.getComponent();
		} else if (subject instanceof EndpointReference) {
			EndpointReference endpointReference = (EndpointReference) subject;
			return endpointReference.getComponent();
		} else if (subject instanceof Component) {
			Component component = (Component) subject;
			return component;
		}
		return null;
		
	}
	
	private String getThreadID(){
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	private void init() {

	}

	@Override
	public void update(Subject subject, Object arg) {
		this.bufferEventType = (BufferEventType) arg;
//		System.out.println("in buffer interceptor update():" + bufferEventType);
	}



}
