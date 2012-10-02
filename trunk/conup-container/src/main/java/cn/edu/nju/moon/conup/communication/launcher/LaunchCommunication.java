package cn.edu.nju.moon.conup.communication.launcher;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.node.ContributionLocationHelper;

import cn.edu.nju.moon.conup.communication.generator.CompositeGenerator;
import cn.edu.nju.moon.conup.communication.generator.CompositeGeneratorImpl;
import cn.edu.nju.moon.conup.communication.generator.VcServiceGenerator;
import cn.edu.nju.moon.conup.communication.generator.VcServiceGeneratorImpl;


public class LaunchCommunication {
	public static Node node;
	public Node launch(String componentName, String businessCompositeLocation) throws Exception {
		System.out.println("Starting communication node ....");
		
		CompositeGenerator compositeGenerator = 
				new CompositeGeneratorImpl(componentName, "cn.edu.nju.moon.conup.communication.services." + componentName + "VcServiceImpl", businessCompositeLocation);
		compositeGenerator.generate();
//		VcServiceGenerator vcServiceGenerator = new VcServiceGeneratorImpl(componentName, "cn.edu.nju.moon.conup.communication.services.VcServiceImpl");
		VcServiceGenerator vcServiceGenerator = new VcServiceGeneratorImpl(componentName, compositeGenerator);
		vcServiceGenerator.generate();
		
        TuscanyRuntime runtime = TuscanyRuntime.newInstance();
        String domainName = "cn.edu.nju.moon.version-consistency";
        String userIdPsw = "userid=" + domainName + "&password=njuics";
        String domainUri = "uri:" + domainName + "?" + userIdPsw;
        //create Tuscany node
        node = runtime.createNode(domainUri);
        String contributionURL = ContributionLocationHelper.getContributionLocation(LaunchCommunication.class);
        node.installContribution(contributionURL);
        String compositeName = compositeGenerator.getCompositeName();
        node.startComposite("conup-container", compositeName);
        
//        CurrentDomain.printDomainAndNodeConf(runtime, node);
        
        System.out.println("communication.composite ready for big business !!!");
        
        return node;
	}
}
