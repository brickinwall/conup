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

package org.apache.tuscany.sca.policy.identity;

import java.util.List;

import javax.security.auth.Subject;

import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.security.SecurityUtil;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * @version $Rev: 792358 $ $Date: 2009-07-09 00:39:36 +0100 (Thu, 09 Jul 2009) $
 */
public class SecurityIdentityImplementationPolicyInterceptor implements PhasedInterceptor {
    private List<SecurityIdentityPolicy> securityIdentityPolicies;
    private Invoker next;

    public SecurityIdentityImplementationPolicyInterceptor(List<SecurityIdentityPolicy> securityIdentityPolicies) {
        super();
        this.securityIdentityPolicies = securityIdentityPolicies;
    }

    /**
     * @see org.apache.tuscany.sca.invocation.Interceptor#getNext()
     */
    public Invoker getNext() {
        return next;
    }

    /**
     * @see org.apache.tuscany.sca.invocation.Interceptor#setNext(org.apache.tuscany.sca.invocation.Invoker)
     */
    public void setNext(Invoker next) {
        this.next = next;
    }
    
    public String getPhase() {
        return Phase.IMPLEMENTATION_POLICY;
    }


    /**
     * @see org.apache.tuscany.sca.invocation.Invoker#invoke(org.apache.tuscany.sca.invocation.Message)
     */
    public Message invoke(Message msg) {
        try {
            
            Subject subject = SecurityUtil.getSubject(msg);
                
            // May do some selection here based on runAs settings.
            // by default though there is nothing to do as the implementation
            // assumes the callers user credentials
            

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
        return getNext().invoke(msg);
    }

}
