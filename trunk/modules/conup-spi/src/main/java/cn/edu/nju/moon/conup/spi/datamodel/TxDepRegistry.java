package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:04:41 PM
 */
public class TxDepRegistry {

	/** take txId as key */
	private Map<String, TxDep> txDeps;
	
	public TxDepRegistry(){
		txDeps = new ConcurrentHashMap<String, TxDep>();
	}
	
	public TxDep getLocalDep(String txId){
		return txDeps.get(txId);
	}
	
	public void addLocalDep(String txId, TxDep txDep){
		if(!contains(txId))
			txDeps.put(txId, txDep);
		else{
			updateLocalDep(txId, txDep);
		}
	}
	
	public void removeLocalDep(String txId){
		if(contains(txId))
			txDeps.remove(txId);
	}
	
	public boolean contains(String txId){
		return txDeps.containsKey(txId);
	}
	
	private void updateLocalDep(String txId, TxDep txDep){
		txDeps.remove(txId);
		txDeps.put(txId, txDep);
	}

}
