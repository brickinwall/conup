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
	private Object freezeSyncMonitor;
	
	private BufferEventType bufferEventType = BufferEventType.NOTHING;
	private boolean freezeFlag = false;
	
	public BufferInterceptor(PolicySubject subject, Operation operation,
			String phase, NodeManager nodeMgr, DynamicDepManager depMgr,
			CompLifecycleManager clMgr, TxDepMonitor txDepMonitor, 
			TxLifecycleManager txLifecycleMgr) {
		this.subject = subject;
		this.operation = operation;
		this.phase = phase;
		this.nodeMgr = nodeMgr;
		this.depMgr = depMgr;
		freezeSyncMonitor = depMgr.getFreezeSyncMonitor();
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
		
		if (phase.equals(Phase.SERVICE_POLICY)) {
			synchronized (freezeSyncMonitor) {
				String hostComp;
				hostComp = getComponent().getName();
				String threadID;
				InterceptorCache cache = InterceptorCache.getInstance(hostComp);
				threadID = getThreadID();
				TransactionContext txCtx = cache.getTxCtx(threadID);
				
				Map<String, Object> msgHeaders = msg.getHeaders();
				Object subTx = msgHeaders.get(SUB_TX);
				
				msg = depMgr.checkOndemand(txCtx, subTx, this, msg);
				if(msg != null)
					return msg;
				
				msg = updateMgr.checkRemoteUpdate(txCtx, subTx, this, msg);
				if(msg != null)
					return msg;
				
				msg = depMgr.checkValidToFree(txCtx, subTx, this, msg, updateMgr);
				
				updateMgr.checkUpdate(this);
				
			}
		}
		
		return msg;
	}
	
	/**
	 * block all incoming message
	 */
	public void freeze(Object synMonitor){
		synchronized (synMonitor) {
			try {
				synMonitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * notify all blocked message
	 */
	public void defreeze(Object synMonitor){
		synchronized (synMonitor) {
			synMonitor.notifyAll();
		}
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
