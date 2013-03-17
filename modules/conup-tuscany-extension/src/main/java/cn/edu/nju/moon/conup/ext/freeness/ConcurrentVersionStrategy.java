/**
 * 
 */
package cn.edu.nju.moon.conup.ext.freeness;

import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.helper.FreenessCallback;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * Implementation of concurrent version strategy for achieving freeness
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class ConcurrentVersionStrategy implements FreenessStrategy {
	/** represent concurrent version strategy */
	public final static String CONCURRENT_VERSION = "CONCURRENT_VERSION_FOR_FREENESS";
	
	private final static Logger LOGGER = Logger.getLogger(ConcurrentVersionStrategy.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	@Override
	public Class<?> achieveFreeness(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp, FreenessCallback fcb) {
		NodeManager nodeMgr;
		DynamicDepManager depMgr;
		nodeMgr = NodeManager.getInstance();
		depMgr = nodeMgr.getDynamicDepManager(hostComp);
		
//		Set<String> oldVersionRootTxs = depMgr.getOldVersionRootTxs();
		CompLifecycleManager compLcMgr;
		Set<String> algorithmOldVersionRootTxs;
		Set<String> bufferOldVersionRootTxs;
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
//		oldVersionRootTxs = compLcMgr.getDynamicUpdateContext().getOldVerionRootTxs();
		algorithmOldVersionRootTxs = compLcMgr.getUpdateCtx().getAlgorithmOldRootTxs();
		bufferOldVersionRootTxs = compLcMgr.getUpdateCtx().getBufferOldRootTxs();
		synchronized(depMgr.getValidToFreeSyncMonitor()){
			if((algorithmOldVersionRootTxs!=null) 
					&& (algorithmOldVersionRootTxs.contains(rootTxID) || bufferOldVersionRootTxs.contains(rootTxID))){
				LOGGER.fine(rootTxID + " is dispatched to old version");
//				fcb.toOldVersionComp(hostComp);
				return compLcMgr.getUpdateCtx().getOldVerClass();
			}else{
				LOGGER.fine(rootTxID + " is dispatched to new version");
//				fcb.toNewVersionComp(hostComp);
				return compLcMgr.getUpdateCtx().getNewVerClass();
			}
		}
		
	}

	@Override
	public String getFreenessType() {
		return ConcurrentVersionStrategy.CONCURRENT_VERSION;
	}

	@Override
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateRCVD) {
		return false;
	}

	@Override
	public boolean isReadyForUpdate(String hostComp) {
//		DynamicDepManager depManager = NodeManager.getInstance().getDynamicDepManager(hostComp);
		CompLifecycleManager compLcMgr;
		Set<String> oldVersionRootTxs;
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
		oldVersionRootTxs = compLcMgr.getUpdateCtx().getAlgorithmOldRootTxs();
		LOGGER.info("oldVersionRootTxs:\n" + oldVersionRootTxs);
		return compLcMgr.getUpdateCtx().isOldRootTxsEquals()
				&& oldVersionRootTxs.size()==0;
	}

}
