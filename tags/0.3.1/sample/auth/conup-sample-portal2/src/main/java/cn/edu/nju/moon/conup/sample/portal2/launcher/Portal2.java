package cn.edu.nju.moon.conup.sample.portal2.launcher;

import java.io.BufferedReader;
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


import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.def.InterceptorCache;
import cn.edu.nju.moon.conup.def.InterceptorCacheImpl;
import cn.edu.nju.moon.conup.def.TransactionDependency;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;
import cn.edu.nju.moon.conup.sample.portal2.services.PortalService;
import cn.edu.nju.moon.conup.sample.portal2.services.ProcService;
import cn.edu.nju.moon.conup.sample.portal2.services.TokenService;

public class Portal2 {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting Portal 2 container....");
        VcContainer container = VcContainerImpl.getInstance();
        String contributionURL = ContributionLocationHelper.getContributionLocation(Portal2.class);
        String compositeLocation = contributionURL + "portal.composite";
        
        container.setBusinessComponentName("Portal2Component", compositeLocation);
        
		System.out.println("Starting node portal....");
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		String domainName = "cn.edu.nju.moon.version-consistency";
        String userIdPsw = "userid=" + domainName + "&password=njuics";
        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
		container.analyseNodeComposite(contributionURL + "portal.composite");
		node.installContribution(contributionURL);
		node.startComposite("conup-sample-portal2", "portal.composite");

		//print domain info
		CurrentDomain.printDomainAndNodeConf(runtime, node);
		
		////add current business node to container
		container.setBusinessNode(node, "Portal2Component");
		
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
		int threadNum = 10;
		for(int i=0; i<threadNum; i++){
			System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
			new PortalVisitorThread(node).start();
			Thread.sleep(2000);
		}
	}

	
	@Deprecated
	public static Node launch() throws Exception {
		System.out.println("Starting node portal....");
		
		TuscanyRuntime runtime = TuscanyRuntime.newInstance();
		String domainName = "cn.edu.nju.moon.version-consistency.test";
        String userIdPsw = "userid=" + domainName + "&password=njuics";
        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
		String contributionURL = ContributionLocationHelper.getContributionLocation(Portal2.class);
		node.installContribution(contributionURL);
		node.startComposite("vc-policy-portal-node", "portal.composite");

		CurrentDomain.printDomainAndNodeConf(runtime, node);
		accessServices(node);

		System.out.println("portal.composite ready for big business !!!");
		
		return node;
	}
	
//	@Deprecated
//	private static void accessServices(Node node) {
//		try {
////			InterceptorCache cache = InterceptorCacheImpl.getInstance();
//			
//			System.out.println("Try to access PortalComponent#service-binding(PortalService/PortalService)");
//			PortalService portalService = node.getService(PortalService.class, 
//					"PortalComponent#service-binding(PortalService/PortalService)");
////			System.out.println("\t" + "" + portalService.execute(""));
//			
////			System.out.println("InterceptorCacheImpl:");
////			Set<Entry<String, TransactionDependency>> tmpResult = cache.getDependencies();
////			for(Entry<String, TransactionDependency> entry : tmpResult){
////				System.out.println("\t" + "" + entry.getKey() + ": " + entry.getValue());
////			}
//			
////			System.out.println("Please input username and password [userName password]....");
////			InputStreamReader is = new InputStreamReader(System.in);
////			BufferedReader br = new BufferedReader(is);
////			String info = br.readLine();
////
////			String[] infos = info.split(" ");
////			String name = infos[0];
////			String passwd = infos[1];
////
////			System.out.println("\nTry to access TokenComponent#service-binding(TokenService/TokenService):");
////			TokenService ts = node.getService(TokenService.class,
////							"TokenComponent#service-binding(TokenService/TokenService)");
////			String cred = name + "," + passwd;
////			String token = ts.getToken(cred);
////			System.out.println("\t" + "" + token);
////
////			System.out.println("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
////			ProcService pi = node.getService(ProcService.class,
////							"ProcComponent#service-binding(ProcService/ProcService)");
////			List<String> result = pi.process(token, "");
////			System.out.println("\t" + "" + result);
//						
//			System.out.println();
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
////		} catch (IOException e) {
////			e.printStackTrace();
//		}
//	}
	
}
