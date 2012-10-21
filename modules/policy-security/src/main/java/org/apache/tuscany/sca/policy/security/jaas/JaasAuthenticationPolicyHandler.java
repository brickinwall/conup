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

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Policy handler to handle PolicySet containing JaasAuthenticationPolicy instances
 *
 * @version $Rev: 796166 $ $Date: 2009-07-21 08:03:47 +0100 (Tue, 21 Jul 2009) $
 */
public class JaasAuthenticationPolicyHandler {
    private static final String jaasPolicy = "JaasPolicy";
    private static final String SCA10_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";
    public static final QName policySetQName = new QName(SCA10_TUSCANY_NS, jaasPolicy);
    private PolicySet applicablePolicySet = null;

    public void setUp(Object... context) {
        if (applicablePolicySet != null) {
        }
    }

    public void cleanUp(Object... context) {
    }

    public void beforeInvoke(Object... context) {
        try {
            JaasAuthenticationPolicy policy = (JaasAuthenticationPolicy)applicablePolicySet.getPolicies().get(0);
            CallbackHandler callbackHandler =
                (CallbackHandler)policy.getCallbackHandlerClass().newInstance();
            LoginContext lc = new LoginContext(policy.getConfigurationName(), callbackHandler);
            lc.login();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void afterInvoke(Object... context) {

    }

    public PolicySet getApplicablePolicySet() {
        return applicablePolicySet;
    }

    public void setApplicablePolicySet(PolicySet applicablePolicySet) {
        this.applicablePolicySet = applicablePolicySet;
    }
}
