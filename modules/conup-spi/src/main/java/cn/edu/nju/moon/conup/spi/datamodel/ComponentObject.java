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

}
