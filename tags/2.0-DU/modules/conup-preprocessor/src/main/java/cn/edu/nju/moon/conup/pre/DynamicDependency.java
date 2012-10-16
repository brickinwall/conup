package cn.edu.nju.moon.conup.pre;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;

public class DynamicDependency {
	private final static Logger LOGGER = Logger.getLogger(DynamicDependency.class.getName());
	public static Logger getLogger() {
		return LOGGER;
	}

	ComponentListener listener = null;
	String transactionid;
	String className;
	String methodName;
	String threadID;
	Set<String> real_past = new ConcurrentSkipListSet<String>();
	List<SetState> states = new LinkedList<SetState>();
	String[] events;
	int currentState = 0;
	static Hashtable<String, DynamicDependency> ddes = new Hashtable<String, DynamicDependency>();

	public DynamicDependency() {

	}

	// private dynamicDependency instance;
	public static DynamicDependency getInstance(String name,String states,String nexts) {
		if (ddes.containsKey(name)) {
			return ddes.get(name);
		} else {
			DynamicDependency instance = new DynamicDependency(name,states,nexts);
			ddes.put(name, instance);
			return instance;
		}
	}

	public Set<String> getFuture() {
		return states.get(currentState).getFuture();
	}

	public Set<String> getRealPast() {
		return real_past;
	}

	/**
	 * 
	 * @param name
	 *            transaction id or name
	 */
	private DynamicDependency(String name,String statesDDA,String nextsDDA) {
		transactionid = name;
		className = name.split(";")[0];
		className = className.replace("/", ".");		
		methodName = name.split(";")[1];
		threadID = name.split(";")[2];

						String[] stateAnno = statesDDA.split(";");
						String[] eventAnno = nextsDDA.split(";");
						LOGGER.fine("---------states:" + states.size());
						for (int i = 0; i < stateAnno.length; i++) {
							String[] si = stateAnno[i].split(",");
							Set<String> stateFuture = new ConcurrentSkipListSet<String>();
							for (int j = 0; j < si.length; j++) {
								 if(si[j].equals("_E")) { 
									 si[j] = ""; 									 
								}
//								System.out.print(si[j]+"   ");
								stateFuture.add(si[j]);
							}
							SetState state = new SetState();
							state.setFuture(stateFuture);
							states.add(state);
						}
						// get event information
						events = new String[eventAnno.length];
						for(int i = 0; i< eventAnno.length; i++){
							if(eventAnno[i].equals("_E")){
								eventAnno[i]="";
							}
							events[i] = eventAnno[i];
						}
	}

	public void notifyRun() {
		listener = new ComponentListenerImpl();
		if (states.isEmpty()) {
			listener.notify("running", threadID,
					new ConcurrentSkipListSet<String>(), real_past);
		} else
			listener.notify("running", threadID, states.get(currentState)
					.getFuture(), real_past);
	}

	/**
	 * get the real past after the given trigger action dynamically
	 * 
	 * @param event
	 */
	public void trigger(String id, String event) {
		listener = new ComponentListenerImpl();		
		if (event.contains("Start")) {
			currentState = 0;
			if (states.isEmpty()) {
				listener.notify("start", threadID,	new ConcurrentSkipListSet<String>(), real_past);
				LOGGER.fine("Transaction  " + id + "  is start!");
				LOGGER.fine("Event=Start" + "Now,state is empty;Future =[];Past=" + real_past);
			} else {
				LOGGER.fine("Transaction  " + id + "  is start!");
				LOGGER.fine("Event=Start" + "Now,state=" + currentState+ ";Future=" + states.get(currentState).getFuture()
						+ ";Past=" + real_past);
				listener.notify("start", threadID, states.get(currentState).getFuture(), real_past);
/*				if(states.get(currentState).getFuture().isEmpty()){
					listener.notify("start", threadID, new ConcurrentSkipListSet<String>(), real_past);
				}
				else{
					listener.notify("start", threadID, states.get(currentState).getFuture(), real_past);
				}
*/
				// listener.notify("running", threadID,
				// states.get(currentState).getFuture(), real_past);
				
			}
		} else if (event.isEmpty()) {
			if (states.isEmpty()) {
				listener.notify("end", threadID,new ConcurrentSkipListSet<String>(), real_past);
				LOGGER.fine("This transaction is end!state is empty!!");
				ddes.remove(id);
			} else {
				listener.notify("end", threadID, states.get(currentState).getFuture(), real_past);
				LOGGER.fine("This transaction is end!");
				ddes.remove(id);
			}
		} else {

			if (event.contains("COM")) {
				String port = event.split("\\.")[1];
				real_past.add(port);

			}

			String eve = events[currentState];
			String eveinf[] = eve.split(",");
			for (String e : eveinf) {
				if (e.contains(event)) {
					currentState = Integer.parseInt(e.split("-")[1]);
					if(states.isEmpty()){
						listener.notify("running", threadID, new ConcurrentSkipListSet<String>(), real_past);
						LOGGER.fine("Event=" + e.split("-")[0]
								+ ";Now,state is empty;Future=[];Past="
								+ real_past);
					}else{
					listener.notify("running", threadID,states.get(currentState).getFuture(), real_past);
					LOGGER.fine("Event=" + e.split("-")[0] + ";Now,state=" + currentState + ";Future=" + states.get(currentState).getFuture() + ";Past="	+ real_past);}
					return;
				}

			}
			LOGGER.fine("The input event is wrong!");
		}

	}

	public static void main(String args[]) {
/*		String name = "cn.edu.nju.moon.vc.pre.Test;execute;1123";

		DynamicDependency.getInstance(name).trigger(name, "Start");
		DynamicDependency.getInstance(name).trigger(name,
				"COM.AuthComponent/TokenService.25");
		DynamicDependency.getInstance(name).trigger(name,
				"COM.ProcComponent/ProcService.37");
		DynamicDependency.getInstance(name).trigger(name, "");*/
		// DynamicDependency.getInstance("accessServices").trigger("accessServices",
		// "");
		/*
		 * dynamicDependency.getInstance("accessServices").trigger("accessServices"
		 * , "COM.c.22");
		 * dynamicDependency.getInstance("accessServices").trigger
		 * ("accessServices", "while.F.28");
		 * dynamicDependency.getInstance("accessServices"
		 * ).trigger("accessServices", "COM.a.33");
		 * dynamicDependency.getInstance
		 * ("accessServices").trigger("accessServices", "COM.b.46");
		 * dynamicDependency
		 * .getInstance("accessServices").trigger("accessServices", "");
		 */
		 DynamicDependency.getInstance("aa", "_E;_E;_E;_E", "if.0.24-1,if.1.24-3;if.0.32-2,if.1.32-3;_E;_E").trigger("aa", "Start");
		 DynamicDependency.getInstance("aa", "_E;_E;_E;_E", "if.0.24-1,if.1.24-3;if.0.32-2,if.1.32-3;_E;_E").trigger("aa", "if.0.24");
		 DynamicDependency.getInstance("aa", "_E;_E;_E;_E", "if.0.24-1,if.1.24-3;if.0.32-2,if.1.32-3;_E;_E").trigger("aa", "if.0.32");
		 DynamicDependency.getInstance("aa", "_E;_E;_E;_E", "if.0.24-1,if.1.24-3;if.0.32-2,if.1.32-3;_E;_E").trigger("aa", "");
	
	}

}
