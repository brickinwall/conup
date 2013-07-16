package cn.edu.nju.moon.conup.ext.freeness;

import java.util.Set;
import java.util.logging.Logger;

import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * Implementation of blocking strategy for achieving freeness
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class BlockingStrategy implements FreenessStrategy {
	private Logger LOGGER = Logger.getLogger(BlockingStrategy.class.getName());
	/** represent blocking strategy */
	public final static String BLOCKING = "BLOCKING_FOR_FREENESS";
	
	@Override
	public Class<?> achieveFreeness(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp) {
		return null;
	}

	@Override
	public String getFreenessType() {
		return BlockingStrategy.BLOCKING;
	}

	@Override
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateReqRCVD) {
		NodeManager nodeMgr = NodeManager.getInstance();
		DynamicDepManager depManager = nodeMgr.getDynamicDepManager(compIdentifier);
		UpdateManager updateMgr = nodeMgr.getUpdateManageer(compIdentifier);
//		CompLifecycleManager compLcMgr;
		Set<String> algorithmOldVersionRootTxs;
		if( isUpdateReqRCVD ){
//			compLcMgr = CompLifecycleManagerImpl.getInstance(compIdentifier);
			algorithmOldVersionRootTxs = updateMgr.getUpdateCtx().getAlgorithmOldRootTxs();
		} else{
			algorithmOldVersionRootTxs = null;
		}
		
		boolean isBlock = depManager.isBlockRequiredForFree(algorithmOldVersionRootTxs, txCtx, isUpdateReqRCVD);
		
		if(isBlock){
			LOGGER.info(txCtx.getRootTx() + " is blocked, \n algorithm:" + algorithmOldVersionRootTxs);
		}
		
		return isBlock;
	}

	@Override
	public boolean isReadyForUpdate(String hostComp) {
		DynamicDepManager depManager = NodeManager.getInstance().getDynamicDepManager(hostComp);
		return depManager.isReadyForUpdate();
	}

}
