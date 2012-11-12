/**
 * 
 */
package cn.edu.nju.moon.conup.core.manager.impl;

import cn.edu.nju.moon.conup.core.DependenceRegistryImpl;
import cn.edu.nju.moon.conup.core.TransactionRegistry;
import cn.edu.nju.moon.conup.spi.datamodel.Algorithm;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.ComponentObject;
import cn.edu.nju.moon.conup.spi.datamodel.Scope;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.factory.AlgorithmFactory;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;

/**
 * For managing/maintaining transactions and dependences
 * @author Jiang Wang <jiang.wang88@gmail.com>
 * 
 */
public class DynamicDepManagerImpl implements DynamicDepManager {
	private Algorithm algorithm = null;
	private ComponentObject compObj;
	private CompStatus compStatus = null;

	private DependenceRegistryImpl inDepRegistry = new DependenceRegistryImpl();
	private DependenceRegistryImpl outDepRegistry = new DependenceRegistryImpl();
	private TransactionRegistry txRegistry = TransactionRegistry.getInstance();

	public DynamicDepManagerImpl() {
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
		String currentTxID = txContext.getCurrentTx();
		if (!txRegistry.contains(currentTxID)) {
			txRegistry.addTransactionContext(currentTxID, txContext);
		} else {
			// if this tx id already in txRegistry, update it...
			txRegistry.updateTransactionContext(currentTxID, txContext);
		}

		return manageDependence(txContext);
	}

	/**
	 * maintain dependences, e.g., arcs
	 * @param txStatus
	 * @param txID
	 * @param futureC
	 * @param pastC
	 * @return
	 */
	@Override
	public boolean manageDependence(TransactionContext txContext) {
		algorithm.manageDependence(txContext);
		return true;
	}

	@Override
	public boolean isInterceptRequired() {
		return false;
	}

	@Override
	public boolean manageDependence(String proctocol, String msgType, String payload) {
		return false;
	}

	@Override
	public boolean isValid() {

		return false;
	}

	@Override
	public boolean isReadyForUpdate() {
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

	public DependenceRegistryImpl getInDepRegistry() {
		return inDepRegistry;
	}

	public void setInDepRegistry(DependenceRegistryImpl inDepRegistry) {
		this.inDepRegistry = inDepRegistry;
	}

	public DependenceRegistryImpl getOutDepRegistry() {
		return outDepRegistry;
	}

	public void setOutDepRegistry(DependenceRegistryImpl outDepRegistry) {
		this.outDepRegistry = outDepRegistry;
	}

	@Override
	public void setCompObject(ComponentObject compObj) {
		this.compObj = compObj;
	}

	@Override
	public void setAlgorithm(String algorithmType) {
		algorithm = new AlgorithmFactory().createAlgorithm(compObj.getAlgorithmConf());
	}

	public CompStatus getCompStatus() {
		return compStatus;
	}

	public void setCompStatus(CompStatus compStatus) {
		synchronized (this) {
			this.compStatus = compStatus;
		}
	}

}
