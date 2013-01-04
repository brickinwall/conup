package cn.edu.nju.moon.conup.printer.composite;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;


public class CompositePrinter {
	/**
     *  print elements in the composite file.
     */
    public static void printCompositeElements(Contribution contribution){
    	Composite composite = contribution.getDeployables().get(0);
    	
    	System.out.println("Print " + composite.getName() + " composite file elements: ");
    	
    	//print policySets
    	System.out.println("Composite PolicySets: ");
    	for(PolicySet policySet : composite.getPolicySets())
    		System.out.println("\t" + policySet);
    	
    	//print intents
    	System.out.println("Composite Required Intents: ");
    	for(Intent intent : composite.getRequiredIntents())
    		System.out.println("\t" + intent);
    	
    	//print components
    	System.out.println("Components: ");
    	for(Component component : composite.getComponents()){
    		//component policysets
    		System.out.println("\t" + component.getName() + ":");
    		System.out.println("\t\tPolicySets:");
    		for(PolicySet ps : component.getPolicySets())
    			System.out.println("\t\t\t" + ps);
    		
    		//component intents
    		System.out.println("\t" + component.getName() + ":");
    		System.out.println("\t\tRequired Intents:");
    		for(Intent intent : component.getRequiredIntents())
    			System.out.println("\t\t\t" + intent);
    		
    		//component implementation policysets
    		System.out.println("\t" + component.getName() + " implementation:");
    		System.out.println("\t\tPolicySets:");
    		for(PolicySet ps : component.getImplementation().getPolicySets())
    			System.out.println("\t\t\t" + ps);
    		
    		//component implementation intents
    		System.out.println("\t" + component.getName() + " implementation:");
    		System.out.println("\t\tRequired Intents:");
    		for(Intent intent : component.getImplementation().getRequiredIntents())
    	    	System.out.println("\t\t\t" + intent);
    	
			// component service policysets
			System.out.println("\t" + component.getName() + " service:");
			System.out.println("\t\tPolicySets:");
			for(ComponentService cs : component.getServices())
				for (PolicySet ps : cs.getPolicySets())
					System.out.println("\t\t\t" + ps);
						
			// component service intents
			System.out.println("\t" + component.getName() + " service:");
			System.out.println("\t\tRequired Intents:");
			for(ComponentService cs : component.getServices())
				for (Intent intent : cs.getRequiredIntents())
					System.out.println("\t\t\t" + intent);		
    		
			// component reference policysets
			System.out.println("\t" + component.getName() + " reference:");
			System.out.println("\t\tPolicySets:");
			for (ComponentReference cr : component.getReferences())
				for (PolicySet ps : cr.getPolicySets())
					System.out.println("\t\t\t" + ps);
			// component reference intents
			System.out.println("\t" + component.getName() + " reference:");
			System.out.println("\t\tRequired Intents:");
			for (ComponentReference cr : component.getReferences())
				for (Intent intent : cr.getRequiredIntents())
					System.out.println("\t\t\t" + intent);
			
//    		Implementation it = component.getImplementation();
//    		for(PolicySet ps : it.getPolicySets())
//    			System.out.print(ps + "\t");
//    		
//    				System.out.println();
//    		System.out.print("\t" + component.getName() + ": Required intents=");
//    		for(Intent intent : component.getRequiredIntents())
//    			System.out.print(intent + "\t");
//    		System.out.println();
    	}
    		
    	System.out.println();
    	
    }
}
