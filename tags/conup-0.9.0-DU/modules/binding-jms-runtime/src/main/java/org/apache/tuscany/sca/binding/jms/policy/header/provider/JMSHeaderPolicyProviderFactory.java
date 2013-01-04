/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.binding.jms.policy.header.provider;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.binding.jms.policy.header.JMSHeaderPolicy;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class JMSHeaderPolicyProviderFactory implements PolicyProviderFactory<JMSHeaderPolicy> {
    private ExtensionPointRegistry registry;
    
    public JMSHeaderPolicyProviderFactory(ExtensionPointRegistry registry) {
        super();
        this.registry = registry;
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProviderFactory#createImplementationPolicyProvider(org.apache.tuscany.sca.runtime.RuntimeComponent, org.apache.tuscany.sca.assembly.Implementation)
     */
    public PolicyProvider createImplementationPolicyProvider(RuntimeComponent component, Implementation implementation) {
        return null;
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProviderFactory#createReferencePolicyProvider(org.apache.tuscany.sca.runtime.RuntimeComponent, org.apache.tuscany.sca.runtime.RuntimeComponentReference, org.apache.tuscany.sca.assembly.Binding)
     */
    public PolicyProvider createReferencePolicyProvider(EndpointReference endpointReference) {
        return new JMSHeaderReferencePolicyProvider(endpointReference);
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProviderFactory#createServicePolicyProvider(org.apache.tuscany.sca.runtime.RuntimeComponent, org.apache.tuscany.sca.runtime.RuntimeComponentService, org.apache.tuscany.sca.assembly.Binding)
     */
    public PolicyProvider createServicePolicyProvider(RuntimeComponent component,
                                                      RuntimeComponentService service,
                                                      Binding binding) {
        return null;
    }

    /**
     * @see org.apache.tuscany.sca.provider.ProviderFactory#getModelType()
     */
    public Class getModelType() {        // TODO Auto-generated method stub
        return null;
    }

    public PolicyProvider createImplementationPolicyProvider(RuntimeComponent arg0) {
        return null; // Not used for a Binding
    }

    public PolicyProvider createServicePolicyProvider(Endpoint arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
