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
package org.apache.tuscany.sca.policy.security.jaas;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.provider.BasePolicyProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * Policy handler to handle PolicySet containing JaasAuthenticationPolicy instances
 *
 * @version $Rev: 1186226 $ $Date: 2011-10-19 15:03:07 +0100 (Wed, 19 Oct 2011) $
 */
public class JaasAuthenticationImplementationPolicyProvider extends BasePolicyProvider<JaasAuthenticationPolicy> {
    private RuntimeComponent component;
    private Implementation implementation;

    public JaasAuthenticationImplementationPolicyProvider(RuntimeComponent component) {
        super(JaasAuthenticationPolicy.class, component.getImplementation());
        this.component = component;
        this.implementation = component.getImplementation();
    }

    public PhasedInterceptor createInterceptor(Operation operation) {
        List<JaasAuthenticationPolicy> policies = findPolicies(operation);
        if (policies == null || policies.isEmpty()) {
            return null;
        } else {
            return new JaasAuthenticationInterceptor(findPolicies(operation));
        }
    }

    public void start() {
    }

    public void stop() {
    }    
}
