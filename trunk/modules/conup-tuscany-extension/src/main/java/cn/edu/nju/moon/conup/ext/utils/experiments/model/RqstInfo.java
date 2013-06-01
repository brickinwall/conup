package cn.edu.nju.moon.conup.ext.utils.experiments.model;

public class RqstInfo implements Comparable<RqstInfo>{
	private int threadId;
	private long startTime;
	private long endTime;
	private long absoluteTime;

	public RqstInfo(int threadId, long startTime, long endTime) {
		this.threadId = threadId;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public RqstInfo(int threadId, long startTime, long endTime, long absoluteTime) {
		this.threadId = threadId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.absoluteTime = absoluteTime;
	}

	public RqstInfo() {
		
	}

	public long getAbsoluteTime() {
		return absoluteTime;
	}
	
	public void setAbsoluteTime(long absoluteTime) {
		this.absoluteTime = absoluteTime;
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
