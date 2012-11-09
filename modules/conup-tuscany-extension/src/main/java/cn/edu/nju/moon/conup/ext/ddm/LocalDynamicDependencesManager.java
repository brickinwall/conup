package cn.edu.nju.moon.conup.ext.ddm;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.tx.manager.TxDepMonitor;
import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;



/**
 * A {@link LocalDynamicDependencesManager} can manage dynamic dependences of a transaction, 
 * that is, get its components to be used in future called future and the components 
 * having been used in the past called real past. It can be used alone, or when events 
 * happen, such as, transaction start, end, first invoke component, its dynamic dependences
 * changed, it will notify you.
 * 
 * @author Ping Su<njupsu@gmail.com>
 */

public class LocalDynamicDependencesManager {
	private final static Logger LOGGER = Logger.getLogger(LocalDynamicDependencesManager.class.getName());
	public static Logger getLogger() {
		return LOGGER;
	}
	
	/**
	 * The object to notify the events they cared happens.
	 */	
	private TxDepMonitor monitor = new TxDepMonitor();
	/**
	 * Unique identifier of a transaction runs.
	 */
	private String transactionID;
	
	private static Hashtable<String, LocalDynamicDependencesManager> ddes = new Hashtable<String, LocalDynamicDependencesManager>();
	/**
	 * Whether a transaction is in a response of the given method of the given component.
	 * the identifier of the method like this : componentName; methodName 
	 */
	private Set<String> real_past = new ConcurrentSkipListSet<String>();
	/**
	 * All states in the transaction's Dynamic Dependency Automaton
	 */
	private List<SetState> states = new LinkedList<SetState>();
	/**
	 * All events in the transaction's Dynamic Dependency Automaton
	 */
	private String[] events;
	/**
	 * The current state of the transaction in its Dynamic Dependency Automaton
	 */
	private int currentState = 0;


	/**
	 * This is for java implementation to get its LDDM to manage its dynamic dependences
	 * @param name
	 * 			transaction id
	 * @param states	 
	 * @param nexts
	 * @return
	 */
	public static LocalDynamicDependencesManager getInstance(String transactionID,String states,String nexts) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {
			LocalDynamicDependencesManager instance = new LocalDynamicDependencesManager(transactionID,states,nexts);
			ddes.put(transactionID, instance);
			return instance;
		}
	}
	/**
	 * This is for Tuscany to get transaction's dynamic dependences
	 * @param transactionID
	 * @return
	 */
	public static LocalDynamicDependencesManager getInstance(String transactionID) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {
			LOGGER.info("The transaction id is wrong!");
			return null;
		}
	}

	/**
	 * 
	 * @param name
	 *            transactionID
	 */
	private LocalDynamicDependencesManager(String transactionID,String statesDDA,String nextsDDA) {
		this.transactionID = transactionID;	
		String[] stateAnno = statesDDA.split(";");
		String[] eventAnno = nextsDDA.split(";");		
		for (int i = 0; i < stateAnno.length; i++) {
			String[] si = stateAnno[i].split(",");
			Set<String> stateFuture = new ConcurrentSkipListSet<String>();
			for (int j = 0; j < si.length; j++) {
				if (si[j].equals("_E")) {
					si[j] = "";
				}
				// System.out.print(si[j]+"   ");
				stateFuture.add(si[j]);
			}
			SetState state = new SetState();
			state.setFuture(stateFuture);
			states.add(state);
		}
		LOGGER.info("---------states:" + states.size());
		// get event information
		events = new String[eventAnno.length];
		for (int i = 0; i < eventAnno.length; i++) {
			if (eventAnno[i].equals("_E")) {
				eventAnno[i] = "";
			}
			events[i] = eventAnno[i];
		}

	}
	/**
	 * 
	 * @return
	 * Set<reference>
	 */

	public Set<String> getFuture() {
		if(states.isEmpty()){
			return new ConcurrentSkipListSet<String>();
		}
		else{
			return states.get(currentState).getFuture();
		}		
	}

	public Set<String> getRealPast() {
		return real_past;
	}
	
	/**	
	 * When a subtransaction begin, you can get whether the component will be used
	 * @param reference
	 *            Service name	     	
	 */
	public boolean isThisLastUsed(String reference){
		if(getFuture().contains(reference)){
			LOGGER.info(reference + "now is in the future set!");
			String eve = events[currentState];
			String eveinf[] = eve.split(",");
			for (String e : eveinf) {
				if (e.contains(reference)) {
					int tempNextState = Integer.parseInt(e.split("-")[1]);									
					LOGGER.info(reference + "is now in use in transaction"+transactionID);
					if(states.get(tempNextState).getFuture().contains(reference)){
						LOGGER.info(reference + "will use in transaction "+transactionID);
						return true;
					}
					else{
						LOGGER.info(reference + "will not use in transaction "+transactionID);
						return false;
					}}}
			LOGGER.info(reference + "is not in the next event set! your input service may be wrong!");			
			return false;
		}
		else{
			LOGGER.info(reference + "now is not in the future set!");
			return false;		}
		
	}

	/**
	 * EventType : first request service from other component
	 */
	public void FirstRequestService() {
		monitor.notify(TxEventType.FirstRequestService,transactionID);
	}

	/**
	 * get the real past after the given trigger action dynamically
	 * 
	 * @param event
	 */
	public void trigger(String event) {	
		if (event.contains("Start")) {
			currentState = 0;
			monitor.notify(TxEventType.TransactionStart, transactionID);
			LOGGER.info("Transaction  " + transactionID + "  is start!");			
			}
		else {
			if (event.isEmpty()) {	
				ddes.remove(transactionID);
				monitor.notify(TxEventType.TransactionEnd, transactionID);
				LOGGER.info("Transaction  " + transactionID + "  is end!");	}
			else {
				//if it is a component-invoked event, change past set. else,leave unchaged.			
				if (event.contains("COM")) {			
				String port = event.split("\\.")[1];
				real_past.add(port);
				}
				//find the next state, get precise future set.
				String eve = events[currentState];
				String eveinf[] = eve.split(",");
				for (String e : eveinf) {
					if (e.contains(event)) {
						currentState = Integer.parseInt(e.split("-")[1]);
						monitor.notify(TxEventType.DependencesChanged, transactionID);						
						LOGGER.info("Transaction  " + transactionID + "  dynamic dependences have been changed!");
						return;
				}
				LOGGER.info("The input event is wrong!");
				}
				}
			}
		}
	public static void main(String args[]){
		String tid = "1234";
		LocalDynamicDependencesManager.getInstance(tid, "TokenService,ProcService;ProcService;_E", "COM.TokenService.23-1;COM.ProcService.35-2;_E").trigger("Start");
		LocalDynamicDependencesManager.getInstance(tid, "TokenService,ProcService;ProcService;_E", "COM.TokenService.23-1;COM.ProcService.35-2;_E").FirstRequestService();
		LocalDynamicDependencesManager.getInstance(tid, "TokenService,ProcService;ProcService;_E", "COM.TokenService.23-1;COM.ProcService.35-2;_E").trigger("COM.TokenService.23");
		LocalDynamicDependencesManager.getInstance(tid, "TokenService,ProcService;ProcService;_E", "COM.TokenService.23-1;COM.ProcService.35-2;_E").trigger("COM.ProcService.35");
		LocalDynamicDependencesManager.getInstance(tid, "TokenService,ProcService;ProcService;_E", "COM.TokenService.23-1;COM.ProcService.35-2;_E").trigger("");
	}
}
