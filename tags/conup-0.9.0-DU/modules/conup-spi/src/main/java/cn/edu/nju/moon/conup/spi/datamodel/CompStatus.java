package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * CompStatus talks about all the possible status
 * during the component running:NORMAL,VALID,ONDEMAND,UPDATING,UPDATED;
 * @author rgc
 */
public enum CompStatus {
	NORMAL,
	ONDEMAND,
	VALID,
	Free,
	UPDATING;
//	UPDATED;
}
