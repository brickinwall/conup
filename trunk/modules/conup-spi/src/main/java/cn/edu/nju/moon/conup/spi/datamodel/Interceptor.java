package cn.edu.nju.moon.conup.spi.datamodel;

import org.apache.tuscany.sca.invocation.Message;

public interface Interceptor {
	public Message invoke(Message msg);
	
	public void update(Object arg);
	
}
