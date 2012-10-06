package cn.edu.nju.moon.conup.printer.container;

import java.util.Iterator;
import java.util.Map.Entry;
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
		LOGGER.info("** InArcRegistry:"	+ arcRegistry.hashCode());
//		System.out.println("InArcRegistry:" + arcRegistry.hashCode());
		printArcRegistry(arcRegistry);
		System.out.println();
		
	}
	
	public void printOutArcRegistry(ArcRegistry arcRegistry){
		LOGGER.info("** OutArcRegistry:"	+ arcRegistry.hashCode());
//		System.out.println("OutArcRegistry:" + arcRegistry.hashCode());
		printArcRegistry(arcRegistry);
		System.out.println();
		
	}
	
	public void printTransactionRegistry(TransactionRegistry registry){
//		System.out.println("TransactionRegistry:" + registry.hashCode());
		LOGGER.info("** TransactionRegistry: " + registry.hashCode());
		Iterator<Entry<String, TransactionDependency>> txIterator;
		txIterator = registry.getDependencies().entrySet().iterator();
		Entry<String, TransactionDependency> tmpEntry;
		while(txIterator.hasNext()){
			tmpEntry = txIterator.next();
//		for(Entry<String, TransactionDependency> tmpEntry : registry.getDependencies().entrySet()){
			
			
//			System.out.println("\t" + "currentTx=" + tmpEntry.getKey() + 
//					", host=" + tmpEntry.getValue().getHostComponent() +
//					", txStatus=" + tmpEntry.getValue().getStatus() );
//			System.out.println("\t\t" + "currentTx=" + tmpEntry.getValue().getCurrentTx() );
//			System.out.println("\t\t" + "futureC.size()=" + tmpEntry.getValue().getFutureComponents().size() );
//			System.out.println("\t\t" + "pastC.size()=" + tmpEntry.getValue().getPastComponents().size() );
//			System.out.println("\t\t" + "rootTx=" + tmpEntry.getValue().getRootTx() +
//					", rootComponent=" + tmpEntry.getValue().getRootComponent() );
//			System.out.println("\t\t" + "parentTx=" + tmpEntry.getValue().getParentTx() +
//					", parentComponent=" + tmpEntry.getValue().getParentComponent() );
//			System.out.println("\t\t" + "sub transactions:");
			String subTxInfos = null;
			for(Entry subEntry : tmpEntry.getValue().getSubTxs().entrySet()){
				SubTransaction subTx = (SubTransaction)subEntry.getValue();
				String tmp = "\n\t\t\t" + "subTxID=" + subEntry.getKey() + ", subTxHost=" + subTx.getSubTxHost() + ", subTxStatus=" + subTx.getSubTxStatus();
				subTxInfos += tmp;
//				System.out.println("\t\t\t" + 
//					"subTxID=" + subEntry.getKey() +
//					", subTxHost=" + subTx.getSubTxHost() +
//					", subTxStatus=" + subTx.getSubTxStatus());
			}
			subTxInfos += "\n\t---------------------------------------------";
			LOGGER.info("\t" + "currentTx=" + tmpEntry.getKey() + ", host=" + tmpEntry.getValue().getHostComponent() + ", txStatus=" + tmpEntry.getValue().getStatus()
					+ "\n\t\t" + "currentTx=" + tmpEntry.getValue().getCurrentTx()
					+ "\n\t\t" + "futureC.size()=" + tmpEntry.getValue().getFutureComponents().size()
					+ "\n\t\t" + "pastC.size()=" + tmpEntry.getValue().getPastComponents().size()
					+ "\n\t\t" + "rootTx=" + tmpEntry.getValue().getRootTx() + ", rootComponent=" + tmpEntry.getValue().getRootComponent()
					+ "\n\t\t" + "parentTx=" + tmpEntry.getValue().getParentTx() + ", parentComponent=" + tmpEntry.getValue().getParentComponent()
					+ "\n\t\t" + "sub transactions:"
					+ subTxInfos);
//			System.out.println("\t" + "---------------------------------------------");
			
		}//END FOR
		
		System.out.println();
	}
	
	public void printArcRegistry(ArcRegistry arcRegistry){
		Set<Arc> arcs = arcRegistry.getArcs();
		Iterator<Arc> arcIterator = arcs.iterator();
		Arc arc;
		String arcInfos = new String();
		while(arcIterator.hasNext()){
			arc = arcIterator.next();
			String tmp = null;
			if(arc.getType().equals(Arc.FUTURE)){
				tmp = "\n\t" + arc.getType() + "[" + arc.getSourceComponent() + "-------RootTx:" + arc.getRootTransaction() + "------->" + arc.getTargetComponent() +"]";
			}else{
				tmp = "\n\t" + arc.getType() + "  [" + arc.getSourceComponent() + "-------RootTx:" + arc.getRootTransaction() + "------->" + arc.getTargetComponent() +"]";
			}
//			String tmp = "\n\t" + "ArcType: " + arc.getType() +
//					"\n\t" + "RootTransaction: " + arc.getRootTransaction() +
//					"\n\t" + "Source: " + arc.getSourceComponent() + "." + arc.getSourceService() +
//					"\n\t" + "Target: " + arc.getTargetComponent() + "." + arc.getTargetService() +
//					"\n\t" + "---------------------------------------------";
			arcInfos += tmp;
//		for(Arc arc : arcRegistry.getArcs()){
//			LOGGER.info("\t" + "ArcType: " + arc.getType() +
//					"\n\t" + "RootTransaction: " + arc.getRootTransaction() +
//					"\n\t" + "Source: " + arc.getSourceComponent() + "." + arc.getSourceService() +
//					"\n\t" + "Target: " + arc.getTargetComponent() + "." + arc.getTargetService() +
//					"\n\t" + "---------------------------------------------");
			
//			System.out.println("\t" + "ArcType: " + arc.getType());
//			System.out.println("\t" + "RootTransaction: " + arc.getRootTransaction());
//			System.out.println("\t" + "Source: " + arc.getSourceComponent() + "." + arc.getSourceService());
//			System.out.println("\t" + "Target: " + arc.getTargetComponent() + "." + arc.getTargetService());
//			System.out.println("\t" + "---------------------------------------------");
		}
		LOGGER.info(arcInfos);
	}

}
