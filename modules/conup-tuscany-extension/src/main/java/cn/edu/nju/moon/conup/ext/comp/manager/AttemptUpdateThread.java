package cn.edu.nju.moon.conup.ext.comp.manager;

import java.util.logging.Logger;

import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.update.CompLifeCycleManager;
import cn.edu.nju.moon.conup.spi.update.DynamicUpdateContext;
import cn.edu.nju.moon.conup.spi.update.UpdateManager;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class AttemptUpdateThread extends Thread {
	private static final Logger LOGGER = Logger.getLogger(AttemptUpdateThread.class.getName());
	private CompLifeCycleManager compLifeCycleMgr = null;
	private UpdateManager updateManager = null;
//	private DynamicDepManager depMgr = null;
	
	public AttemptUpdateThread(UpdateManager updateManager,
			CompLifeCycleManager compLifeCycleMgr) {
		this.updateManager = updateManager;
		this.compLifeCycleMgr = compLifeCycleMgr;
	}
	
	public void run(){
		//waiting while on-demand setup
		Object ondemandSyncMonitor = compLifeCycleMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				LOGGER.fine("in compLifeCycleMg, before depMgr.isOndemandSetupRequired()");
				if (compLifeCycleMgr.isOndemandSetupRequired()) {
					LOGGER.fine("----------------in compLifeCycleMg, ondemandSyncMonitor.wait();compLifeCycleMg------------");
					ondemandSyncMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//calculate old version root txs
		Object validToFreeSyncMonitor = compLifeCycleMgr.getValidToFreeSyncMonitor();
		synchronized (validToFreeSyncMonitor) {
			DynamicUpdateContext updateCtx;
			updateCtx = updateManager.getUpdateCtx();
			if(compLifeCycleMgr.isValid()
					&& updateManager.isDynamicUpdateRqstRCVD()){
				if(!updateCtx.isOldRootTxsInitiated()){
					updateManager.initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = compLifeCycleMgr.getCompObject().getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf, compLifeCycleMgr);
				if(freeness.isReadyForUpdate(compLifeCycleMgr.getCompObject().getIdentifier())){
					compLifeCycleMgr.achievedFree();
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

}
