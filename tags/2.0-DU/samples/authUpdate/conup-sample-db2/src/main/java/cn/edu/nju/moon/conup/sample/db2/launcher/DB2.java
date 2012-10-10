package cn.edu.nju.moon.conup.sample.db2.launcher;

import java.io.File;

import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.container.VcContainer;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.printer.domain.CurrentDomain;


public class DB2 {
	/**
	 * distributed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting DB 2 container....");
        String contributionURL = ContributionLocationHelper.getContributionLocation(DB2.class);
        String compositeLocation = contributionURL + "db.composite";
        
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
        container.setBusinessComponentName("DB2Component", compositeLocation, absContributionPath, null, domainUri);
        
        System.out.println("Starting node DB....");
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        container.analyseNodeComposite(contributionURL + "db.composite");
        node.installContribution(contributionURL);
//        CompositeAnalyzerImpl
        node.startComposite("conup-sample-db2", "db.composite");
        
        //print domain info
        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        //add current business node to container
        container.setBusinessNode(node, "DB2Component");
        
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
	
}
