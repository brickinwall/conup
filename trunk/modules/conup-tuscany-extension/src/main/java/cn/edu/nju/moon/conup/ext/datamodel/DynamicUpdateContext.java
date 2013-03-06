package cn.edu.nju.moon.conup.ext.datamodel;

import java.util.Set;
import java.util.logging.Logger;


/** 
 * 	For dynamic update only, maintain old and new version classes
 * 
 *  @author JiangWang<jiang.wang88@gmail.com>
 */
public class DynamicUpdateContext {
	private final static Logger LOGGER = Logger.getLogger(DynamicUpdateContext.class.getName());
	/** old version of the class  */
	private Class<?>  oldVerClass;
	/** old version of the class  */
	private Class<?>  newVerClass;
	/** represents whether is the DynamicUpdateContext loaded */
	private boolean isLoaded = false;
	/**  root txs that should be responded with old version component */
//	private Set<String> oldVerionRootTxs = new HashSet<String>();
//	private Set<String> oldVerionRootTxs = null;
	private Set<String> bufferOldRootTxs = null;
	private Set<String> algorithmOldRootTxs = null;
	
	public boolean isLoaded() {
		return isLoaded;
	}
	
	public void removeAlgorithmOldRootTx(String oldRootTx){
		LOGGER.fine("in DynaUpdateCtx.removeAlgorithmOldRootTx(), rm " + oldRootTx);
		algorithmOldRootTxs.remove(oldRootTx);
	}
	
	public void removeBufferOldRootTx(String oldRootTx){
		LOGGER.fine("in DynaUpdateCtx.removeBufferOldRootTx(), rm " + oldRootTx);
		bufferOldRootTxs.remove(oldRootTx);
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public Class<?> getOldVerClass() {
		return oldVerClass;
	}

	public void setOldVerClass(Class<?> oldVerClass) {
		this.oldVerClass = oldVerClass;
	}

	public Class<?> getNewVerClass() {
		return newVerClass;
	}

	public void setNewVerClass(Class<?> newVerClass) {
		this.newVerClass = newVerClass;
	}

	public Set<String> getBufferOldRootTxs() {
		return bufferOldRootTxs;
	}

	public void setBufferOldRootTxs(Set<String> bufferOldRootTxs) {
		this.bufferOldRootTxs = bufferOldRootTxs;
	}

	public Set<String> getAlgorithmOldRootTxs() {
		return algorithmOldRootTxs;
	}

	public void setAlgorithmOldRootTxs(Set<String> algorithmOldRootTxs) {
		this.algorithmOldRootTxs = algorithmOldRootTxs;
	}
	
	public boolean isOldRootTxsInitiated(){
		return (bufferOldRootTxs!=null) && (algorithmOldRootTxs!=null);
	}
	
	public boolean isOldRootTxsEquals(){
		boolean isEquals = false;
		LOGGER.fine("algorithmOldRootTxs.size()=" + algorithmOldRootTxs.size() + 
				", bufferOldRootTxs.size()=" + bufferOldRootTxs.size());
		LOGGER.fine("algorithmOldRootTxs: " + algorithmOldRootTxs);
		LOGGER.fine("bufferOldRootTxs: " + bufferOldRootTxs);
		if(algorithmOldRootTxs.containsAll(bufferOldRootTxs)
				&& bufferOldRootTxs.containsAll(algorithmOldRootTxs)){
			isEquals = true;
		} else if(algorithmOldRootTxs.size() != bufferOldRootTxs.size()){
			isEquals = false;
		}
		return isEquals;
	}
	
}
