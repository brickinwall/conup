package cn.edu.nju.moon.conup.spi.pubsub;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * Jul 26, 2013 10:06:29 PM
 */
public interface Observer {
	public void update(Subject subject, Object arg);
}
