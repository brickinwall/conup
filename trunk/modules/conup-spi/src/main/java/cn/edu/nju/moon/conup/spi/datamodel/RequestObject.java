package cn.edu.nju.moon.conup.spi.datamodel;

import java.io.Serializable;

/**
 * 
 * @author Guochao Ren<rgc.nju.cs@gmail.com>
 * @version Created time: Jul 28, 2013 11:03:53 PM
 */
public class RequestObject implements Serializable {
	
	private static final long serialVersionUID = -2202113397807809095L;
	
	private String srcIdentifier;
	private String targetIdentifier;
	// the protocol type can be CONSISTENCY, QUIESCENCE and TRANQUILLITY
	private String protocol;
	private MsgType msgType;
	private String payload;
	private CommType commType;

	public CommType getCommType() {
		return commType;
	}

	public void setCommType(CommType commType) {
		this.commType = commType;
	}

	public MsgType getMsgType() {
		return msgType;
	}
	
	public void setMsgType(MsgType msgType) {
		this.msgType = msgType;
	}
	
	public String getSrcIdentifier() {
		return srcIdentifier;
	}

	public void setSrcIdentifier(String srcIdentifier) {
		this.srcIdentifier = srcIdentifier;
	}

	public String getTargetIdentifier() {
		return targetIdentifier;
	}

	public void setTargetIdentifier(String targetIdentifier) {
		this.targetIdentifier = targetIdentifier;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	@Override
	public String toString() {
		return "RequestObject: srcIdentifier: " + srcIdentifier + " targetIdentifier: " + targetIdentifier +
				" protocol: " + protocol + " payload: " + payload;
	}

}
