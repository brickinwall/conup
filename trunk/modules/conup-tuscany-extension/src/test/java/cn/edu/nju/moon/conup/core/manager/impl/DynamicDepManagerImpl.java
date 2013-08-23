package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class DynamicDepManagerImpl implements DynamicDepManager {

	private ComponentObject compObj;

	@Override
	public boolean manageTx(TransactionContext txContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadyForUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Scope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	@Override
	public void setAlgorithm(Algorithm algorithm) {
	}

	@Override
	public Set<String> getAlgorithmOldVersionRootTxs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScope(Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ondemandSetupIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dynamicUpdateIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getStaticDeps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Dependence> getRuntimeDeps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Dependence> getRuntimeInDeps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, TransactionContext> getTxs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getStaticInDeps() {
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

	// @Override
	// public boolean initLocalSubTx(String hostComp, String fakeSubTx, String
	// rootTx, String rootComp, String parentTx, String parentComp) {
	// // TODO Auto-generated method stub
	// return false;
	// }

	// @Override
	// public Map<String, TransactionContext> getFakeTxs() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public void dependenceChanged(String hostComp) {
		// TODO Auto-generated method stub

	}

	@Override
	public TxLifecycleManager getTxLifecycleMgr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTxLifecycleMgr(TxLifecycleManager txLifecycleMgr) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCompLifeCycleMgr(CompLifeCycleManager compLifecycleManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTxDepRegistry(TxDepRegistry txDepRegistry) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean manageDependence(String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType transactionstart,
			InvocationContext invocationCtx,
			CompLifeCycleManager compLifeCycleMgr) {
		// TODO Auto-generated method stub
		return false;
	}

}
