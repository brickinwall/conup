package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.ext.utils.experiments.Experiment;
import cn.edu.nju.moon.conup.ext.utils.experiments.model.ExpSetting;
import cn.edu.nju.moon.conup.sample.portal.services.PortalService;

public class PortalVisitorThread extends Thread {
	private static final Logger LOGGER = Logger.getLogger(PortalVisitorThread.class.getName());
	private Node node;
	private int roundId;
	private int threadId;
	
	public PortalVisitorThread(Node node) {
		this.node = node;
	}
	
	public PortalVisitorThread(Node node,int roundId, int threadId) {
		this.node = node;
		this.roundId = roundId;
		this.threadId = threadId;
	}

	public void run() {
		try {
			long startTime = System.nanoTime();
			
			PortalService portalService = node.getService(PortalService.class, "PortalComponent/PortalService");
			portalService.execute("", "nju", "cs");
			long endTime = System.nanoTime();
			double responseTime = (endTime - startTime) / 1000000.0;
			LOGGER.info("responseTime:" + responseTime);
			
			Experiment exp = Experiment.getInstance();
			ExpSetting expSetting = exp.getExpSetting();
			if(expSetting.getType().contains("disruption")){
				String statusWhenStart = "start_status";
				String statusWhenEnd = "end_status";
				exp.writeResponseTimeToFile(roundId, threadId, statusWhenStart, statusWhenEnd, responseTime);
			}
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}
