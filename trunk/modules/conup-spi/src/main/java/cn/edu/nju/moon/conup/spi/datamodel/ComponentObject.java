package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * A abstract component model, which is supposed to be independent to concrete component model,
 * e.g., tuscany component model.
 * 
 * With this abstract component model, conup is decoupled from different platform.
 * 
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ComponentObject {
	/** an identifier should exclusively identify a component  */
	private String identifier = null;
	/** version of the component */
	private String componentVersion = null;
	/** user specified algorithm in the configuration file */
	private String algorithmConf = null;
	/** user specified freeness strategy in the configuration file */
	private String freenessConf = null;
	
	/**
	 * @param identifier
	 * @param compVersion
	 * @param algorithmConf
	 * @param freenessConf
	 */
	public ComponentObject(String identifier, String compVersion, 
			String algorithmConf, String freenessConf){
		this.identifier = identifier;
		this.componentVersion = compVersion;
		this.algorithmConf = algorithmConf;
		this.freenessConf = freenessConf;
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the componentVersion
	 */
	public String getComponentVersion() {
		return componentVersion;
	}
	
	/**
	 * @param componentVersion the componentVersion to set
	 */
	public void setComponentVersion(String componentVersion) {
		this.componentVersion = componentVersion;
	}

	/**
	 * @return the algorithmConf
	 */
	public String getAlgorithmConf() {
		return algorithmConf;
	}

	/**
	 * @param algorithmConf the algorithmConf to set
	 */
	public void setAlgorithmConf(String algorithmConf) {
		this.algorithmConf = algorithmConf;
	}

	/**
	 * @return the freenessConf
	 */
	public String getFreenessConf() {
		return freenessConf;
	}

	/**
	 * @param freenessConf the freenessConf to set
	 */
	public void setFreenessConf(String freenessConf) {
		this.freenessConf = freenessConf;
	}
	
}
