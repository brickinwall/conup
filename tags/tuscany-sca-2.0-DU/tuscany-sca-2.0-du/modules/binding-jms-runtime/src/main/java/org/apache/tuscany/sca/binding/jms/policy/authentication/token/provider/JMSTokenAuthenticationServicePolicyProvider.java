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

package org.apache.tuscany.sca.binding.jms.policy.authentication.token.provider;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.binding.jms.policy.authentication.token.JMSTokenAuthenticationPolicy;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

/**
 * @version $Rev: 906557 $ $Date: 2010-02-04 16:43:19 +0000 (Thu, 04 Feb 2010) $
 */
public class JMSTokenAuthenticationServicePolicyProvider extends BasePolicyProvider<JMSTokenAuthenticationPolicy> {

    private Endpoint endpoint;

    public JMSTokenAuthenticationServicePolicyProvider(Endpoint endpoint) {
        super(JMSTokenAuthenticationPolicy.class, endpoint);
        this.endpoint = endpoint;
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#createInterceptor(org.apache.tuscany.sca.interfacedef.Operation)
     */
    public PhasedInterceptor createInterceptor(Operation operation) {
        PolicySet ps = findPolicySet();
        return ps == null ? null : new JMSTokenAuthenticationServicePolicyInterceptor(getContext(), ps, getPhase());
    }   

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#getPhase()
     */
    public String getPhase() {
        return Phase.SERVICE_BINDING_POLICY;
    }

}
