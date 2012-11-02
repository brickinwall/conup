package cn.edu.nju.moon.conup.ext.ddm;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class SetState {
	
	
	Set<String> future = new ConcurrentSkipListSet<String>();
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
	public static String printSet(Set<String> str){
		String strTemp="";
		for(String s : str){
			strTemp = s + ",";
		}
		return strTemp;
	}

}