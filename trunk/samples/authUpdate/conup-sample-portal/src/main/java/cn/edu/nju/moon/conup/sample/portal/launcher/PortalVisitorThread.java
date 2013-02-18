package cn.edu.nju.moon.conup.sample.portal.launcher;

import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.portal.services.PortalService;

public class PortalVisitorThread extends Thread {
	private Node node;

	public PortalVisitorThread(Node node) {
		this.node = node;
	}

	public void run() {
		try {

			String targetComponent = "AuthComponent";
			String freenessSetup = "ConcurrentVersion";
			// DomainComponentUpdateService domainComponentUpdateService =
			// node.getService(DomainComponentUpdateService.class,
			// "DomainManagerComponent#service-binding(DomainComponentUpdateService/DomainComponentUpdateService)");
			// boolean updateResult =
			// domainComponentUpdateService.onDemandRequest(targetComponent,
			// freenessSetup);
			// LOGGER.fine("updateResult" + updateResult);

			PortalService portalService = node.getService(PortalService.class, "PortalComponent#service-binding(PortalService/PortalService)");
			portalService.execute("", "nju", "cs");
//			LOGGER.fine("\t" + "" + portalService.execute("", "nju", "cs"));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}// END RUN()
}
