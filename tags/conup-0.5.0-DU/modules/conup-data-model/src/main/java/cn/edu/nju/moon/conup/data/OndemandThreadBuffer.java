package cn.edu.nju.moon.conup.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


public class OndemandThreadBuffer {
	private static OndemandThreadBuffer instance = new OndemandThreadBuffer();
	private Set<Runnable> threads = new HashSet<Runnable>();
	
	private OndemandThreadBuffer(){
		
	}
	
	public static OndemandThreadBuffer getInstance(){
		return instance;
	}

	/**
	 * @return the threads
	 */
	public Set<Runnable> getThreads() {
		return threads;
	}

	/**
	 * @param threads the threads to set
	 */
	public void setThreads(Set<Runnable> threads) {
		this.threads = threads;
	}
	
	
}
