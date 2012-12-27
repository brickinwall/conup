package cn.edu.nju.moon.conup.ext.tx.manager;

import static org.junit.Assert.*;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.InterceptorCache;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * @author rgc
 */
public class TxLifecycleManagerTest {
	TxLifecycleManager txLifecycleMgr = null;
	ComponentObject compObj = null;
	NodeManager nodeMgr = null;
	
	@Before
	public void setUp() throws Exception {
		txLifecycleMgr = new TxLifecycleManager();
		
		nodeMgr = NodeManager.getInstance();
		
		final String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
		String compIdentifier = null;
		String compVer = null;
		String algorithmConf = null;
		String freenessConf = null;
		compIdentifier = "AuthComponent";
		compVer = "1.1";
		algorithmConf = "";
		freenessConf = CONCURRENT_VERSION;
		String JAVA_POJO_IMPL_TYPE = "JAVA_POJO";
		compObj = new ComponentObject(compIdentifier, compVer, algorithmConf, freenessConf, 
				null,null, JAVA_POJO_IMPL_TYPE);
		nodeMgr.addComponentObject(compObj.getIdentifier(), compObj);
	}

	@Test
	public void testCreateID() {
		String componentIdentifier = "AuthComponent";
		txLifecycleMgr.addToAssociateTx(getThreadID(), componentIdentifier);
		InterceptorCache interceptorCache = InterceptorCache.getInstance(componentIdentifier);
		
		String hostComp = componentIdentifier;
		String parentComp = "PortalComponent";
		String rootComp = "PortalComponent";
		TransactionContext txContext = interceptorCache.getTxCtx(getThreadID());
		String rootTx = UUID.randomUUID().toString();
		
		if(txContext == null){
			// generate and init TransactionDependency
			txContext = new TransactionContext();
			txContext.setCurrentTx(null);
			txContext.setHostComponent(hostComp);
			txContext.setParentTx(rootTx);
			txContext.setParentComponent(parentComp);
			txContext.setRootTx(rootTx);
			txContext.setRootComponent(rootComp);
			//add to InterceptorCacheImpl
			interceptorCache.addTxCtx(getThreadID(), txContext);
		} else{
			txContext.setHostComponent(hostComp);
		}
		
		String currentTxID = txLifecycleMgr.createID();
		assertEquals(currentTxID, txContext.getCurrentTx());
		txLifecycleMgr.destroyID(currentTxID);
	}

	private String getThreadID() {
		return new Integer(Thread.currentThread().hashCode()).toString();
	}

	@Test
	public void testDestroyID() {
		String currentTxID = UUID.randomUUID().toString();
		txLifecycleMgr.TX_IDS.put(currentTxID, new TransactionContext());
		
		
		txLifecycleMgr.destroyID(currentTxID);
		assertTrue(!txLifecycleMgr.TX_IDS.containsKey(currentTxID));
	}

	@Test
	public void testGetTxs() {
		String currentTxID = UUID.randomUUID().toString();
		txLifecycleMgr.TX_IDS.put(currentTxID, new TransactionContext());
		
		assertEquals(1, txLifecycleMgr.getTxs());
	}

}
