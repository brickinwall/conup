package cn.edu.nju.moon.conup.txpre;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/**
 * @author Ping Su<njupsu@gmail.com>
 */
public class StateMachine {

	
	private int start;
	
//	private int end;
	private Set<Integer> end = new ConcurrentSkipListSet<Integer>();
	
	private List<Integer> states = new LinkedList<Integer>();
	
	private List<Event> events = new LinkedList<Event>();

	
	public StateMachine() {
		start = -1;
	}
	public StateMachine(List<Integer> states,List<Event> events) {		
		this.states = states;
		this.events = events;
		start = -1;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Set<Integer> getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(Set<Integer> end) {
		this.end = end;
	}

	/**
	 * @return the states
	 */
	public List<Integer> getStates() {
		return states;
	}

	/**
	 * @param states
	 *            the states to set
	 */
	public void setStates(List<Integer> states) {
		this.states = states;
	}
	
	@Deprecated
	public int getstate(int head, String event) {
		for (int i = 0; i < events.size(); i++) {
			Event e = events.get(i);
			if (e.getHead() == head && e.getEvent().equals(event))
				return e.getTail();
		}
		return -1;
	}
	@Deprecated
	public int stateindex(int index) {
		for (int i = 0; i < states.size(); i++) {
			if ((Integer) states.get(i) == index)
				return i;
		}
		return -1;

	}

	public void addState(int state) {
		states.add(state);
	}

	public void deleteState(int state) {
		for (int i = 0; i < states.size(); i++) {
			if ((Integer) states.get(i) == state)
				states.remove(i);
		}
	}
	/**
	 * merge the two states into one(the latter one)
	 * @param s1
	 *  state location
	 * @param s2
	 *  state location
	 */
	public void mergeStates(int s1, int s2) {
		//delete s1, change to s2 where there is s1
		if(s1 == start){
			this.setStart(s2);
//			System.out.println("$$$$$$$ change start to: "+s2);
		}
		if(states.contains(s1)){
			int state_index = states.indexOf(s1);
			states.remove(state_index);
		}
		else{
			System.out.println("there is some wrong!");
		}
		for (int i = 0; i < events.size(); i++) {
			Event e = events.get(i);
			if (e.getTail() == s1) {
				e.setTail(s2);
			}
			if(e.getHead() == s1)
				e.setHead(s2);
		}
	}

	public int getStatesCount() {

		return states.size();
	}

	public void addEvent(Event event) {
		events.add(event);
	}

	public void deleteEvent(Event event) {
		for (int i = 0; i < events.size(); i++) {
			if (event.getEvent().equals(events.get(i).getEvent()))
				events.remove(i);
		}
	}

	public List<Event> getEvents() {
		return events;
	}
	
	public Event getEvent(String eve){
		for(Event e : events){
			if(e.getEvent().equals(eve)){
				return e;
			}
		}
		return null;
	}
	/**
	 * the next state from the current state
	 * @param srcState
	 * @return
	 */
	public Set<Integer> getNextStateLocation(int srcState){
		Set<Integer> nextState = new HashSet<Integer>();
		for(Event e: events){
			if(e.getHead() == srcState){
				nextState.add(e.getTail());
			}
		}
		return nextState;
	}
	public List<Event> getNextEvent(int srcState){
		List<Event> nextEvent = new LinkedList<Event>();
		for(Event e: events){
			if(e.getHead() == srcState){
				nextEvent.add(e);
			}
		}
		return nextEvent;
	}

}
