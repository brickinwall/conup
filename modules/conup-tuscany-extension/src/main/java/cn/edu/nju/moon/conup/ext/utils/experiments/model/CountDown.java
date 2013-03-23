package cn.edu.nju.moon.conup.ext.utils.experiments.model;

public class CountDown {
	private int count;

	public CountDown(int count) {
		this.count = count;
	}

	public synchronized void countDown() {
		count--;
	}

	public synchronized boolean hasNext() {
		return (count > 0);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
