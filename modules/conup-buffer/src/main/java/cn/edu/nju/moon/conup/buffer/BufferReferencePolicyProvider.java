package cn.edu.nju.moon.conup.buffer;

import java.util.List;

import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

public class BufferReferencePolicyProvider extends
		BasePolicyProvider<BufferPolicy> {
	
	public BufferReferencePolicyProvider(EndpointReference endpointReference) {
        super(BufferPolicy.class, endpointReference);
    }
	
    public PhasedInterceptor createInterceptor(Operation operation) {
        List<BufferPolicy> policies = findPolicies();
//        return policies.isEmpty() ? null : new TracePolicyInterceptor(subject, getContext(), operation, policies, Phase.REFERENCE_POLICY);
        return new BufferPolicyInterceptor(subject, getContext(), operation, policies, Phase.REFERENCE_POLICY);
        
    }

}
