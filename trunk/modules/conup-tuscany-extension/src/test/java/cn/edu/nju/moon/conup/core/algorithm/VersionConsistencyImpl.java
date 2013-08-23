package cn.edu.nju.moon.conup.core.algorithm;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.utils.OperationType;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class VersionConsistencyImpl implements Algorithm {
	/** represent version-consistency algorithm */
	public final static String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";

	@Override
	public String getAlgorithmType() {
		return ALGORITHM_TYPE;
	}

	@Override
	public void manageDependence(TransactionContext txContext,
			DynamicDepManager depMgr, CompLifeCycleManager compLifeCycleMgr) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean manageDependence(OperationType operationType,
			Map<String, String> params, DynamicDepManager depMgr,
			CompLifeCycleManager compLifeCycleMgr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean readyForUpdate(String compIdentifier,
			DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateIsDone(String hostComp, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initiate(String identifier, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr, DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		return false;
	}

}
