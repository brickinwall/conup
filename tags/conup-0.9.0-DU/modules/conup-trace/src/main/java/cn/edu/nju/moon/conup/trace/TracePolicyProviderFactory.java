package cn.edu.nju.moon.conup.trace;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

public class TracePolicyProviderFactory implements PolicyProviderFactory<TracePolicy> {
	private ExtensionPointRegistry registry;
	
    public TracePolicyProviderFactory(ExtensionPointRegistry registry) {
        super();
//    	System.out.println("TracePolicyProviderFactory...");
        this.registry = registry;
    }

	public Class<TracePolicy> getModelType() {
		return TracePolicy.class;
	}

	public PolicyProvider createReferencePolicyProvider(EndpointReference endpointReference) {
//		System.out.println("TracePolicyProviderFactory.createReferencePolicyProvider()");
		return new TraceReferencePolicyProvider(endpointReference);
	}

	public PolicyProvider createServicePolicyProvider(Endpoint endpoint) {
//		System.out.println("TracePolicyProviderFactory.createServicePolicyProvider()");
		return new TraceServicePolicyProvider(endpoint);
	}

	public PolicyProvider createImplementationPolicyProvider(
			RuntimeComponent component) {
//		System.out.println("TracePolicyProviderFactory.createImplementationPolicyProvider()");
		return new TraceImplementationPolicyProvider(component);
	}

}