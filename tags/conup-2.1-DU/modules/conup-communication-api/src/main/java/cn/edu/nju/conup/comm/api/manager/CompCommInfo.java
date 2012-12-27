package cn.edu.nju.conup.comm.api.manager;
/**
 * @author rgc
 * @version Nov 27, 2012 9:22:23 PM
 */
public class CompCommInfo {
	private String componentIdentifier;
	private String ip;
	private int port;
	
	public CompCommInfo(String componentIdentifier, String ip, int port){
		this.componentIdentifier = componentIdentifier;
		this.ip = ip;
		this.port = port;
	}

	public String getComponentIdentifier() {
		return componentIdentifier;
	}

	public void setComponentIdentifier(String componentIdentifier) {
		this.componentIdentifier = componentIdentifier;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
