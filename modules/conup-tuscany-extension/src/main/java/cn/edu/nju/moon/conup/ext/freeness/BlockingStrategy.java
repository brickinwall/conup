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
			String curTxID, String hostComp, FreenessCallback fcb) {
		return null;
//		while(isInterceptRequiredForFree(rootTxID, hostComp)){
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public String getFreenessType() {
		return BlockingStrategy.BLOCKING;
	}

	@Override
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateReqRCVD) {
		DynamicDepManager depManager = NodeManager.getInstance().getDynamicDepManager(compIdentifier);
		CompLifecycleManager compLcMgr;
		Set<String> algorithmOldVersionRootTxs;
		if( isUpdateReqRCVD ){
			compLcMgr = CompLifecycleManager.getInstance(compIdentifier);
			algorithmOldVersionRootTxs = compLcMgr.getUpdateCtx().getAlgorithmOldRootTxs();
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
		CompLifecycleManager compLcMgr;
//		Set<String> oldRootTxs;
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
//		Set<String> oldVersionRootTxs;
//		oldVersionRootTxs = compLcMgr.getUpdateCtx().getAlgorithmOldRootTxs();
		return compLcMgr.getUpdateCtx().isOldRootTxsEquals()
				&& depManager.isReadyForUpdate();
//				&& oldVersionRootTxs.size()==0;
	}

}
