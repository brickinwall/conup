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

package org.apache.tuscany.sca.binding.ws.axis2.policy.authentication.basic;

import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.authentication.basic.BasicAuthenticationPolicy;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

/**
 * @version $Rev: 938419 $ $Date: 2010-04-27 13:28:09 +0100 (Tue, 27 Apr 2010) $
 */
public class BasicAuthenticationReferencePolicyProvider extends BasePolicyProvider<BasicAuthenticationPolicy> {

    public BasicAuthenticationReferencePolicyProvider(EndpointReference endpointReference) {
        super(BasicAuthenticationPolicy.class, endpointReference);
    }

    public PhasedInterceptor createBindingInterceptor() {
        PolicySet ps = findPolicySet();
        return ps == null ? null : new BasicAuthenticationReferencePolicyInterceptor(getContext(), ps);
    }

    public void start() {
    }

    public void stop() {
    }

}
