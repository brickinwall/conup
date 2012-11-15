package cn.edu.nju.moon.conup.ext.ddm;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/**
 * 
 * @author Ping Su <njupsu@gmail.com>
 *
 */
public class SetState {
	
	
	Set<String> future = new ConcurrentSkipListSet<String>();
	/**
	 * past is not used now, it maybe used in future.
	 */
	Set<String> past = new ConcurrentSkipListSet<String>();

	public SetState(){
		
	}
	public void setFuture(Set<String> future) {
		this.future = future;
	}

	public void setPast(Set<String> past) {
		this.past = past;
	}

	public Set<String> getFuture() {
		return future;
	}

	public Set<String> getPast() {
		return past;
	}	

	public void addFuture(String f){
		future.add(f);
	}
	
	public void addPast(String p){
		past.add(p);
	}
	
}