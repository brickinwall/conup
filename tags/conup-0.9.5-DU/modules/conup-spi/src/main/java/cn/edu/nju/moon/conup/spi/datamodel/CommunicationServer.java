package cn.edu.nju.moon.conup.spi.datamodel;

public interface CommunicationServer {
	
	/**
	 * 
	 * @param compIdentifier
	 * @return
	 */
	public boolean start(String compIdentifier);
	
	/**
	 * 
	 * @param compIdentifier
	 * @param ip
	 * @param port
	 * @return
	 */
	public boolean stop(String compIdentifier);
}
