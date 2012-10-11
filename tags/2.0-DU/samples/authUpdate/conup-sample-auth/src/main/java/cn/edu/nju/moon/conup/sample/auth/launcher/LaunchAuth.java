package cn.edu.nju.moon.conup.sample.auth.launcher;


import java.io.File;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.communication.services.ComponentUpdateService;
import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;
import cn.edu.nju.moon.conup.sample.auth.services.TokenService;


public class LaunchAuth {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting Auth container....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchAuth.class);
		String compositeLocation = contributionURL + "auth.composite";
		
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
        container.setBusinessComponentName("AuthComponent", compositeLocation, absContributionPath, null, domainUri);

        System.out.println("Starting auth node ....");
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
//        container.analyseNodeComposite(contributionURL + "auth.composite");
        container.analyseNodeComposite(compositeLocation);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-auth", "auth.composite");
        
        //print domain info
        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        //add current business node to container
        container.setBusinessNode(node, "AuthComponent");
        
        //access
//        accessServices(node);
        
        System.out.println("auth.composite ready for big business !!!");
        System.out.println("container.toString:" + container.toString());
        System.out.println("container.getCommunicationNode:" + container.getCommunicationNode());
        System.out.println("container.getBusinessNode:" + container.getBusinessNode());
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
	private static void accessServices(Node node) {
		try {
			System.out.println("\nTry to access AuthComponent" +
					"#service-binding(TokenService/TokenService):");
			TokenService oldTokenService = node.getService(TokenService.class, 
					"AuthComponent#service-binding(TokenService/TokenService)");
			System.out.println("\toldTokenService.getToken: " + "" + oldTokenService.getToken("nju,cs"));
			
			String baseDir = "/home/nju/deploy/sample/update/";
			String classpath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
			String contributionURI = "conup-sample-auth";
			String compositeURI = "auth.composite";
			System.out.println("\nTry to access AuthComponentComm" +
					"#service-binding(ComponentUpdateService/ComponentUpdateService):");
			ComponentUpdateService authComm = node.getService(ComponentUpdateService.class, 
					"AuthComponentComm#service-binding(ComponentUpdateService/ComponentUpdateService)");
			System.out.println("\t" + "authComm.update: " + 
					authComm.update(baseDir, classpath, contributionURI, compositeURI));
			
			System.out.println("\nTry to access AuthComponent" +
					"#service-binding(TokenService/TokenService):");
			TokenService newTokenService = node.getService(TokenService.class, 
					"AuthComponent#service-binding(TokenService/TokenService)");
			System.out.println("\newTokenService.getToken: " + "" + newTokenService.getToken("nju,cs"));
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}
}
