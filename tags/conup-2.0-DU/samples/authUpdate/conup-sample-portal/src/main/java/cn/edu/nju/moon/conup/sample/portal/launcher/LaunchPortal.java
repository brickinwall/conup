package cn.edu.nju.moon.conup.sample.portal.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.oasisopen.sca.NoSuchServiceException;


import cn.edu.nju.moon.conup.communication.services.ComponentConfService;
import cn.edu.nju.moon.conup.communication.services.ComponentUpdateService;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;
import cn.edu.nju.moon.conup.sample.portal.services.PortalService;
import cn.edu.nju.moon.conup.sample.portal.services.ProcService;
import cn.edu.nju.moon.conup.sample.portal.services.TokenService;

public class LaunchPortal {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting Portal container....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchPortal.class);
		String compositeLocation = contributionURL + "portal.composite";
		
		VcContainer container = VcContainerImpl.getInstance();
		//contribution's absolute path 
        File file = new File("");
        String absContributionPath = file.getAbsolutePath();
        absContributionPath += File.separator + "target" + File.separator + "classes";
        //domain uri
      	String domainUri = null;
      	domainUri = container.getDomainUri();
//      String domainName = "cn.edu.nju.moon.version-consistency";
//      String userIdPsw = "userid=" + domainName + "&password=njuics";
//      String domainUri = "uri:" + domainName + "?" + userIdPsw;
        container.setBusinessComponentName("PortalComponent", compositeLocation, absContributionPath, null, domainUri);
        
		System.out.println("Starting node portal....");
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
		container.analyseNodeComposite(contributionURL + "portal.composite");
		node.installContribution(contributionURL);
		node.startComposite("conup-sample-portal", "portal.composite");

		//print domain info
		CurrentDomain.printDomainAndNodeConf(runtime, node);
		
		////add current business node to container
		container.setBusinessNode(node, "PortalComponent");
		
		//access services
//		accessServices(node);
		
		System.out.println("portal.composite ready for big business !!!");
		System.out.println("container.toString:" + container.toString());
        System.out.println("container.getCommunicationNode:" + container.getCommunicationNode());
        System.out.println("container.getBusinessNode:" + container.getBusinessNode());
		
		System.in.read();
		System.out.println("Stopping ...");
		node.stop();
		System.out.println();
	}
	
	private static void accessServices(Node node) throws InterruptedException {
//		int threadNum = 3;
//		for(int i=0; i<threadNum; i++){
//			System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
//			new PortalVisitorThread(node).start();
//			Thread.sleep(2000);
//		}
		
		try {
			System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
			PortalService portalService = node.getService(PortalService.class, 
				    "PortalComponent#service-binding(PortalService/PortalService)");
			System.out.println("\t" + "" + portalService.execute("nju", "cs"));
			
//			String baseDir = "/home/nju/deploy/sample/update/";
//			String classpath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
//			String contributionURI = "conup-sample-auth";
//			String compositeURI = "auth.composite";
//			System.out.println("\nTry to access AuthComponentComm" +
//					"#service-binding(ComponentUpdateService/ComponentUpdateService):");
//			ComponentUpdateService authComm = node.getService(ComponentUpdateService.class, 
//					"AuthComponentComm#service-binding(ComponentUpdateService/ComponentUpdateService)");
//			System.out.println("\t" + "authComm.update: " + 
//					authComm.update(baseDir, classpath, contributionURI, compositeURI));
			
//			System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
//			PortalService portalService2 = node.getService(PortalService.class, 
//				    "PortalComponent#service-binding(PortalService/PortalService)");
//			System.out.println("\t" + "" + portalService2.execute("nju", "cs"));
			
//			System.out.println("Try to access AuthComponentComm#service-binding(ComponentConfService/ComponentConfService)");
//			ComponentConfService compConfService = node.getService(ComponentConfService.class, 
//				    "AuthComponentComm#service-binding(ComponentConfService/ComponentConfService)");
//			System.out.println("\t" + "" + compConfService.getAllStatuses());
//			System.out.println("\t" + "" + compConfService.getStartedCompositeUri());
//			
//			System.out.println("\nTry to access AuthComponentComm" +
//					"#service-binding(ComponentUpdateService/ComponentUpdateService):");
//			authComm = node.getService(ComponentUpdateService.class, 
//					"AuthComponentComm#service-binding(ComponentUpdateService/ComponentUpdateService)");
//			System.out.println("\t" + "authComm.update: " + 
//					authComm.update(baseDir, classpath, contributionURI, compositeURI));
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
		
	}
	
}
