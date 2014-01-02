package cn.edu.nju.moon.conup.spi.datamodel;
/**
 * Indicate the event type
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 10:57:38 PM
 */
public enum BufferEventType {
	NORMAL,
	ONDEMAND,
	VALIDTOFREE,
	WAITFORREMOTEUPDATE,
	EXEUPDATE,
	DEFREEZE;
}
