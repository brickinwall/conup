package cn.edu.nju.moon.conup.printer.policy;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.policy.BindingType;
import org.apache.tuscany.sca.policy.ImplementationType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

public class DefinitionPrinter {
	/**
     * print system definitions or self-defined definition.
     * @param Definitions
     */
    public static void printDefinitions(Definitions definitions){
    	System.out.println("Bindings: ");
    	for(Binding binding : definitions.getBindings())
    		System.out.println("\t" + binding + ", ");
    	
    	System.out.println("Binding Types: ");
    	for(BindingType bt : definitions.getBindingTypes()){
    		System.out.println("\t" + bt + ", ");
    	}
    	
    	System.out.println("ImplementationTypes:");
    	for(ImplementationType it : definitions.getImplementationTypes())
    		System.out.println("\t" + it + ", ");
    			
    	System.out.println("PolicySets: ");
    	for(PolicySet ps : definitions.getPolicySets())
    		System.out.println("\t" + ps + ", ");
    			
    	System.out.println("Intents: ");
    	for(Intent intent : definitions.getIntents())
    		System.out.println("\t" + intent + ", ");
    	
    }
}
