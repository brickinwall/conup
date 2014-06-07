package cn.edu.nju.moon.conup.ext.utils.experiments.model;

import cn.edu.nju.moon.conup.spi.datamodel.Scope;

//	<indepRun>200</indepRun>
//	<targetComp>CurrencyConverter</targetComp>
//	<ipAddress>10.0.2.15</ipAddress>
public class ExpSetting {
	private int indepRun;
	private int rqstInterval;
	private String targetComp;
	private String ipAddress;
	private String baseDir;
	private Scope scope;

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
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
		return "indepRun:" + indepRun + " rqstInterval:" + rqstInterval
				+ " targetComp:" + targetComp + " ipAddress:" + ipAddress
				+ " baseDir:" + baseDir;
	}
}
