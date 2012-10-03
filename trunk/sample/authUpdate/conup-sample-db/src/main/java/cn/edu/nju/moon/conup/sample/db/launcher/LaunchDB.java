package cn.edu.nju.moon.conup.sample.db.launcher;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;
import cn.edu.nju.moon.conup.sample.db.services.DBService;


public class LaunchDB {
	/**
	 * distributed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting DB container....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchDB.class);
		String compositeLocation = contributionURL + "db.composite";
		
        VcContainer container = VcContainerImpl.getInstance();
        container.setBusinessComponentName("DBComponent", compositeLocation);
        
        System.out.println("Starting node DB....");
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        String domainName = "cn.edu.nju.moon.version-consistency";
        String userIdPsw = "userid=" + domainName + "&password=njuics";
        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        container.analyseNodeComposite(contributionURL + "db.composite");
        node.installContribution(contributionURL);
//        CompositeAnalyzerImpl
        node.startComposite("conup-sample-db", "db.composite");
        
        //print domain info
        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        //add current business node to container
        container.setBusinessNode(node, "DBComponent");
        
        //access
//        accessServices(node);
        
        System.out.println("db.composite ready for big business !!!");
        System.out.println("container.toString:" + container.toString());
        System.out.println("container.getCommunicationNode:" + container.getCommunicationNode());
        System.out.println("container.getBusinessNode:" + container.getBusinessNode());
        
//        System.out.println("LaunchDB's classloader: " + LaunchDB.class.getClassLoader());
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
	
//	private static void accessServices(Node node) {
//		try {
//			
//			System.out.println("\nTry to access DBComponent#service-binding(DBService/DBService):");
//			DBService db = node.getService(DBService.class, "DBComponent#service-binding(DBService/DBService)");
//			System.out.println("\t" + "" + db.dbOperation());
//			
//		} catch (NoSuchServiceException e) {
//			e.printStackTrace();
//		}
//	}
	
}
