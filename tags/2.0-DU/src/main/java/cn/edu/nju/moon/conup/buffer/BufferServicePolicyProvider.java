package cn.edu.nju.moon.conup.buffer;

import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

public class BufferServicePolicyProvider extends BasePolicyProvider<BufferPolicy> {
	private Endpoint endpoint;
	
    public BufferServicePolicyProvider(Endpoint endpoint) {
        super(BufferPolicy.class, endpoint);
        
        this.endpoint = endpoint;
    }

    public PhasedInterceptor createInterceptor(Operation operation) {
    	ComponentService componentSevice = endpoint.getService();
    	List<PolicySet> policySets = componentSevice.getPolicySets();
    	if(subject.getPolicySets().isEmpty()){
//    		for(PolicySet ps : policySets){
//    			List<PolicyExpression> policyExtensions = ps.getPolicies();
//    			for(PolicyExpression pe : policyExtensions)
//    				System.out.print("PolicyExtensions:" + pe + "  ");
//    			System.out.println();
//    		}
    		subject.getPolicySets().addAll(policySets);
    	}
    		
    	
//    	System.out.println("TraceServicePolicyProvider related policysets:");
//    	for(PolicySet ps : subject.getPolicySets())
//    		System.out.println("\t" + ps);
    	
    	List<BufferPolicy> policies = findPolicies();
    	return new BufferPolicyInterceptor(subject, getContext(), operation, policies, Phase.SERVICE_POLICY);
//    	return policies.isEmpty()? null : new TracePolicyInterceptor(subject, getContext(), operation, policies, Phase.SERVICE_POLICY);
    	

    }
	
}
