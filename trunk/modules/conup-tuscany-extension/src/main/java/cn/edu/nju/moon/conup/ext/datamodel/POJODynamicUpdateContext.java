package cn.edu.nju.moon.conup.ext.datamodel;

import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.complifecycle.DynamicUpdateContext;


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
	
	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return isLoaded;
	}
	
	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#removeAlgorithmOldRootTx(java.lang.String)
	 */
	@Override
	public void removeAlgorithmOldRootTx(String oldRootTx){
		LOGGER.fine("in DynaUpdateCtx.removeAlgorithmOldRootTx(), rm " + oldRootTx);
		algorithmOldRootTxs.remove(oldRootTx);
	}
	
	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#setLoaded(boolean)
	 */
	@Override
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#getOldVerClass()
	 */
	@Override
	public Class<?> getOldVerClass() {
		return oldVerClass;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#setOldVerClass(java.lang.Class)
	 */
	@Override
	public void setOldVerClass(Class<?> oldVerClass) {
		this.oldVerClass = oldVerClass;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#getNewVerClass()
	 */
	@Override
	public Class<?> getNewVerClass() {
		return newVerClass;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#setNewVerClass(java.lang.Class)
	 */
	@Override
	public void setNewVerClass(Class<?> newVerClass) {
		this.newVerClass = newVerClass;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#getAlgorithmOldRootTxs()
	 */
	@Override
	public Set<String> getAlgorithmOldRootTxs() {
		return algorithmOldRootTxs;
	}

	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#setAlgorithmOldRootTxs(java.util.Set)
	 */
	@Override
	public void setAlgorithmOldRootTxs(Set<String> algorithmOldRootTxs) {
		this.algorithmOldRootTxs = algorithmOldRootTxs;
	}
	
	/* (non-Javadoc)
	 * @see cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext#isOldRootTxsInitiated()
	 */
	@Override
	public boolean isOldRootTxsInitiated(){
		return algorithmOldRootTxs != null;
	}
	
}
