package cn.edu.nju.moon.conup.ext.datamodel;

import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;


/** 
 * 	For dynamic update only, maintain old and new version classes
 * 
 *  @author JiangWang<jiang.wang88@gmail.com>
 */
public class POJODynamicUpdateContext implements DynamicUpdateContext {
	private final static Logger LOGGER = Logger.getLogger(POJODynamicUpdateContext.class.getName());
	/** old version of the class  */
	private Class<?>  oldVerClass;
	/** old version of the class  */
	private Class<?>  newVerClass;
	/** represents whether is the DynamicUpdateContext loaded */
	private boolean isLoaded = false;
	/**  root txs that should be responded with old version component */
	private Set<String> algorithmOldRootTxs = null;
	
	@Override
	public boolean isLoaded() {
		return isLoaded;
	}
	
	@Override
	public void removeAlgorithmOldRootTx(String oldRootTx){
		LOGGER.fine("in DynaUpdateCtx.removeAlgorithmOldRootTx(), rm " + oldRootTx);
		algorithmOldRootTxs.remove(oldRootTx);
	}
	
	@Override
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	@Override
	public Class<?> getOldVerClass() {
		return oldVerClass;
	}

	@Override
	public void setOldVerClass(Class<?> oldVerClass) {
		this.oldVerClass = oldVerClass;
	}

	@Override
	public Class<?> getNewVerClass() {
		return newVerClass;
	}

	@Override
	public void setNewVerClass(Class<?> newVerClass) {
		this.newVerClass = newVerClass;
	}

	@Override
	public Set<String> getAlgorithmOldRootTxs() {
		return algorithmOldRootTxs;
	}

	@Override
	public void setAlgorithmOldRootTxs(Set<String> algorithmOldRootTxs) {
		this.algorithmOldRootTxs = algorithmOldRootTxs;
	}
	
	@Override
	public boolean isOldRootTxsInitiated(){
		return algorithmOldRootTxs != null;
	}
	
}
