package cn.edu.nju.moon.conup.pre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;


public class DynamicDependency {
	ComponentListener listener = ComponentListenerImpl.getInstance();
	String transactionid;
	String className;
	String methodName;
	String threadID;
	Set<String> real_past=new ConcurrentSkipListSet<String>();	
	List<SetState> states = new LinkedList<SetState>();
	String [] events;
	int currentState=0;
	static Hashtable<String,DynamicDependency> ddes = new Hashtable<String,DynamicDependency>();
	public DynamicDependency(){
		
	}
	//private  dynamicDependency instance; 
	public static DynamicDependency getInstance(String name){		
		if(ddes.containsKey(name)){
			return ddes.get(name);
		}
		else
		{
			DynamicDependency instance = new DynamicDependency(name);
			ddes.put(name, instance);
			return instance;
		}		
	}
	public Set<String> getFuture(){
		return states.get(currentState).getFuture();
	}
	public Set<String> getRealPast(){
		return real_past;
	}
	/**
	 * 
	 * @param name
	 * transaction id or name
	 */
	private DynamicDependency(String name){
		transactionid = name;
		className = name.split(";")[0];
		className = className.replace("/", ".");
		methodName = name.split(";")[1];
		threadID = name.split(";")[2];
		System.out.println(className+","+methodName);

		try {
			for(Method m : Class.forName(className).getMethods()){
				if(m.isAnnotationPresent(VcTransaction.class)){
					VcTransaction tran = m.getAnnotation(VcTransaction.class);
					String nameAnno = tran.name();					
					if(nameAnno.equals(methodName)){

					String [] stateAnno = tran.states();
					String [] eventAnno = tran.next();

					for(int i=0; i<stateAnno.length;i++){
						String [] si = stateAnno[i].split(",");
						Set<String> stateFuture = new ConcurrentSkipListSet<String>();
						for(int j=0;j< si.length;j++){
			/*				if(si[j].isEmpty()){
								si[j] = "";
							}*/
							stateFuture.add(si[j]);
						}
						SetState state = new SetState();
						state.setFuture(stateFuture);
						states.add(state);
					}
					System.out.println("states:"+states.size());
			//get event information
					events = eventAnno;
				}
				}
			}
		}
		catch(Exception e){
			System.out.println(e.getStackTrace());
		}
		
	}	
	/**
	 * get the real past after the given trigger action dynamically
	 * @param event
	 */
	public void trigger(String id,String event){
//		Set<String> fut = new ConcurrentSkipListSet<String>();
		if(event.contains("Start")){
			currentState = 0;
			if(states.isEmpty()){
				listener.notify("start", threadID, new ConcurrentSkipListSet<String>(), real_past);
				listener.notify("running", threadID, states.get(currentState).getFuture(), real_past);
				System.out.println("Transaction  "+id+"  is start!");
				System.out.println("Event=Start"+"Now,state="+currentState+";Future= "+";Past="+real_past);
			}else{
				listener.notify("start", threadID, states.get(currentState).getFuture(), real_past);
				listener.notify("running", threadID, states.get(currentState).getFuture(), real_past);
				System.out.println("Transaction  "+id+"  is start!");
				System.out.println("Event=Start"+"Now,state="+currentState+";Future="+states.get(currentState).getFuture()+";Past="+real_past);
			}
		}
		else if(event.isEmpty()){
			if(states.isEmpty()){
				listener.notify("end", threadID, new ConcurrentSkipListSet<String>(), real_past);
				System.out.println("This transaction is end!");
				ddes.remove(id);		
			}else{
				listener.notify("end", threadID, states.get(currentState).getFuture(), real_past);
				System.out.println("This transaction is end!");
				ddes.remove(id);			
			}
		}
		else{
		
		if(event.contains("COM")){
			String port = event.split("\\.")[1];				
				real_past.add(port);		
			
		}
		
		String eve = events[currentState];
		String eveinf[] = eve.split(",");
		for(String e: eveinf){
			if(e.contains(event))
			{
				currentState = Integer.parseInt(e.split("-")[1]);
				listener.notify("running", threadID, states.get(currentState).getFuture(), real_past);
				System.out.println("Event="+e.split("-")[0]+";Now,state="+currentState+";Future="+states.get(currentState).getFuture()+";Past="+real_past);
				return;
			}
			
		}
		System.out.println("The input event is wrong!");
	 }
		
	}
	public static void main(String args[]){
		String name="cn.edu.nju.moon.vc.pre.Test;execute;1123";
		
		DynamicDependency.getInstance(name).trigger(name, "Start");
		DynamicDependency.getInstance(name).trigger(name, "COM.AuthComponent/TokenService.25");
		DynamicDependency.getInstance(name).trigger(name, "COM.ProcComponent/ProcService.37");
		DynamicDependency.getInstance(name).trigger(name, "");
//		DynamicDependency.getInstance("accessServices").trigger("accessServices", "");
/*		dynamicDependency.getInstance("accessServices").trigger("accessServices", "COM.c.22");
		dynamicDependency.getInstance("accessServices").trigger("accessServices", "while.F.28");
		dynamicDependency.getInstance("accessServices").trigger("accessServices", "COM.a.33");
		dynamicDependency.getInstance("accessServices").trigger("accessServices", "COM.b.46");
		dynamicDependency.getInstance("accessServices").trigger("accessServices", "");*/
	}
	

}
