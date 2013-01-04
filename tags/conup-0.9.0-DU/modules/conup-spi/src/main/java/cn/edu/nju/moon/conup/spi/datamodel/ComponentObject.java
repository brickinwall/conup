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
//	private String ipAndPort = null;
	/** component's communication module's info */
//	private CompCommInfo compCommInfo = null;
	/** component's all static info(including who depends on current component and depend on other component */
//	private Map<String, List<CompCommInfo>> allStaticDeps = null;
	
	public Set<String> getStaticInDeps() {
		return staticInDeps;
	}
	
	public void setStaticInDeps(Set<String> staticInDeps) {
		this.staticInDeps = staticInDeps;
	}
//	public CompCommInfo getCompCommInfo() {
//		return compCommInfo;
//	}
//
//	public void setCompCommInfo(CompCommInfo compCommInfo) {
//		this.compCommInfo = compCommInfo;
//	}

	
//	public Map<String, List<CompCommInfo>> getAllStaticDeps() {
//		return allStaticDeps;
//	}
//
//	public void setAllStaticDeps(Map<String, List<CompCommInfo>> allStaticDeps) {
//		this.allStaticDeps = allStaticDeps;
//	}

//	public String getIpAndPort() {
//		return ipAndPort;
//	}
//	
//	public void setIpAndPort(String ipAndPort) {
//		this.ipAndPort = ipAndPort;
//	}

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
//		this.allStaticDeps = new HashMap<String, List<CompCommInfo>>();
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
