package cn.edu.nju.moon.conup.spi.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.Dependence;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;

/**
 * @author rgc
 * @version Dec 1, 2012 9:12:27 PM
 */
public class Printer {
	private final static Logger LOGGER = Logger.getLogger(Printer.class.getName());
	public static Logger getLogger() {
		return LOGGER;
	}
	
	public void printDeps(Set<Dependence> deps, String type){
//		LOGGER.info("Deps:" + type);
		String result = "Deps:" + type + "\n\t";
		for (Iterator<Dependence> iterator = deps.iterator(); iterator.hasNext();) {
			Dependence dependence = (Dependence) iterator.next();
			result += dependence + "\n\t";
		}
		LOGGER.fine(result);
	}
	
	public void printTxs(Map<String, TransactionContext> txs){
//		LOGGER.info("Txs:");
		Iterator<Entry<String, TransactionContext>> txsIterator = txs.entrySet().iterator();
		String result = "Txs:\n\t";
		while(txsIterator.hasNext()){
			Entry<String, TransactionContext> entry = txsIterator.next();
//			String txID = entry.getKey();
			TransactionContext tc = entry.getValue();
			result += tc.toString() + "\n\t";
		}
		LOGGER.fine(result);
	}
	
	public void printTxs(Logger LOGGER, Map<String, TransactionContext> txs){
		Iterator<Entry<String, TransactionContext>> txsIterator = txs.entrySet().iterator();
		String result = "Txs:\n\t";
		while(txsIterator.hasNext()){
			Entry<String, TransactionContext> entry = txsIterator.next();
//			String txID = entry.getKey();d
			TransactionContext tc = entry.getValue();
			result += tc.toString() + "\n\t";
		}
		LOGGER.fine(result);
	}
}
