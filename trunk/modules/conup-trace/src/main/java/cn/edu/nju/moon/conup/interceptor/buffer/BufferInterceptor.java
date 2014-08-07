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
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.Interceptor;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
/**
 * BufferInterceptor is used to block message during the ondemand-setup, achieve
 * free and update
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * 
 */
public class BufferInterceptor extends Interceptor {
	private final static Logger LOGGER = Logger
			.getLogger(BufferInterceptor.class.getName());
	// private static final String SUB_TX = "SUB_TX";
	private static String COMP_CLASS_OBJ_IDENTIFIER = "COMP_CLASS_OBJ_IDENTIFIER";

	private PolicySubject subject;
	private String phase;
	@SuppressWarnings("unused")
	private TxLifecycleManager txLifecycleMgr;
	private UpdateManager updateMgr;
	private CompLifeCycleManager compLifeCycleMgr = null;
	private ComponentObject compObj = null;
	// TODO every component should have a freezeSyncMonitor, current code means
	// that all components runs in one node have a freezeSyncMonitor
	private Object freezeSyncMonitor = null;
	private Object ondemandSyncMonitor = null;
	private FreenessStrategy freeness = null;

	public BufferInterceptor(PolicySubject subject, String phase,
			TxLifecycleManager txLifecycleMgr, FreenessStrategy freeness,
			CompLifeCycleManager compLifeCycleMgr, UpdateManager updateMgr) {
		this.subject = subject;
		this.phase = phase;
		this.txLifecycleMgr = txLifecycleMgr;
		this.freeness = freeness;
		this.compLifeCycleMgr = compLifeCycleMgr;
		this.updateMgr = updateMgr;
		this.compObj = compLifeCycleMgr.getCompObject();
		this.freezeSyncMonitor = compObj.getFreezeSyncMonitor();
		this.ondemandSyncMonitor = compObj.getOndemandSyncMonitor();
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
			
			TxLifecycleManager txLifecycleMgr = NodeManager.getInstance().getTxLifecycleManager(hostComp);
			Map<String, Object> msgHeaders = msg.getHeaders();

			// waiting during on-demand setup
			String subTx = ((InvocationContext) msgHeaders
					.get(TxInterceptor.INVOCATION_CONTEXT)).getSubTx();
			synchronized (ondemandSyncMonitor) {
				if (compLifeCycleMgr.isNormal()) {
					// the invoked transaction is not a root transaction
					if(txCtx.getRootTx() != null){
						assert txCtx.getParentTx() != null;
						assert txCtx.getParentComponent() != null;
						assert subTx != null;
						assert msgHeaders != null;
						
						txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
					}
					return msg;
				}
				
				try {
					if (compLifeCycleMgr.isOndemandSetting()) {
						LOGGER.fine("ThreadID=" + getThreadID() + " " +compLifeCycleMgr.getCompObject().getIdentifier()+ "----------------ondemandSyncMonitor.wait()------------");
						ondemandSyncMonitor.wait();
						LOGGER.fine("ThreadID=" + getThreadID() + " " +compLifeCycleMgr.getCompObject().getIdentifier()+ "----------------finish ondemand------------");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			
			Object waitingRemoteCompUpdateDoneMonitor = compObj.getWaitingRemoteCompUpdateDoneMonitor();
			synchronized (waitingRemoteCompUpdateDoneMonitor) {
				if(!updateMgr.isDynamicUpdateRqstRCVD()) {	
					if( freeness.isInterceptRequiredForFree(txCtx.getRootTx(), hostComp, txCtx, false)){
						try {
							LOGGER.fine("ThreadID=" + getThreadID() + " " + compObj.getIdentifier() + " thread suspended to wait for remote update done--------------------------");
							waitingRemoteCompUpdateDoneMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
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
			Object validToFreeSyncMonitor = compObj.getValidToFreeSyncMonitor();
			synchronized (validToFreeSyncMonitor) {
					
				if(compLifeCycleMgr.isValid() && updateMgr.isDynamicUpdateRqstRCVD()) {	
					// calculate old version root txs
					if (!updateMgr.getUpdateCtx().isOldRootTxsInitiated()) {
						updateMgr.initOldRootTxs();
						// Printer printer = new Printer();
						// printer.printDeps(depMgr.getRuntimeInDeps(),
						// "inDeps:");
					}
					if (!freeness.isReadyForUpdate(hostComp)) {
						// LOGGER.fine("ThreadID=" + getThreadID()
						// + "compStatus=" + depMgr.getCompStatus()
						// + ", in buffer, try to be free via "
						// + freeness.getFreenessType());
						Class<?> compClass = freeness.achieveFreeness(
								txCtx.getRootTx(), txCtx.getRootComponent(),
								txCtx.getParentComponent(), txCtx.getCurrentTx(),
								hostComp);
						if (compClass != null) {
							addBufferMsgBody(msg, compClass);
						}
					}
					if (freeness.isReadyForUpdate(hostComp)) {
						updateMgr.achieveFree();
					} else if (freeness.isInterceptRequiredForFree(
							txCtx.getRootTx(), hostComp, txCtx, true)) {
						LOGGER.fine("ThreadID="
								+ getThreadID()
								+ "compStatus="
								+ compLifeCycleMgr.getCompStatus()
								+ "----------------validToFreeSyncMonitor.wait();buffer------------root:"
								+ txCtx.getRootTx() + ",parent:"
								+ txCtx.getParentTx());
						try {
							validToFreeSyncMonitor.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
					}
				}
			}
			
			
			// if ready for update
			Object updatingSyncMonitor = compObj.getUpdatingSyncMonitor();
			synchronized (updatingSyncMonitor) {
				if (compLifeCycleMgr.isFree()) {
					LOGGER.fine("ThreadID="
							+ getThreadID()
							+ "compStatus="
							+ compLifeCycleMgr.getCompStatus()
							+ ", in buffer updatingSyncMonitor, is Free for update now, try to execute update...");
					updateMgr.executeUpdate();
					updateMgr.cleanupUpdate();
				}

			}
			
			// the invoked transaction is not a root transaction
			if (txCtx.getRootTx() != null) {
				assert txCtx.getParentTx() != null;
				assert txCtx.getParentComponent() != null;
				assert subTx != null;
				txLifecycleMgr.initLocalSubTx(hostComp, subTx.toString(), txCtx);
			}
			
		}
		
		return msg;
	}

	private void addBufferMsgBody(Message msg, Class<?> compClass) {
		String className = compClass.getName();
		List<Object> originalMsgBody;
		List<Object> copyOfMsgBody = new ArrayList<Object>();
		originalMsgBody = Arrays.asList((Object[]) msg.getBody());
		copyOfMsgBody.addAll(originalMsgBody);
		copyOfMsgBody.add(COMP_CLASS_OBJ_IDENTIFIER + ":" + className);
		copyOfMsgBody.add(compClass);
		msg.setBody((Object[]) copyOfMsgBody.toArray());
	}

	/**
	 * block all incoming message by using freezeSyncMonitor
	 */
	public void freeze() {
		synchronized (freezeSyncMonitor) {
			try {
				
				String hostComp = getComponent().getName();
				String threadID = getThreadID();
				InterceptorCache cache = InterceptorCache.getInstance(hostComp);
				TransactionContext txCtx = cache.getTxCtx(threadID);
				LOGGER.fine("freezee() --> ThreadID="
						+ getThreadID()
						+ "compStatus:"
						+ compLifeCycleMgr.getCompStatus() 
						+ "----------------validToFreeSyncMonitor.wait();buffer------------root:"
						+ txCtx.getRootTx() + ",parent:" + txCtx.getParentTx());
				freezeSyncMonitor.wait();
				LOGGER.fine("freezee() --> ThreadID="
						+ getThreadID() + " after...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * notify all blocked message by using freezeSyncMonitor
	 */
	public void defreeze() {
		synchronized (freezeSyncMonitor) {
			freezeSyncMonitor.notifyAll();
		}
	}

	private Component getComponent() {
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

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	private void init() {
		
	}

	@Override
	public void update(Object arg) {
		
	}

}