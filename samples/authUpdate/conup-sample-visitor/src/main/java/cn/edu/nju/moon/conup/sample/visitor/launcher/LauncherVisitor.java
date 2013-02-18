package cn.edu.nju.moon.conup.sample.visitor.launcher;


import java.util.logging.Logger;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.oasisopen.sca.NoSuchServiceException;

import cn.edu.nju.moon.conup.sample.visitor.services.VisitorService;

public class LauncherVisitor {
	public static Node node;
	private static Logger LOGGER = Logger.getLogger(LauncherVisitor.class.getName());
	public static void main(String[] args) throws Exception {
		LOGGER.fine("Starting conup-sample-visitor node ....");
		String contributionURL = ContributionLocationHelper.getContributionLocation(LauncherVisitor.class);
		
        //domain uri
      	String domainUri = "uri:default";
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        //create Tuscany node
        Node node = runtime.createNode(domainUri);
        node.installContribution(contributionURL);
        node.startComposite("conup-sample-visitor", "visitor.composite");
        
        LOGGER.fine("visitor.composite ready for big business !!!");
        
        //access
        visit(node);
        
        System.in.read();
        LOGGER.fine("Stopping ...");
        node.stop();
        
		
    }
	
	private static void visit(Node node){
		try {
			VisitorService visitorService = node.getService(VisitorService.class, 
					    "VisitorComponent#service-binding(VisitorService/VisitorService)");
			visitorService.visitPortal(1);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		}
//		new ContinuousVisitor(LauncherVisitor.node).start();
	}
}
