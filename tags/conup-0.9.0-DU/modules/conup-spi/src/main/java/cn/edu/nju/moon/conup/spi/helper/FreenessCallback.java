package cn.edu.nju.moon.conup.spi.helper;


/**
 * We're trying to make the core of our software independent to concrete component 
 * model/platform, e.g., Apache Tuscany, so we define a conup-spi module between 
 * tuscany-extension module and conup-core module.
 * 
 * As to freeness strategies, however, in some cases, they may need to manipulate 
 * tuscany-extension module. So a FreenessCallback is defined.
 * 
 * Tuscany-extension module is responsible for passing a FreenessCallback instance to
 * FreenessStrategy when invoking it. If nesscessary, the FreenessStrategy will callback
 * to ask the tuscany-extension execute some operations which is platform/tuscany dependent.
 * 
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public interface FreenessCallback {
	/**
	 * Replace the original component implementation with the new version class
	 * @param compIdentifer
	 * @param compClass class for replacement
	 */
	public void toNewVersionComp(String compIdentifer);
	
	/**
	 * Replace current component implementation with the old version class
	 * @param compIdentifer
	 */
	public void toOldVersionComp(String compIdentifer);
	
	/**
	 * @return the implementation type of the component, e.g., POJO, EJB
	 */
	public String getCompImplType();
	
}
