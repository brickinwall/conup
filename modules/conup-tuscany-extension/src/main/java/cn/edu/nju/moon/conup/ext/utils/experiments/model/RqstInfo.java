package cn.edu.nju.moon.conup.ext.utils.experiments.model;

public class RqstInfo implements Comparable<RqstInfo>{
	private int threadId;
	private long startTime;
	private long endTime;

	public RqstInfo(int threadId, long startTime, long endTime) {
		this.threadId = threadId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public RqstInfo() {

	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public double getRqstResponseTime() {
		return (endTime - startTime) * 1e-6;
	}

	@Override
	public String toString() {
		return "threadId:" + threadId + " startTime:" + startTime + " endTime:" + endTime;
	}

	@Override
	public int compareTo(RqstInfo rqstInfo) {
		return this.threadId - rqstInfo.getThreadId();
	}
}
