package cn.edu.nju.moon.conup.listener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class State {
	
	int loc;
	List<String> future = new LinkedList<String>();
	List<String> past = new LinkedList<String>();

	public State(){
		future =null;
		past = null;
	}
	
	public State(int l) {
		this.loc = l;		
	}

	public void setLoc(int l) {
		this.loc = l;
	}
	public int getLoc() {
		return loc;
	}

	public void setFuture(List<String> future) {
		this.future = future;
	}

	public void setPast(List<String> past) {
		this.past = past;
	}

	public List<String> getFuture() {
		return future;
	}

	public List<String> getPast() {
		return past;
	}	

	public void addFuture(String f){
		future.add(f);
	}
	
	public void addPast(String p){
		past.add(p);
	}

}
