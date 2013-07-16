package cn.edu.nju.moon.conup.spi.pubsub;

public interface Subject {
	public void registerObserver(Observer o);
	
	public void removeObserver(Observer o);
	
	public void notifyObservers(Object arg);
	
	public void setResult(String result);
}
