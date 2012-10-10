package cn.edu.nju.moon.conup.sample.portal2.launcher;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.portal2.services.DomainComponentUpdateService;
import cn.edu.nju.moon.conup.sample.portal2.services.PortalService;

public class PortalVisitorThread extends Thread {
	private Node node;

	public PortalVisitorThread(Node node){
		this.node = node;
	}

	public void run(){
		try {
			String targetComponent = "AuthComponent";
			String freenessSetup = "ConcurrentVersion";
			DomainComponentUpdateService domainComponentUpdateService = node.getService(DomainComponentUpdateService.class, "DomainManagerComponent#service-binding(DomainComponentUpdateService/DomainComponentUpdateService)");
			boolean updateResult = domainComponentUpdateService.onDemandRequest(targetComponent, freenessSetup);
			System.out.println("updateResult" + updateResult);
			
			
			PortalService portalService = node.getService(PortalService.class, 
				    "Portal2Component#service-binding(PortalService/PortalService)");
			System.out.println("\t" + "" + portalService.execute("nju", "cs"));
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}//END RUN()
}
