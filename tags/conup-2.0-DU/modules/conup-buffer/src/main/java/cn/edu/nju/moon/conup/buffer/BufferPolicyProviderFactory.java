package cn.edu.nju.moon.conup.buffer;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

public class BufferPolicyProviderFactory implements PolicyProviderFactory<BufferPolicy> {
	private ExtensionPointRegistry registry;
	
    public BufferPolicyProviderFactory(ExtensionPointRegistry registry) {
        super();
//    	System.out.println("TracePolicyProviderFactory...");
        this.registry = registry;
    }

	public Class<BufferPolicy> getModelType() {
		return BufferPolicy.class;
	}

	public PolicyProvider createReferencePolicyProvider(EndpointReference endpointReference) {
//		System.out.println("TracePolicyProviderFactory.createReferencePolicyProvider()");
		return new BufferReferencePolicyProvider(endpointReference);
	}

	public PolicyProvider createServicePolicyProvider(Endpoint endpoint) {
//		System.out.println("TracePolicyProviderFactory.createServicePolicyProvider()");
		return new BufferServicePolicyProvider(endpoint);
	}

	public PolicyProvider createImplementationPolicyProvider(
			RuntimeComponent component) {
//		System.out.println("TracePolicyProviderFactory.createImplementationPolicyProvider()");
		return new BufferImplementationPolicyProvider(component);
	}

}
