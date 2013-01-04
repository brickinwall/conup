package cn.edu.nju.moon.conup.spi.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * As to a Dependence, we care about four properties:
 * <ul>
 * 	<li>source component
 * 	<li>target component
 * 	<li>root transaction
 * 	<li>dependence type, e.g., future/past/static
 * </ul>
 * 
 * For a specific Dependence, its type changes frequently, and should always in the order of
 * create, remove, otherwise a error occurs.
 * 
 * The DepRecorder is used to record the actions on the dependence type. A creation or removal
 * of a dependence is regarded as a action. 
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class DepRecorder {
	private static DepRecorder depRecorder = new DepRecorder();
	/** 
	 * key: source_component#target_component#rootTx 
	 * 	value: action sequences on the dependence type
	 */
	private Map<String, List<String>> deps = new ConcurrentSkipListMap<String, List<String>>();
	/**  */
	public static final String SEPERATOR = "_";
	/** creation action */
	public static final String CREATION = "CREATION";
	/** removal action */
	public static final String REMOVAL = "REMOVAL";
	
	private DepRecorder(){
	}
	
	public static DepRecorder getInstance(){
		return depRecorder;
	}
	
	public void addAction(String key, String action){
		if(deps.get(key) == null){
			List<String> list = new ArrayList<String>();
			list.add(action);
			deps.put(key, list);
		}  else{
			List<String> list = deps.get(key);
			list.add(action);
		}
	}
	
//	public void addAllDeps(String key, List<String> dep){
//		if(deps.get(key) == null){
//			List<String> list = new ArrayList<String>();
//			list.addAll(dep);
//			deps.put(key, list);
//		}  else{
//			List<String> list = deps.get(key);
//			list.addAll(dep);
//		}
//	}
//	
//	public void setDeps(String key, List<String> dep){
//		deps.put(key, dep);
//	}
	
	public void printRecorder(){
		Iterator<Entry<String, List<String>>> iterator;
		iterator = deps.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, List<String>> entry = iterator.next();
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

}
