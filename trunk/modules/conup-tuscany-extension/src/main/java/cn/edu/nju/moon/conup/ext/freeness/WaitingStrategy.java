package cn.edu.nju.moon.conup.ext.freeness;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * Implementation of waiting strategy for achieving freeness
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class WaitingStrategy implements FreenessStrategy {
	/** represent waiting strategy */
	public final static String WAITING = "WAITING_FOR_FREENESS";
	private CompLifeCycleManager compLifeCycleMgr = null;
	
	public WaitingStrategy(CompLifeCycleManager compLifeCycleMgr) {
		this.compLifeCycleMgr  = compLifeCycleMgr;
	}

	@Override
	public Class<?> achieveFreeness(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp) {
		return null;
	}

	@Override
	public String getFreenessType() {
		return WaitingStrategy.WAITING;
	}

	@Override
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateRCVD) {
		return false;
	}

	@Override
	public boolean isReadyForUpdate(String hostComp) {
		return compLifeCycleMgr.isReadyForUpdate();
	}

}
