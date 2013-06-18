//package cn.edu.nju.moon.conup.sample.visitor.launcher;
//
//import org.apache.tuscany.sca.Node;
//import org.apache.tuscany.sca.TuscanyRuntime;
//import org.apache.tuscany.sca.node.ContributionLocationHelper;
//
//import cn.edu.nju.moon.conup.sample.visitor.services.OndemandRequest;
//
///**
// * 	ATTENTION: 
// * 
// * 
// * */
//public class LaunchOndemand {
//	private static Node node;
//	public static void main(String[] args) throws Exception {
//		LOGGER.fine("Starting conup-sample-visitor node ....");
//		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchOndemand.class);
//		
//        //domain uri
//      	String domainUri = "uri:default";
//        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
//        //create Tuscany node
//        Node node = runtime.createNode(domainUri);
//        node.installContribution(contributionURL);
//        node.startComposite("conup-sample-visitor", "ondemand.composite");
//        
//        LOGGER.fine("ondemand.composite ready for big business !!!");
//        
//        //access
//        ondemand(node);
//        
//        System.in.read();
//        LOGGER.fine("Stopping ...");
//        node.stop();
//        LOGGER.fine();
//    }
//	
//	private static void ondemand(Node node){
//		new OndemandRequest(LaunchOndemand.node).start();
//	}
//}
