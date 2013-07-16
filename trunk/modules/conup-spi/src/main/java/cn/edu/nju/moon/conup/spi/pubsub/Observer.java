package cn.edu.nju.moon.conup.spi.pubsub;


public interface Observer {
	public void update(Subject subject, Object arg);
}
