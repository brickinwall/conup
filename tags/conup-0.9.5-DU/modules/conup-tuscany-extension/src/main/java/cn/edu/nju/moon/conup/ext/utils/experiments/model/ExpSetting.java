package cn.edu.nju.moon.conup.ext.utils.experiments.model;

//	<indepRun>200</indepRun>
//	<nThreads>10</nThreads>
//	<threadId>5</threadId>
//	<targetComp>CurrencyConverter</targetComp>
//	<ipAddress>10.0.2.15</ipAddress>
public class ExpSetting {
	private int indepRun;
	private int nThreads;
	private int threadId;
	private int rqstInterval;
	private int updateInterval;
	private String targetComp;
	private String ipAddress;
	private String baseDir;
	private String type;

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public int getRqstInterval() {
		return rqstInterval;
	}

	public void setRqstInterval(int rqstInterval) {
		this.rqstInterval = rqstInterval;
	}

	public int getIndepRun() {
		return indepRun;
	}

	public void setIndepRun(int indepRun) {
		this.indepRun = indepRun;
	}

	public int getnThreads() {
		return nThreads;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public String getTargetComp() {
		return targetComp;
	}

	public void setTargetComp(String targetComp) {
		this.targetComp = targetComp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String toString() {
		return "indepRun:" + indepRun + " nThreads:" + nThreads + " threadId:" + threadId 
				+ " rqstInterval:" + rqstInterval + " targetComp:" + targetComp 
				+ " ipAddress:" + ipAddress + " baseDir:" + baseDir
				+ " type:" + type;
	}
}
