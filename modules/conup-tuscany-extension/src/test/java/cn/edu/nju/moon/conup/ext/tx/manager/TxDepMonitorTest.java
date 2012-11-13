package cn.edu.nju.moon.conup.ext.tx.manager;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import cn.edu.nju.moon.conup.ext.ddm.LocalDynamicDependencesManager;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
/**
 * @author rgc
 */
public class TxDepMonitorTest {
	TxDepMonitor txDepMonitor = null;
	
	@Before
	public void setUp() throws Exception {
		txDepMonitor = new TxDepMonitor();
	}

	@Test
	public void testNotifyTxEventTypeString() {
		
		
		String curTxID = UUID.randomUUID().toString();
		TransactionContext tc = new TransactionContext();
		tc.setCurrentTx(curTxID);
		tc.setHostComponent("AuthComponent");
		ComponentObject compObject = new ComponentObject("AuthComponent", "1.1", "CONSISTENCY_ALGORITHM", FreenessStrategy.CONCURRENT_VERSION);
		NodeManager nodeMgr = NodeManager.getInstance();
		nodeMgr.setComponentObject("AuthComponent", compObject);
		DynamicDepManager ddm = nodeMgr.getDynamicDepManager("AuthComponent");
		ddm.setCompStatus(CompStatus.NORMAL);
		Map<String, TransactionContext> TX_IDS = TxLifecycleManager.TX_IDS;
		
		TX_IDS.put(curTxID, tc);
		
		String states = "";
		String nexts = "";
		LocalDynamicDependencesManager localDDM = LocalDynamicDependencesManager.getInstance(curTxID, states, nexts);
		txDepMonitor.notify(TxEventType.FirstRequestService, curTxID);
	}

}
