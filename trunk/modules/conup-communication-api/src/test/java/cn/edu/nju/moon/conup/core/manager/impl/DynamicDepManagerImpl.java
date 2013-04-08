package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

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
	public boolean manageDependence(TransactionContext txContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean manageDependence(String proctocol, String payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInterceptRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNormal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void ondemandSetting() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOndemandSetting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOndemandSetupRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getAlgorithmOldVersionRootTxs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> convertToAlgorithmRootTxs(Map<String, String> oldRootTxs) {
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
	public void remoteDynamicUpdateIsDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void achievedFree() {
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
	public void updating() {
		// TODO Auto-generated method stub

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
	public CompStatus getCompStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getOndemandSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValidToFreeSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getUpdatingSyncMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getWaitingRemoteCompUpdateDoneMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateIsReceived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUpdateRequiredComp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlgorithmRoot(String parentTx, String rootTx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, TransactionContext> getFakeTxs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp,
			String curComp, String rootTx, String parentTx, String subTx) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TxDepMonitor getTxDepMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTxDepMonitor(TxDepMonitor txDepMonitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean initLocalSubTx(String hostComp, String fakeSubTx, String rootTx,
			String rootComp, String parentTx, String parentComp) {
		// TODO Auto-generated method stub
		return false;
	}

}
