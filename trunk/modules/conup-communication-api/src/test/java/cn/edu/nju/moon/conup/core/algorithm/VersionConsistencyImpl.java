package cn.edu.nju.moon.conup.core.algorithm;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class VersionConsistencyImpl implements Algorithm {
	/** represent version-consistency algorithm */
	public final static String ALGORITHM_TYPE = "CONSISTENCY_ALGORITHM";
	@Override
	public void manageDependence(TransactionContext txContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean manageDependence(String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadyForUpdate(String compIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getOldVersionRootTxs(Set<Dependence> allDeps) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmType() {
		return ALGORITHM_TYPE;
	}

	@Override
	public boolean isBlockRequiredForFree(
			Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext,
			boolean isUpdateReqRCVD) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateIsDone(String hostComp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initiate(String identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmRoot(String parentTx, String rootTx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx,
			String parentTx, String subTx) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx, String rootComp, String parentTx, String parentComp) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public void setDynamicDepMgr(DynamicDepManager depMgr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		// TODO Auto-generated method stub
		
	}

}
