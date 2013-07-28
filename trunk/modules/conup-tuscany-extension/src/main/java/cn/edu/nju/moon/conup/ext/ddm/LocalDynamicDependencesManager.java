package cn.edu.nju.moon.conup.ext.ddm;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.TxEventType;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.tx.TxDepMonitor;

/**
 * A {@link LocalDynamicDependencesManager} can manage dynamic dependences of a
 * transaction, that is, get its components to be used in future called future
 * and the components having been used in the past called real past. It can be
 * used alone, or when events happen, such as, transaction start, end, first
 * invoke component, its dynamic dependences changed, it will notify you.
 * 
 * @author Ping Su<njupsu@gmail.com>
 */

public class LocalDynamicDependencesManager {
	private static Hashtable<String, LocalDynamicDependencesManager> ddes = new Hashtable<String, LocalDynamicDependencesManager>();
	private final static Logger LOGGER = Logger
			.getLogger(LocalDynamicDependencesManager.class.getName());

	/**
	 * This is for Tuscany to get transaction's dynamic dependences
	 * 
	 * @param transactionID
	 * @return
	 */
	public static LocalDynamicDependencesManager getInstance(
			String transactionID) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {
			LOGGER.info("The transaction id is wrong!");
			return null;
		}
	}

	/**
	 * TxDepMonitor This is for java implementation to get its LDDM to manage
	 * its dynamic dependences
	 * @param transaction id
	 * @param states
	 * @param nexts
	 * @return
	 */
	public static LocalDynamicDependencesManager getInstance(
			String transactionID, String states, String nexts) {
		if (ddes.containsKey(transactionID)) {
			return ddes.get(transactionID);
		} else {
			LocalDynamicDependencesManager instance = new LocalDynamicDependencesManager(
					transactionID, states, nexts);
			ddes.put(transactionID, instance);
			return instance;
		}
	}

	/**
	 * The current state of the transaction in its Dynamic Dependency Automaton
	 */
	private int currentState = 0;
	/**
	 * All events in the transaction's Dynamic Dependency Automaton
	 */
	private String[] events;
	/**
	 * Whether a transaction is in a response of the given method of the given
	 * component. the identifier of the method like this : componentName;
	 * methodName
	 */
	private Set<String> past = null;
	/**
	 * All states in the transaction's Dynamic Dependency Automaton
	 */
	private List<SetState> states = new LinkedList<SetState>();

	/**
	 * Unique identifier of a transaction runs.
	 */
	private String transactionID;
	/**
	 * The object to notify the events they cared happens.
	 */
	// private TxDepMonitorImpl txDepMointor = new TxDepMonitorImpl();
	// TODO tell ping to change her function call code
	private TxDepMonitor txDepMointor = null;

	/**
	 * @param name transactionID
	 */
	private LocalDynamicDependencesManager(String transactionID,
			String statesDDA, String nextsDDA) {
		this.transactionID = transactionID;
		String[] stateAnno = statesDDA.split(";");
		String[] eventAnno = nextsDDA.split(";");
		for (int i = 0; i < stateAnno.length; i++) {
			String[] si = stateAnno[i].split(",");
			Set<String> stateFuture = new ConcurrentSkipListSet<String>();
			for (int j = 0; j < si.length; j++) {
				if (!si[j].equals("_E")) {
					stateFuture.add(si[j]);
				}
			}
			SetState state = new SetState();
			state.setFuture(stateFuture);
			states.add(state);
		}
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
	 * EventType : first request service from other component
	 */
	public void FirstRequestService() {
		txDepMointor.notify(TxEventType.FirstRequestService, transactionID);
	}

	/**
	 * get services to be used in future
	 * 
	 * @return Set<reference>
	 */
	public Set<String> getFuture() {
		if (states.isEmpty()) {
			return new ConcurrentSkipListSet<String>();
		} else {
			return reverse(states.get(currentState).getFuture());
		}
	}

	/**
	 * get services have been used
	 * @return
	 */
	public Set<String> getPast() {
		return reverse(past);
	}

	/**
	 * get current state contents, which is the future set in current state
	 * @return Set<reference>
	 */
	private Set<String> getTempFuture() {
		if (states.isEmpty()) {
			return new ConcurrentSkipListSet<String>();
		} else {
			return states.get(currentState).getFuture();
		}
	}

	/**
	 * get the fully qualified name
	 * @param aset
	 * @return
	 */
	public Set<String> reverse(Set<String> aset) {
		Set<String> newset = new ConcurrentSkipListSet<String>();
		if (aset == null) {
			return newset;
		} else {
			for (String content : aset) {
				content = content.replace(File.separator, ".");
				newset.add(content);
			}
		}
		return newset;
	}

	/**
	 * get the real past after the given trigger action dynamically
	 * 
	 * @param event
	 */
	public void trigger(String event, String compIdentifier) {
		if (event.contains("Start")) {
			currentState = 0;
			past = new ConcurrentSkipListSet<String>();
			if (txDepMointor == null) {
				this.txDepMointor = NodeManager.getInstance().getTxDepMonitor(
						compIdentifier);
			}
			assert txDepMointor != null;
			txDepMointor.notify(TxEventType.TransactionStart, transactionID);
			LOGGER.fine("Transaction  " + transactionID + "  is start!");
		} else {
			if (event.isEmpty()) {
				txDepMointor.notify(TxEventType.TransactionEnd, transactionID);
				ddes.remove(transactionID);
				LOGGER.fine("Transaction  " + transactionID + "  is end!");
			} else {
				// if it is a component-invoked event, change past set.
				// else,leave unchaged.
				if (event.contains("COM")) {
					String port = event.split("\\.")[1];
					past.add(port);
				}
				// find the next state, get precise future set.
				String eve = events[currentState];
				String eveinf[] = eve.split(",");
				for (String e : eveinf) {
					if (e.contains(event)) {
						currentState = Integer.parseInt(e.split("-")[1]);
						txDepMointor.notify(TxEventType.DependencesChanged,
								transactionID);
						LOGGER.fine("Transaction  " + transactionID
								+ "  dynamic dependences have been changed!");
						return;
					}
				}
				LOGGER.warning("The input event is wrong!");
			}
		}
	}

	/**
	 * When a sub-transaction begins, you can get whether the component will be
	 * used in the future
	 * @param reference Service with fully qualified name
	 */
	public boolean whetherUseInFuture(String reference) {
		String referenceTemp = reference.replace(".", File.separator);
		if (getTempFuture().contains(referenceTemp)) {
			String eve = events[currentState];
			String eveinf[] = eve.split(",");
			for (String e : eveinf) {
				if (e.contains(referenceTemp)) {
					int tempNextState = Integer.parseInt(e.split("-")[1]);
					if (states.get(tempNextState).getFuture()
							.contains(referenceTemp)) {
						LOGGER.fine(reference
								+ "is in use now and will be used in transaction "
								+ transactionID + " in future!");
						return true;
					} else {
						LOGGER.fine(reference
								+ " is in use now, but will not use in transaction "
								+ transactionID + " in future!");
						return false;
					}
				}
			}
			LOGGER.fine(reference
					+ " is not in use now, but will be used in the future!");
			return true;
		} else {
			LOGGER.fine(reference + " is not in use now, and won't in future!");
			return false;
		}

	}
}
