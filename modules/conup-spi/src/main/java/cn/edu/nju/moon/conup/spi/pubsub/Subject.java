package cn.edu.nju.moon.conup.spi.pubsub;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * Jul 26, 2013 10:06:21 PM
 */
public interface Subject {
	public void registerObserver(Observer o);
	
	public void removeObserver(Observer o);
	
	public void notifyObservers(Object arg);
	
	public void setResult(String result);
}
