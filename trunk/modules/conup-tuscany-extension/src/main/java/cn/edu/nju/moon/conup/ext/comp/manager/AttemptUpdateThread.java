package cn.edu.nju.moon.conup.ext.comp.manager;


import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifecycleManager;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class AttemptUpdateThread extends Thread {
	private static final Logger LOGGER = Logger.getLogger(AttemptUpdateThread.class.getName());
//	private CompLifecycleManager compLcMgr = null;
	private UpdateManager updateManager = null;
	private DynamicDepManager depMgr = null;
//	public AttemptUpdateThread(CompLifecycleManager compLcMgr, DynamicDepManager depMgr){
//		this.compLcMgr = compLcMgr;
//		this.depMgr = depMgr;
//	}
	
	public AttemptUpdateThread(UpdateManager updateManager,
			DynamicDepManager depMgr) {
		this.updateManager = updateManager;
		this.depMgr = depMgr;
	}
	
	public void run(){
		//waiting while on-demand setup
		Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				LOGGER.fine("in compLifeCycleMg, before depMgr.isOndemandSetupRequired()");
				if (depMgr.isOndemandSetupRequired()) {
					LOGGER.fine("----------------in compLifeCycleMg, ondemandSyncMonitor.wait();compLifeCycleMg------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//calculate old version root txs
		Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			DynamicUpdateContext updateCtx;
			updateCtx = updateManager.getUpdateCtx();
			if(depMgr.getCompStatus().equals(CompStatus.VALID) 
					&& updateManager.isDynamicUpdateRqstRCVD()){
				if(!updateCtx.isOldRootTxsInitiated()){
					updateManager.initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = depMgr.getCompObject().getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
				if(freeness.isReadyForUpdate(depMgr.getCompObject().getIdentifier())){
					depMgr.achievedFree();
				} else{
					try {
						LOGGER.fine("not ready for update yet, suspend AttemptUpdateThread------------------");
						validToFreeSyncMonitor.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		updateManager.attemptToUpdate();
	}

//	public void run(){
//		//on-demand setup
////		if (depMgr.isNormal()) {
////			NodeManager nodeMgr;
////			nodeMgr = NodeManager.getInstance();
////			OndemandSetupHelper ondemandHelper;
////			ondemandHelper = nodeMgr.getOndemandSetupHelper(depMgr.getCompObject().getIdentifier());
////			ondemandHelper.ondemandSetup();
////		}
//
//		//waiting while on-demand setup
//		Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
//		synchronized (ondemandSyncMonitor) {
//			try {
//				LOGGER.fine("in compLifeCycleMg, before depMgr.isOndemandSetupRequired()");
//				if (depMgr.isOndemandSetupRequired()) {
//					LOGGER.fine("----------------in compLifeCycleMg, ondemandSyncMonitor.wait();compLifeCycleMg------------");
//					ondemandSyncMonitor.wait();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		//calculate old version root txs
//		Object validToFreeSyncMonitor = depMgr.getValidToFreeSyncMonitor();
//		synchronized (validToFreeSyncMonitor) {
//			DynamicUpdateContext updateCtx;
//			updateCtx = compLcMgr.getUpdateCtx();
//			if(depMgr.getCompStatus().equals(CompStatus.VALID) 
//					&& compLcMgr.isDynamicUpdateRqstRCVD()){
//				if(!updateCtx.isOldRootTxsInitiated()){
//					compLcMgr.initOldRootTxs();
////					Printer printer = new Printer();
////					printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
//				}
//				String freenessConf = depMgr.getCompObject().getFreenessConf();
//				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
//				if(freeness.isReadyForUpdate(depMgr.getCompObject().getIdentifier())){
//					depMgr.achievedFree();
//				} else{
//					try {
//						LOGGER.fine("not ready for update yet, suspend AttemptUpdateThread------------------");
//						validToFreeSyncMonitor.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//		
//		compLcMgr.attemptToUpdate();
//	}
	
}
