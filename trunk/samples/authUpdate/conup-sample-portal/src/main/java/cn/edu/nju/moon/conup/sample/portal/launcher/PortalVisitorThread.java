package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

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
			String executeStr = portalService.execute("", "nju", "cs");
			LOGGER.info(executeStr);
			long endTime = System.nanoTime();
			double responseTime = (endTime - startTime) / 1000000.0;
			LOGGER.fine("responseTime:" + responseTime);
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}
