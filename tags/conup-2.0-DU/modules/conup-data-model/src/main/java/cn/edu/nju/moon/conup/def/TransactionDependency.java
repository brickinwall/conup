package cn.edu.nju.moon.conup.def;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransactionDependency {
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
	/** transaction status: idle, start, running, end. */
	private String status = TransactionSnapshot.IDLE;
	/** transactions that will never be used. */
	private Set<String> pastComponents;
	/** transactions that will be used later. */
	private Set<String> futureComponents;
	/** current tx's subTx */
	private Map<String, SubTransaction> subTxs;
	
	public TransactionDependency(){
		if(subTxs == null)
			subTxs = new HashMap<String, SubTransaction>();
	}
	
//	public TransactionDependency(String root, String parent, String current, String hostComponentName){
//		this.root = root;
//		this.parent = parent;
//		this.current = current;
//	}
	

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
	 * @return root transaction's component
	 */
	public String getRootComponent() {
		return rootComponent;
	}

	/**
	 * @param rootComponent root transaction's component
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
	 * @return parent transaction's component
	 */
	public String getParentComponent() {
		return parentComponent;
	}

	/**
	 * @param parentComponent parent transaction's component
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

	/** a transaction's status can be idle, start, running and end */
	public String getStatus() {
		return status;
	}

	/** a transaction's status can be idle, start, running and end */
	public void setStatus(String status) {
		this.status = status;
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
	
	/** get current transaction's sub-transactions */
	public Map<String, SubTransaction> getSubTxs() {
		return subTxs;
	}
	
	public SubTransaction getSubTx(String subTxID){
		return subTxs.get(subTxID);
	}

	/** set current transaction's sub-transactions */
	public void setSubTx(Map<String, SubTransaction> subTxs) {
		this.subTxs = subTxs;
	}

}
