package cn.edu.nju.moon.conup.core.manager.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.core.DependenceRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.InvocationContext;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.datamodel.TxDepRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.DepOperationType;
import cn.edu.nju.moon.conup.spi.utils.DepPayloadResolver;
import cn.edu.nju.moon.conup.spi.utils.DepPayload;
import cn.edu.nju.moon.conup.spi.utils.Printer;

/**
 * For managing/maintaining transactions and dependences
 * @author JiangWang<jiang.wang88@gmail.com>
 * 
 */
public class DynamicDepManagerImpl implements DynamicDepManager {
	private Algorithm algorithm = null;
	
	private CompLifeCycleManager compLifeCycleMgr = null;
	private ComponentObject compObj;
	/** dependences by other components */
	private DependenceRegistry inDepRegistry = new DependenceRegistry();
	private Logger LOGGER = Logger.getLogger(DynamicDepManagerImpl.class.getName());
	/** dependences to other components */
	private DependenceRegistry outDepRegistry = new DependenceRegistry();
	
	private Scope scope = null;
	
	private TxLifecycleManager txLifecycleMgr = null;
	
//	private TransactionRegistry txRegistry = null;

	public DynamicDepManagerImpl() {
	}

	@Override
	public void dependenceChanged(String hostComp) {
		if(compObj.isTargetComp()){
			UpdateManager updateMgr = NodeManager.getInstance().getUpdateManageer(compObj.getIdentifier());
			updateMgr.checkFreeness(hostComp);
		}
	}
	
	@Override
	public void dynamicUpdateIsDone() {
		algorithm.updateIsDone(compObj.getIdentifier(), this);
	}
	
	@Override
	public Set<String> getAlgorithmOldVersionRootTxs() {
		return algorithm.getOldVersionRootTxs(inDepRegistry.getDependences());
	}

	@Override
	public ComponentObject getCompObject() {
		return compObj;
	}

	public DependenceRegistry getInDepRegistry() {
		return inDepRegistry;
	}

	public DependenceRegistry getOutDepRegistry() {
		return outDepRegistry;
	}

	@Override
	public Set<Dependence> getRuntimeDeps() {
		return outDepRegistry.getDependences();
	}

	@Override
	public Set<Dependence> getRuntimeInDeps() {
		return inDepRegistry.getDependences();
	}

	@Override
	public Scope getScope() {
		return scope;
	}
	
	@Override
	public Set<String> getStaticDeps() {
		return compObj.getStaticDeps();
	}
	
	@Override
	public Set<String> getStaticInDeps() {
		return compObj.getStaticInDeps();
	}

	public TxLifecycleManager getTxLifecycleMgr() {
		if(txLifecycleMgr == null){
			txLifecycleMgr = NodeManager.getInstance().getTxLifecycleManager(compObj.getIdentifier());
		}
		assert txLifecycleMgr != null;
		return txLifecycleMgr;
	}

	
	@Override
	public Map<String, TransactionContext> getTxs() {
//		return txRegistry.getTransactionContexts();
		assert txLifecycleMgr != null;
		return txLifecycleMgr.getTxs();
	}

	@Override
	public boolean initLocalSubTx(TransactionContext txContext) {
		return algorithm.initLocalSubTx(txContext, compLifeCycleMgr, this);
	}

	@Override
	public boolean isBlockRequiredForFree(Set<String> algorithmOldVersionRootTxs,
			TransactionContext txContext, boolean isUpdateReqRCVD) {
		return algorithm.isBlockRequiredForFree(algorithmOldVersionRootTxs, txContext, isUpdateReqRCVD, this);
	}
	
	@Override
	public boolean isReadyForUpdate() {
		boolean algReadyForUpdate = algorithm.readyForUpdate(compObj.getIdentifier(), this);
		LOGGER.info("algReadyForUpdate:" + algReadyForUpdate + " compStatus.equals(CompStatus.VALID): " + compLifeCycleMgr.getCompStatus().equals(CompStatus.VALID));
		return algReadyForUpdate;
	}

	@Override
	public boolean manageDependence(String payload) {
		DepPayloadResolver plResolver;
		plResolver = new DepPayloadResolver(payload);
		DepOperationType operationType = plResolver.getOperation();
		Map<String, String> params = getParamFromPayload(plResolver);
		
		return algorithm.manageDependence(operationType, params, this, compLifeCycleMgr);
	}
	
	/**
	 * maintain dependences, e.g., arcs
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	private boolean manageDependence(TransactionContext txContext) {
		LOGGER.fine("DynamicDepManagerImpl.manageDependence(...)");
		
		algorithm.manageDependence(txContext, this, compLifeCycleMgr);
		return true;
	}

	/**
	 * maintain tx
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	@Override
	public boolean manageTx(TransactionContext txContext) {
		LOGGER.fine("DynamicDepManagerImpl.manageTx(...)");
		String currentTxID = txContext.getCurrentTx();
		
		txLifecycleMgr.updateTxContext(currentTxID, txContext);
		
//		if (!txRegistry.contains(currentTxID)) {
//			txRegistry.addTransactionContext(currentTxID, txContext);
//		} else {
//			// if this tx id already in txRegistry, update it...
//			txRegistry.updateTransactionContext(currentTxID, txContext);
//		}
		
		return manageDependence(txContext);
	}

	@Override
	public void ondemandSetupIsDone() {
		
		//FOR TEST
		String inDepsStr = "";
		for (Dependence dep : inDepRegistry.getDependences()) {
			inDepsStr += "\n" + dep.toString();
		}
		LOGGER.info("ondemandSetupIsDone, inDepsStr:" + inDepsStr);
		
		String outDepsStr = "";
		for (Dependence dep : outDepRegistry.getDependences()) {
			outDepsStr += "\n" + dep.toString();
		}
		LOGGER.info("ondemandSetupIsDone, outDepsStr:" + outDepsStr);

		Printer printer = new Printer();
		LOGGER.info("ondemandSetupIsDone, Txs:");
		printer.printTxs(LOGGER, getTxs());
				
		algorithm.initiate(compObj.getIdentifier(), this);
	}
	
	@Override
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public void setCompLifeCycleMgr(CompLifeCycleManager compLifeCycleMgr) {
		this.compLifeCycleMgr = compLifeCycleMgr;
	}
	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	public void setInDepRegistry(DependenceRegistry inDepRegistry) {
		this.inDepRegistry = inDepRegistry;
	}

	public void setOutDepRegistry(DependenceRegistry outDepRegistry) {
		this.outDepRegistry = outDepRegistry;
	}

	@Override
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	@Override
	public void setTxLifecycleMgr(TxLifecycleManager txLifecycleMgr) {
		this.txLifecycleMgr = txLifecycleMgr;
//		this.txRegistry = txLifecycleMgr.getTxRegistry();
	}

	private Map<String, String> getParamFromPayload(DepPayloadResolver plResolver) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("srcComp", plResolver.getParameter(DepPayload.SRC_COMPONENT));
		params.put("targetComp", plResolver.getParameter(DepPayload.TARGET_COMPONENT));
		params.put("rootTx", plResolver.getParameter(DepPayload.ROOT_TX));
		params.put("parentTx", plResolver.getParameter(DepPayload.PARENT_TX));
		params.put("subTx", plResolver.getParameter(DepPayload.SUB_TX));
		return params;
	}

//	@Override
//	public boolean notifySubTxStatus(TxEventType subTxStatus, String subComp, String curComp, String rootTx,
//			String parentTx, String subTx) {
//		return algorithm.notifySubTxStatus(subTxStatus, subComp, curComp, rootTx, parentTx, subTx);
//	}

	@Override
	public boolean notifySubTxStatus(TxEventType subTxStatus,
			InvocationContext invocationCtx, CompLifeCycleManager compLifeCycleMgr, String proxyRootTxId) {
		return algorithm.notifySubTxStatus(subTxStatus, invocationCtx, compLifeCycleMgr, this, proxyRootTxId);
	}
}
