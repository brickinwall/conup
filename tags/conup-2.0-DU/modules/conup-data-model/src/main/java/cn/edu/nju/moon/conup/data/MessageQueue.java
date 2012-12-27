package cn.edu.nju.moon.conup.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;



public class MessageQueue {
	private static MessageQueue messageQueue = new MessageQueue();
	private Map<PhasedInterceptor, Queue<Message>> msgMap = new HashMap<PhasedInterceptor, Queue<Message>>();
	
	private MessageQueue(){
		
	}
	
	public static MessageQueue getInstance(){
		return messageQueue;
	}

	public Map<PhasedInterceptor, Queue<Message>> getMsgMap() {
		return msgMap;
	}

	public void setMsgMap(Map<PhasedInterceptor, Queue<Message>> msgMap) {
		this.msgMap = msgMap;
	}
	

}
