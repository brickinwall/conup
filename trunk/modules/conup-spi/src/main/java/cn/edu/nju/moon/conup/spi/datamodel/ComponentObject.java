package cn.edu.nju.moon.conup.spi.datamodel;

import java.util.Set;

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
	/** a component should be aware of which components it statically depends on */
	private Set<String> staticDeps = null;
	/** components who statically depends on current component */
	private Set<String> staticInDeps = null;

	/** implementation type of the component */
	private String implType = null;
	
	public Set<String> getStaticInDeps() {
		return staticInDeps;
	}
	
	public void setStaticInDeps(Set<String> staticInDeps) {
		this.staticInDeps = staticInDeps;
	}

	public String getImplType() {
		return implType;
	}

	public void setImplType(String implType) {
		this.implType = implType;
	}

	/**
	 * @param identifier
	 * @param compVersion
	 * @param algorithmConf
	 * @param freenessConf
	 */
	public ComponentObject(String identifier, String compVersion, 
			String algorithmConf, String freenessConf, 
			Set<String> staticDeps, Set<String> staticIndeps, String implType){
		this.identifier = identifier;
		this.componentVersion = compVersion;
		this.algorithmConf = algorithmConf;
		this.freenessConf = freenessConf;
		this.staticDeps = staticDeps;
		this.implType = implType;
		this.staticInDeps = staticIndeps;
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
	
	/**
	 * @return components that current component statically depends on
	 */
	public Set<String> getStaticDeps() {
		return staticDeps;
	}

	public void setStaticDeps(Set<String> staticDeps) {
		this.staticDeps = staticDeps;
	}

	
}
