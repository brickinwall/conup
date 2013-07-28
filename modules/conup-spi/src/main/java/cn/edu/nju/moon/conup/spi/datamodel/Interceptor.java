package cn.edu.nju.moon.conup.spi.datamodel;

import org.apache.tuscany.sca.invocation.Message;
/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:03:20 PM
 */
public interface Interceptor {
	public Message invoke(Message msg);
	
	public void update(Object arg);
	
}
