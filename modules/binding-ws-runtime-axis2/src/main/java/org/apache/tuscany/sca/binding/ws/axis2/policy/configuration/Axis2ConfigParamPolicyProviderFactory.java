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

package org.apache.tuscany.sca.binding.ws.axis2.policy.configuration;


import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * @version $Rev: 916311 $ $Date: 2010-02-25 15:07:17 +0000 (Thu, 25 Feb 2010) $
 */
public class Axis2ConfigParamPolicyProviderFactory implements PolicyProviderFactory<Axis2ConfigParamPolicy> {

    public Axis2ConfigParamPolicyProviderFactory(ExtensionPointRegistry registry) {
        super();
    }

    public PolicyProvider createImplementationPolicyProvider(RuntimeComponent component) {
        return null;
    }

    public PolicyProvider createReferencePolicyProvider(EndpointReference endpointReference) {
        return new Axis2ConfigParamPolicyProvider(endpointReference);
    }

    public PolicyProvider createServicePolicyProvider(Endpoint endpoint) {
        return new Axis2ConfigParamPolicyProvider(endpoint);
    }

    public Class<Axis2ConfigParamPolicy> getModelType() {
        return Axis2ConfigParamPolicy.class;
    }
}
