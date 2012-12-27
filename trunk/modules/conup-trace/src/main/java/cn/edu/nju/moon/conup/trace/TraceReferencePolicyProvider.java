package cn.edu.nju.moon.conup.trace;

import java.util.List;

import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

public class TraceReferencePolicyProvider extends
		BasePolicyProvider<TracePolicy> {
	
	public TraceReferencePolicyProvider(EndpointReference endpointReference) {
        super(TracePolicy.class, endpointReference);
    }
	
    public PhasedInterceptor createInterceptor(Operation operation) {
        List<TracePolicy> policies = findPolicies();
//        return policies.isEmpty() ? null : new TracePolicyInterceptor(subject, getContext(), operation, policies, Phase.REFERENCE_POLICY);
        return new TracePolicyInterceptor(subject, getContext(), operation, policies, Phase.REFERENCE_POLICY);
        
    }

}
