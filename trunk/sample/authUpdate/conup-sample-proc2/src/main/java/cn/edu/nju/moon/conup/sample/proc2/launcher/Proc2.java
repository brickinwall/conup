package cn.edu.nju.moon.conup.sample.proc2.launcher;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.apache.tuscany.sca.runtime.DomainRegistry;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;
import cn.edu.nju.moon.conup.sample.proc2.services.DBService;
import cn.edu.nju.moon.conup.sample.proc2.services.ProcService;
import cn.edu.nju.moon.conup.sample.proc2.services.TokenService;
import cn.edu.nju.moon.conup.sample.proc2.services.VerificationService;

public class Proc2 {
	/**
	 * distributed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting Proc 2 container....");
        String contributionURL = ContributionLocationHelper.getContributionLocation(Proc2.class);
        String compositeLocation = contributionURL + "proc.composite";
        
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
        container.setBusinessComponentName("Proc2Component", compositeLocation, absContributionPath, null, domainUri);
		
        System.out.println("\nStarting node Proc....");
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        container.analyseNodeComposite(contributionURL + "proc.composite");
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-proc2", "proc.composite");
        
        //print domain info
        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        //add current business node to container
        container.setBusinessNode(node, "Proc2Component");
        
        //access services
//        accessServices(node);
        
        System.out.println("proc.composite ready for big business !!!");
        System.out.println("container.toString:" + container.toString());
        System.out.println("container.getCommunicationNode:" + container.getCommunicationNode());
        System.out.println("container.getBusinessNode:" + container.getBusinessNode());
        
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
	@Deprecated
	public static Node launch() throws Exception {
        System.out.println("Starting node Proc....");

        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        String domainName = "cn.edu.nju.moon.version-consistency.test";
        String userIdPsw = "userid=" + domainName + "&password=njuics";
        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        String contributionURL = ContributionLocationHelper.getContributionLocation(Proc2.class);
        node.installContribution(contributionURL);
        node.startComposite("vc-policy-proc-node", "proc.composite");
        
        CurrentDomain.printDomainAndNodeConf(runtime, node);
//        accessServices(node);
        
        System.out.println("proc.composite ready for big business !!!");
		
		return node;
	}
	
	private static void accessServices(Node node) {
		try {
//			System.out.println("\nTry to access ProcComponent#service-binding(ProcService/ProcService):");
//			ProcService pi = node.getService(ProcService.class, "ProcComponent#service-binding(ProcService/ProcService)");
//			System.out.println("\t" + "" + pi.process("nju,cs,pass", ""));
//			
//			System.out.println("\nTry to access TokenComponent#service-binding(TokenService/TokenService):");
//			TokenService ts = node.getService(TokenService.class, "TokenComponent#service-binding(TokenService/TokenService)");
//			String token = ts.getToken("nju,cs");
//			System.out.println("\t" + "" + token);
//			
//			System.out.println("\nTry to access VerificationComponent#service-binding(VerificationService/VerificationService):");
//			VerificationService vs = node.getService(VerificationService.class, "VerificationComponent#service-binding(VerificationService/VerificationService)");
//			System.out.println("\t" + "" + vs.verify(token));
			
			System.out.println("\nTry to access DBComponent#service-binding(DBService/DBService):");
			DBService db = node.getService(DBService.class, "DBComponent#service-binding(DBService/DBService)");
			System.out.println("\t" + "" + db.dbOperation());
			
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
	}

}
