package cn.edu.nju.moon.conup.interceptor.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySubject;

import cn.edu.nju.moon.conup.interceptor.tx.TxInterceptor;
import cn.edu.nju.moon.conup.spi.datamodel.BufferEventType;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
/**
 * BufferInterceptor is used to block message during the ondemand-setup, achieve free and update 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 *
 */
public class BufferInterceptor implements Interceptor {
	private final static Logger LOGGER = Logger.getLogger(BufferInterceptor.class.getName());
//	private static final String SUB_TX = "SUB_TX";
	private static String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";
	
	private PolicySubject subject;
	private String phase;
	private TxLifecycleManager txLifecycleMgr;
	private UpdateManager updateMgr;
	private CompLifeCycleManager compLifeCycleMgr = null;
	private static Object freezeSyncMonitor = new Object();
	private FreenessStrategy freeness = null;
	
	private BufferEventType bufferEventType = BufferEventType.NOTHING;
	
	public BufferInterceptor(PolicySubject subject, String phase, TxLifecycleManager txLifecycleMgr, FreenessStrategy freeness,
			CompLifeCycleManager compLifeCycleMgr, UpdateManager updateMgr) {
		this.subject = subject;
		this.phase = phase;
		this.txLifecycleMgr = txLifecycleMgr;
		this.freeness = freeness;
		this.compLifeCycleMgr = compLifeCycleMgr;
		this.updateMgr = updateMgr;
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
//			Object subTx = msgHeaders.get(SUB_TX);
			String subTx = ((InvocationContext)msgHeaders.get(TxInterceptor.INVOCATION_CONTEXT)).getSubTx();
			System.out.println("in buffer interceptor invoke():" + bufferEventType);
			synchronized (bufferEventType) {
				switch(bufferEventType){
				case ONDEMAND:
					ondemandSetup(msg);
					break;
				case VALIDTOFREE:
					validToFree(msg);
					break;
				case WAITFORREMOTEUPDATE:
					waitRemoteUpdate(msg);
					break;
				case EXEUPDATE:
					exeUpdate();
					break;
				default:
					break;
				}
			}
			
			if(txCtx.getRootTx() != null){
				assert txCtx.getParentTx() != null;
				assert txCtx.getParentComponent() != null;
				assert subTx != null;
				txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
			}
			
		}// END IF(SERVICE_POLICY)
		return msg;
	}
	
	// wait for remote target component finish update
	private Message waitRemoteUpdate(Message msg) {
		String hostComp = getComponent().getName();
		String threadID = getThreadID();
		InterceptorCache cache = InterceptorCache.getInstance(hostComp);
		TransactionContext txCtx = cache.getTxCtx(threadID);

		assert freeness != null;
		assert txCtx != null;
		
		synchronized (freezeSyncMonitor) {
			if (freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, false)) {
				freeze();
			}
		}
		return msg;
		
	}
	
	private void exeUpdate() {
		freeze();
	}

	// try to be ready for update
	private void validToFree(Message msg) {
		String hostComp = getComponent().getName();
		String threadID = getThreadID();
		InterceptorCache cache = InterceptorCache.getInstance(hostComp);
		TransactionContext txCtx = cache.getTxCtx(threadID);

		Object validToFreeSyncMonitor = compLifeCycleMgr.getCompObject().getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			// calculate old version root txs
			if (!updateMgr.getUpdateCtx().isOldRootTxsInitiated()) {
				updateMgr.initOldRootTxs();
//				Printer printer = new Printer();
//				printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
			}
			if (!freeness.isReadyForUpdate(hostComp)) {
				Class<?> compClass = freeness.achieveFreeness(
						txCtx.getRootTx(), txCtx.getRootComponent(),
						txCtx.getParentComponent(), txCtx.getCurrentTx(),
						hostComp);
				if (compClass != null) {
					addBufferMsgBody(msg, compClass);
				}
			}
			if (freeness.isReadyForUpdate(hostComp)) {
//				compLifeCycleMgr.achieveFree();
				updateMgr.achieveFree();
			} else if (freeness.isInterceptRequiredForFree(txCtx.getRootTx(),
					hostComp, txCtx, true)) {
				LOGGER.info("ThreadID="	+ getThreadID() + "compStatus=" + compLifeCycleMgr.getCompStatus() + "----------------validToFreeSyncMonitor.wait();buffer------------root:"	+ txCtx.getRootTx() + ",parent:" + txCtx.getParentTx());
//				Printer printer = new Printer();
//				printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");

				try {
					validToFreeSyncMonitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LOGGER.info("ThreadID=" + getThreadID() + "compStatus=" + compLifeCycleMgr.getCompStatus() + "----------------validToFreeSyncMonitor.recover()");
			} else {
			}
		}

		// Map<String, Object> msgHeaders = msg.getHeaders();
		// Object subTx = msgHeaders.get(SUB_TX);
		// // the invoked transaction is not a root transaction
		// if(txCtx.getRootTx() != null){
		// assert txCtx.getParentTx() != null;
		// assert txCtx.getParentComponent() != null;
		// assert subTx != null;
		// txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
		// }
	}
		
	private void ondemandSetup(Message msg) {
		freeze();
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
	
	/**
	 * block all incoming message by using freezeSyncMonitor 
	 */
	public void freeze(){
		synchronized (freezeSyncMonitor) {
			try {
				freezeSyncMonitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * notify all blocked message by using freezeSyncMonitor
	 */
	public void defreeze(){
		synchronized (freezeSyncMonitor) {
			freezeSyncMonitor.notifyAll();
		}
	}

	/**
	 * block all incoming message
	 */
//	public void freeze(Object synMonitor){
//		synchronized (synMonitor) {
//			try {
//				synMonitor.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	

	/**
	 * notify all blocked message
	 */
//	public void defreeze(Object synMonitor){
//		synchronized (synMonitor) {
//			synMonitor.notifyAll();
//		}
//	}

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


	@Override
	public void update(Object arg) {
//		synchronized (freezeSyncMonitor) 
		{
			this.bufferEventType = (BufferEventType) arg;
			if(bufferEventType.equals(BufferEventType.NOTHING) ||
					bufferEventType.equals(BufferEventType.VALIDTOFREE) ||
					bufferEventType.equals(BufferEventType.WAITFORREMOTEUPDATE)){
				defreeze();
			}
		}
	}

}