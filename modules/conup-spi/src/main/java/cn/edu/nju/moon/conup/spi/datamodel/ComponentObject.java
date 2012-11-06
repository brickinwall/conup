package cn.edu.nju.moon.conup.spi.datamodel;

/**
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class ComponentObject {
	private String componentName = null;
	private String componentVersion = null;
	
	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}
	
	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
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
