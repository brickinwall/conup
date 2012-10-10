package cn.edu.nju.moon.conup.sample.auth2.launcher;


import java.io.File;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;

import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;


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
        container.analyseNodeComposite(contributionURL + "auth.composite");
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-auth2", "auth.composite");
        
        //print domain info
        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        //add current business node to container
        container.setBusinessNode(node, "AuthComponent");
        
        System.out.println("auth.composite ready for big business !!!");
        System.out.println("container.toString:" + container.toString());
        System.out.println("container.getCommunicationNode:" + container.getCommunicationNode());
        System.out.println("container.getBusinessNode:" + container.getBusinessNode());
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
	
}
