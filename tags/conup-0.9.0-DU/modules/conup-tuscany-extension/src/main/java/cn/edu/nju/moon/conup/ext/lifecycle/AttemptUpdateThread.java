package cn.edu.nju.moon.conup.ext.lifecycle;


import cn.edu.nju.moon.conup.ext.datamodel.DynamicUpdateContext;
import cn.edu.nju.moon.conup.ext.update.UpdateFactory;
import cn.edu.nju.moon.conup.spi.datamodel.CompStatus;
import cn.edu.nju.moon.conup.spi.datamodel.FreenessStrategy;
import cn.edu.nju.moon.conup.spi.manager.DynamicDepManager;
import cn.edu.nju.moon.conup.spi.utils.Printer;

/**
 * @author JiangWang<jiang.wang88@gmail.com>
 *
 */
public class AttemptUpdateThread extends Thread {
	private CompLifecycleManager compLcMgr = null;
	private DynamicDepManager depMgr = null;
	public AttemptUpdateThread(CompLifecycleManager compLcMgr, DynamicDepManager depMgr){
		this.compLcMgr = compLcMgr;
		this.depMgr = depMgr;
	}
	
	public void run(){
		//on-demand setup
//		if (depMgr.isNormal()) {
//			NodeManager nodeMgr;
//			nodeMgr = NodeManager.getInstance();
//			OndemandSetupHelper ondemandHelper;
//			ondemandHelper = nodeMgr.getOndemandSetupHelper(depMgr.getCompObject().getIdentifier());
//			ondemandHelper.ondemandSetup();
//		}

		//waiting while on-demand setup
		Object ondemandSyncMonitor = depMgr.getOndemandSyncMonitor();
		synchronized (ondemandSyncMonitor) {
			try {
				System.out.println("in compLifeCycleMg, before depMgr.isOndemandSetupRequired()");
				if (depMgr.isOndemandSetupRequired()) {
					System.out.println("----------------in compLifeCycleMg, ondemandSyncMonitor.wait();compLifeCycleMg------------");
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
			updateCtx = compLcMgr.getUpdateCtx();
			if(depMgr.getCompStatus().equals(CompStatus.VALID) 
					&& compLcMgr.isDynamicUpdateRqstRCVD()){
				if(!updateCtx.isOldRootTxsInitiated()){
					compLcMgr.initOldRootTxs();
//					Printer printer = new Printer();
//					printer.printDeps(depMgr.getRuntimeInDeps(), "inDeps:");
				}
				String freenessConf = depMgr.getCompObject().getFreenessConf();
				FreenessStrategy freeness = UpdateFactory.createFreenessStrategy(freenessConf);
				if(freeness.isReadyForUpdate(depMgr.getCompObject().getIdentifier())){
					depMgr.achievedFree();
				} else{
					try {
						System.out.println("not ready for update yet, suspend AttemptUpdateThread------------------");
						validToFreeSyncMonitor.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		compLcMgr.attemptToUpdate();
	}
	
}
