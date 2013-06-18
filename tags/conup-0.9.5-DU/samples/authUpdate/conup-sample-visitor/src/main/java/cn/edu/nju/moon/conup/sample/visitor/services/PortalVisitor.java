package cn.edu.nju.moon.conup.sample.visitor.services;

import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.portal.services.PortalService;

public class PortalVisitor extends Thread {

	private Node node = null;
	private static Logger LOGGER = Logger.getLogger(VisitorServiceImpl.class.getName());

	public PortalVisitor(Node node) {
		this.node = node;

	}

	private PortalService portalService = null;

	public PortalVisitor(PortalService portalService) {
		this.portalService = portalService;
	}

	public void run() {
		// while(true){
		// PortalService portalService = node.getService(PortalService.class,
		// "PortalComponent#service-binding(PortalService/PortalService)");
		// logger.info("\t" + "" + portalService.execute("nju", "cs"));
		// sleep(1000);
		// }
		LOGGER.fine(portalService.execute("", "nju", "cs"));
	}// END RUN()

}
