//package cn.edu.nju.moon.conup.sample.visitor.launcher;
//
//import org.apache.tuscany.sca.Node;
//import org.apache.tuscany.sca.TuscanyRuntime;
//import org.apache.tuscany.sca.node.ContributionLocationHelper;
//
//import cn.edu.nju.moon.conup.sample.visitor.services.UpdateRequest;
//
//public class LaunchUpdate {
//	private static Node node;
//	public static void main(String[] args) throws Exception {
//		LOGGER.fine("Starting conup-sample-visitor node ....");
//		String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchUpdate.class);
//		
//        //domain uri
//      	String domainUri = "uri:default";
//        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
//        //create Tuscany node
//        Node node = runtime.createNode(domainUri);
//        node.installContribution(contributionURL);
//        node.startComposite("conup-sample-visitor", "update.composite");
//        
//        LOGGER.fine("update.composite ready for big business !!!");
//        
//        //update
//        update(node);
//        
//        System.in.read();
//        LOGGER.fine("Stopping ...");
//        node.stop();
//        LOGGER.fine();
//    }
//	
//	private static void update(Node node){
//		String newVersionClassUri = "/home/nju/deploy/sample/update";
//		String className = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
//		String contributionName = "conup-sample-auth";
//		String compositeName = "auth.composite";
//		new UpdateRequest(LaunchUpdate.node, newVersionClassUri, 
//				className, contributionName, compositeName).start();
//	}
//}
