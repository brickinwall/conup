package cn.edu.nju.moon.conup.buffer;

import java.util.List;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.provider.BasePolicyProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

public class BufferImplementationPolicyProvider extends	BasePolicyProvider<BufferPolicy> {

	public BufferImplementationPolicyProvider(RuntimeComponent component) {
        super(BufferPolicy.class, component.getImplementation());
    }
	
	/**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#createInterceptor(org.apache.tuscany.sca.interfacedef.Operation)
     */
    public PhasedInterceptor createInterceptor(Operation operation) {
        List<BufferPolicy> policies = findPolicies();
//        return policies.isEmpty() ? null : new TracePolicyInterceptor(subject, getContext(), operation,
//                                                                           policies, Phase.IMPLEMENTATION_POLICY);
    	return new BufferPolicyInterceptor(subject, getContext(), operation,
              policies, Phase.IMPLEMENTATION_POLICY);
    }
	

}
