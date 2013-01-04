package cn.edu.nju.moon.conup.domain.launcher;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;



public class LaunchDomainManager {
	public static Set<String> createdTransactions = new ConcurrentSkipListSet<String>();
	public static Node node = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        System.out.println("Starting domain-manager node ....");
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //domain uri
        String domainUri = null;
        domainUri = "uri:default";
//        String domainName = "cn.edu.nju.moon.version-consistency";
//        String userIdPsw = "userid=" + domainName + "&password=njuics";
//        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        node = runtime.createNode(domainUri);
        String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchDomainManager.class);
        node.installContribution(contributionURL);
        node.startComposite("conup-domain-manager", "DomainManager.composite");
        
        //print domain info
//        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        System.out.println("DomainManager.composite ready for big business !!!");
        
        //simulate test Freeness
        FreenessTest freenessTest = new FreenessTest(node);
        freenessTest.test();
        
        System.in.read();
        System.out.println("Stopping ...");
        node.stop();
        System.out.println();
    }
}
