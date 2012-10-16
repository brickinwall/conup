package cn.edu.nju.moon.conup.printer.container;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

import cn.edu.nju.moon.conup.data.ArcRegistry;
import cn.edu.nju.moon.conup.data.TransactionRegistry;
import cn.edu.nju.moon.conup.def.Arc;
import cn.edu.nju.moon.conup.def.SubTransaction;
import cn.edu.nju.moon.conup.def.TransactionDependency;

public class ContainerPrinter {
	
	private final static Logger LOGGER = Logger.getLogger(ContainerPrinter.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	public void printInArcRegistry(ArcRegistry arcRegistry){
		printArcRegistry("InArcRegistry", arcRegistry);
		System.out.println();
		
	}
	
	public void printOutArcRegistry(ArcRegistry arcRegistry){
		printArcRegistry("OutArcRegistry", arcRegistry);
		System.out.println();
		
	}
	
	public void printTransactionRegistry(TransactionRegistry registry){
		LOGGER.setLevel(Level.FINE);
		LOGGER.fine("** TransactionRegistry: " + registry.hashCode());
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txIterator = registry.getDependencies().entrySet().iterator();
		Entry<String, TransactionDependency> tmpEntry;
		while(txIterator.hasNext()){
			tmpEntry = txIterator.next();
			String subTxInfos = null;
			for(Entry subEntry : tmpEntry.getValue().getSubTxs().entrySet()){
				SubTransaction subTx = (SubTransaction)subEntry.getValue();
				String tmp = "\n\t\t\t" + "subTxID=" + subEntry.getKey() + ", subTxHost=" + subTx.getSubTxHost() + ", subTxStatus=" + subTx.getSubTxStatus();
				subTxInfos += tmp;
			}
			subTxInfos += "\n\t---------------------------------------------";
			LOGGER.fine("\t" + "currentTx=" + tmpEntry.getKey() + ", host=" + tmpEntry.getValue().getHostComponent() + ", txStatus=" + tmpEntry.getValue().getStatus()
					+ "\n\t\t" + "currentTx=" + tmpEntry.getValue().getCurrentTx()
					+ "\n\t\t" + "futureC.size()=" + tmpEntry.getValue().getFutureComponents().size()
					+ "\n\t\t" + "pastC.size()=" + tmpEntry.getValue().getPastComponents().size()
					+ "\n\t\t" + "rootTx=" + tmpEntry.getValue().getRootTx() + ", rootComponent=" + tmpEntry.getValue().getRootComponent()
					+ "\n\t\t" + "parentTx=" + tmpEntry.getValue().getParentTx() + ", parentComponent=" + tmpEntry.getValue().getParentComponent()
					+ "\n\t\t" + "sub transactions:"
					+ subTxInfos);
			
		}//END FOR
		
		System.out.println();
	}
	
	public void printArcRegistry(String regsitryType, ArcRegistry arcRegistry){
		LOGGER.setLevel(Level.INFO);
		Set<Arc> arcs = arcRegistry.getArcs();
		Iterator<Arc> arcIterator = arcs.iterator();
		Arc arc;
		String arcInfos = new String();
		String tmp = "** " + regsitryType;
		arcInfos += tmp;
		while(arcIterator.hasNext()){
			arc = arcIterator.next();
			if(arc.getType().equals(Arc.PAST)){
				tmp = "\n\t" + arc.getType() + "  [" + arc.getSourceComponent() + "-------RootTx:" + arc.getRootTransaction() + "------->" + arc.getTargetComponent() +"]";
			}else{
				tmp = "\n\t" + arc.getType() + "[" + arc.getSourceComponent() + "-------RootTx:" + arc.getRootTransaction() + "------->" + arc.getTargetComponent() +"]";
			}
			arcInfos += tmp;
		}
		LOGGER.info(arcInfos);
	}

}
