package cn.edu.nju.moon.conup.spi.datamodel;

public enum BufferEventType {
	NOTHING,
	ONDEMAND,
	VALIDTOFREE,
	WAITFORREMOTEUPDATE,
	EXEUPDATE;
}
