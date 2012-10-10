package cn.edu.nju.moon.conup.pre;



/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class Event {
	private int head;
	/**
	 * @return the head
	 */
	private int tail;
	private String event;

	public Event(int head, int tail, String event) {
		super();
		this.head = head;
		this.tail = tail;
		this.event = event;
		System.out.println(head + "-" + event + "-" + tail);
	}

	/**
	 * @param head
	 * @param tail
	 * @param event
	 */
	public int getHead() {
		return head;
	}

	/**
	 * @param head
	 *            the head to set
	 */
	public void setHead(int head) {
		this.head = head;
	}

	/**
	 * @return the tail
	 */
	public int getTail() {
		return tail;
	}

	/**
	 * @param tail
	 *            the tail to set
	 */
	public void setTail(int tail) {
		this.tail = tail;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event
	 *            the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	public String getPort() {
		if (event.contains("COM")) {
//			System.out.println(event.split("\\.")[1]);
			return event.split("\\.")[1];
			
		}
		return null;

	}

}
