package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * For recording:
 * <ul>
 * 		<li>current transaction id</li>
 *  	<li>current transaction's host component</li>
 *  	<li>current transaction's status</li>
 *  	<li>current futureC and pastC</li>
 *  	<li>current transaction's sub-transactions</li>
 * 		<li>a transaction's root/parent transaction/components </li>
 * 		<li>
 * </ul>
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class TransactionContext {
	
	/** current transaction  */
	private String currentTx;
	/** current(host) component */
	private String hostComponent;
	/** root transaction */
	private String rootTx;
	/** root component */
	private String rootComponent;
	/** parent transaction */
	private String parentTx;
	/** parent component */
	private String parentComponent;
	/** TxEventType: 
	 * TransactionStart,TransactionEnd,	FirstRequestService,DependencesChanged
	 */
	private TxEventType eventType = null;
	/** components that will never be used. */
	private Set<String> pastComponents;
	/** components that will be used later. */
	private Set<String> futureComponents;
	/** it's used to identify whether a sub-tx is a fake transaction */
	private boolean isFakeTx = false;

	//	private Map<String, SubTransaction> subTxs;
	/** subTx's host components, it takes sub-tx id as the key */
	private Map<String, String> subTxHostComps;
	/** subTx's statuses, it takes sub-tx id as the key */
	private Map<String, TxEventType> subTxStatuses;
	
//	private TxDepMonitor txDepMonitor;
	
	public TransactionContext(){
		if(subTxHostComps == null)
			subTxHostComps = new HashMap<String, String>();
		if(subTxStatuses == null)
			subTxStatuses = new HashMap<String, TxEventType>();
	}
	
	/**
	 * @return root transaction ID
	 */
	public String getRootTx() {
		return rootTx;
	}

	/**
	 * @param rootTx root transaction ID
	 */
	public void setRootTx(String rootTx) {
		this.rootTx = rootTx;
	}

	/**
	 * @return root transaction's component object identifier
	 */
	public String getRootComponent() {
		return rootComponent;
	}

	/**
	 * @param rootComponent root transaction's component object identifier
	 */
	public void setRootComponent(String rootComponent) {
		this.rootComponent = rootComponent;
	}

	/**
	 * @return parent transaction ID
	 */
	public String getParentTx() {
		return parentTx;
	}

	/**
	 * @param parentTx parent transaction ID
	 */
	public void setParentTx(String parentTx) {
		this.parentTx = parentTx;
	}

	/**
	 * @return parent transaction's component object identifier
	 */
	public String getParentComponent() {
		return parentComponent;
	}

	/**
	 * @param parentComponent parent transaction's component object identifier
	 */
	public void setParentComponent(String parentComponent) {
		this.parentComponent = parentComponent;
	}


	/**
	 * @return current transaction ID
	 */
	public String getCurrentTx() {
		return currentTx;
	}

	/**
	 * @param currentTx current transaction ID
	 */
	public void setCurrentTx(String currentTx) {
		this.currentTx = currentTx;
	}

	/**
	 * @return the hostComponent
	 */
	public String getHostComponent() {
		return hostComponent;
	}

	/**
	 * @param hostComponent the hostComponent to set
	 */
	public void setHostComponent(String hostComponent) {
		this.hostComponent = hostComponent;
	}

	public Set<String> getPastComponents() {
		return pastComponents;
	}

	public void setPastComponents(Set<String> pastComponents) {
		this.pastComponents = pastComponents;
	}

	public Set<String> getFutureComponents() {
		return futureComponents;
	}

	public void setFutureComponents(Set<String> futureComponents) {
		this.futureComponents = futureComponents;
	}
	
	/**
	 * @return key is txID, value is compIdentifier
	 */
	public Map<String, String> getSubTxHostComps() {
		return subTxHostComps;
	}
	
	/**
	 * @return key is txID, value is tx status
	 */
	public Map<String, TxEventType> getSubTxStatuses() {
		return subTxStatuses;
	}

	public TxEventType getEventType() {
		return eventType;
	}

	public void setEventType(TxEventType eventType) {
		this.eventType = eventType;
	}
	
//	/**
//	 * Each tx has one TxDepMonitor
//	 * @return
//	 */
//	public TxDepMonitor getTxDepMonitor() {
//		return txDepMonitor;
//	}
//
//	/**
//	 * Each tx has one TxDepMonitor
//	 */
//	public void setTxDepMonitor(TxDepMonitor txDepMonitor) {
//		this.txDepMonitor = txDepMonitor;
//	}
	
	public boolean isFakeTx() {
		return isFakeTx;
	}

	public void setFakeTx(boolean isFakeTx) {
		this.isFakeTx = isFakeTx;
	}

	@Override
	public String toString() {
		String result = null;
		
		result = "root:" + rootComponent + " " + rootTx + ", " +
				"parent:" + parentComponent + " " + parentTx +", " +
				"current:" + hostComponent + " " + currentTx + " " + eventType + ", " +
				"subTxs:";
		
		for (Entry<String, String> subHostComps : subTxHostComps.entrySet()) {
			result += "\n" + subHostComps.getKey() + " " + subHostComps.getValue() + " " + subTxStatuses.get(subHostComps.getKey());
		}
				
		return result;
	}

	
}
