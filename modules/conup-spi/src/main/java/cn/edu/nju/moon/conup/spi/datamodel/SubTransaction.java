package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * 
 * @author Jiang Wang
 *
 */
public 	class SubTransaction{
	private String subTxHost = null;
	private String subTxStatus = null;
	
	public String getSubTxHost() {
		return subTxHost;
	}
	public void setSubTxHost(String subTxHost) {
		this.subTxHost = subTxHost;
	}
	public String getSubTxStatus() {
		return subTxStatus;
	}
	public void setSubTxStatus(String subTxStatus) {
		this.subTxStatus = subTxStatus;
	}
}
