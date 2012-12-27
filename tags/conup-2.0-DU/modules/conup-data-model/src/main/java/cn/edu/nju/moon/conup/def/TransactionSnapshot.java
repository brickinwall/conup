package cn.edu.nju.moon.conup.def;

import java.util.Set;

/** shared data format */
public class TransactionSnapshot {
	public static String IDLE = "idle";
	public static String START = "start";
	public static String RUNNING = "running";
	public static String END = "end";
	
	/** transaction status: idle, start, running, end. */
	private String transactionStatus = TransactionSnapshot.IDLE;
	
	/** current transaction  */
	private String current;
	
	/** root transaction */
	private String root;
	
	/** transactions that will never be used. */
	private Set<String> pastComponents;
	
	/** transactions that will be used later. */
	private Set<String> futureComponents;
	
	public String getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
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
	
	
	
	
}
