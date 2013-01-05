package cn.edu.nju.moon.conup.ext.freeness;


import cn.edu.nju.moon.conup.ext.lifecycle.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.datamodel.TransactionContext;
import cn.edu.nju.moon.conup.spi.helper.FreenessCallback;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.manager.NodeManager;

/**
 * Implementation of waiting strategy for achieving freeness
 * @author Jiang Wang <jiang.wang88@gmail.com>
 *
 */
public class WaitingStrategy implements FreenessStrategy {
	/** represent waiting strategy */
	public final static String WAITING = "WAITING_FOR_FREENESS";
	
	@Override
	public Class<?> achieveFreeness(String rootTxID, String rootComp, String parentComp,
			String curTxID, String hostComp, FreenessCallback fcb) {
		return null;
	}

	@Override
	public String getFreenessType() {
		return WaitingStrategy.WAITING;
	}

	@Override
	public boolean isInterceptRequiredForFree(String rootTx, String compIdentifier, TransactionContext txCtx, boolean isUpdateRCVD) {
//		if(NodeManager.getInstance().getDynamicDepManager(compIdentifier).isReadyForUpdate())
//			return true;
		return false;
	}
	
	
//	private Set<Dependence> getDepsBelongToSameRootTx(String rootTxID, Set<Dependence> allDeps){
//		Set<Dependence> result = new HashSet<Dependence>();
//		for (Dependence dep : allDeps) {
//			if(dep.getRootTx().equals(rootTxID)){
//				result.add(dep);
//			}
//		}
//		return result;
//	}

	@Override
	public boolean isReadyForUpdate(String hostComp) {
		DynamicDepManager depManager = NodeManager.getInstance().getDynamicDepManager(hostComp);
		CompLifecycleManager compLcMgr;
		compLcMgr = CompLifecycleManager.getInstance(hostComp);
		/*
		 * As to waiting strategy, in order to achieve free, it won't block any request, which means
		 * that when trying to judge whether the component is ready for update, it should re-calculate
		 * the old root tx, not just when the component received update request.
		 */
		compLcMgr.reinitOldRootTxs();
		return depManager.isReadyForUpdate()
				&& compLcMgr.getUpdateCtx().isOldRootTxsEquals();
	}

}
