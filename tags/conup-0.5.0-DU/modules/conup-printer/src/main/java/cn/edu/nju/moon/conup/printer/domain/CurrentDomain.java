package cn.edu.nju.moon.conup.printer.domain;

import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.impl.NodeImpl;
import org.apache.tuscany.sca.runtime.DomainRegistry;

public class CurrentDomain {
	
	/**
	 * Print info in current node and domain.
	 * @param TuscanyRuntime
	 * @param Node
	 * @return 
	 */
	public static boolean printDomainAndNodeConf(TuscanyRuntime runtime, Node node){
		System.out.println("---------------------Info: current node---------------------");
		System.out.println("Domain name=" + node.getDomainName() + ", Domain URI=" + node.getDomainURI());
		System.out.println("Node names:");
		for(String name : node.getNodeNames())
			System.out.println("\t" + name);
		System.out.println("Installed contribution URIs:");
		for (String uri : node.getInstalledContributionURIs())
			System.out.println("\t" + uri);
		
		System.out.println("---------------------Info: domain registry---------------------");
		DomainRegistry domainRegistry = ((NodeImpl)node).getDomainRegistry();
		System.out.println("Domain name=" + domainRegistry.getDomainName() + ", Domain URI=" + domainRegistry.getDomainURI());
		System.out.println("Node names:");
		for(String name : domainRegistry.getNodeNames())
			System.out.println("\t" + name);
		System.out.println("Installed contribution URIs:");
		for (String uri : domainRegistry.getInstalledContributionURIs())
			System.out.println("\t" + uri);
		System.out.println("Running composite URIs:");
		for(Map.Entry<String, List<String>> entry : domainRegistry.getRunningCompositeURIs().entrySet())
			System.out.println("\t" + entry.getKey() + ", " + entry.getValue());
		System.out.println("Endpoints:");
		for(Endpoint ep : domainRegistry.getEndpoints()){
			System.out.println("\t" + ep + ":" );
			System.out.println("\t\t" + "component=" + ep.getComponent());
			System.out.println("\t\t" + "service=" + ep.getService());
			System.out.println("\t\t" + "isRemote=" + ep.isRemote());
			System.out.println("\t\t" + "isAsyncInvocation=" + ep.isAsyncInvocation());
			System.out.println("\t\t" + "requiredIntents=" + ep.getRequiredIntents());
			System.out.println("\t\t" + "policySets=" + ep.getPolicySets());
			System.out.println("\t\t" + "binding=" + ep.getBinding());
			System.out.println("\t\t" + "deployedUri=" + ep.getDeployedURI());
		}
		System.out.println("Endpoint references:");
		for(EndpointReference er : domainRegistry.getEndpointReferences())
			System.out.println("\t" + er + ": component=" + er.getComponent() + 
											", service=" + er.getReference() + 
											", binding=" + er.getBinding());
		
		
		return true;
	}
	
}//class
