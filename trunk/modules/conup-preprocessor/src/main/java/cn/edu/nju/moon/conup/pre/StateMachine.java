package cn.edu.nju.moon.conup.pre;

import java.util.LinkedList;
import java.util.List;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class StateMachine {

	// 瀵拷顬婇懞鍌滃仯
	private int start;
	// 缂佸牊顒涢懞鍌滃仯
	private int end;
	//閹惰棄褰囬崙铏规畱閹碉拷婀佺粙瀣碍閻樿埖锟�
	private List<Integer> states = new LinkedList<Integer>();
	//閹碉拷婀侀惃鍕儲鏉烆兛绨ㄦ禒锟�
	private List<Event> events = new LinkedList<Event>();

	/**
	 * 
	 */
	public StateMachine() {

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
	public int getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(int end) {
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

	public int getstate(int head, String event) {
		for (int i = 0; i < events.size(); i++) {
			Event e = events.get(i);
			if (e.getHead() == head && e.getEvent().equals(event))
				return e.getTail();
		}
		return -1;
	}

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

	public void mergeStates(int s1, int s2) {

		int s1_index = states.indexOf(s1);
		states.remove(s1_index);
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

}
