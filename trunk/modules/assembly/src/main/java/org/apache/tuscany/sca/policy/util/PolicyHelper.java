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

package org.apache.tuscany.sca.policy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * A utility that helps to navigate the policy model
 * @tuscany.spi.extension.asclient
 */
public class PolicyHelper {
    public static PolicySet getPolicySet(PolicySubject subject, QName policySetName) {
        for (PolicySet ps : subject.getPolicySets()) {
            if (ps.getName().equals(policySetName)) {
                return ps;
            }
        }
        return null;
    }

    public static Intent getIntent(Definitions subject, QName intentName) {
        for (Intent i : subject.getIntents()) {
            if (i.getName().equals(intentName)) {
                return i;
            }
            for (Intent qualifiedIntent : i.getQualifiedIntents()){
                if (qualifiedIntent.getName().equals(intentName)) {
                    return qualifiedIntent;
                }
            }
        }
        return null;
    }

    public static PolicySet getPolicySet(Definitions subject, QName policySetName) {
        for (PolicySet ps : subject.getPolicySets()) {
            if (ps.getName().equals(policySetName)) {
                return ps;
            }
        }
        return null;
    }

    public static Intent getIntent(PolicySubject subject, QName intentName) {
        for (Intent i : subject.getRequiredIntents()) {
            if (i.getName().equals(intentName)) {
                return i;
            }
        }
        return null;
    }

    public static Collection<PolicyExpression> getPolicyExpressions(PolicySubject subject, QName policyName) {
        Collection<PolicyExpression> policies = new ArrayList<PolicyExpression>();
        for (PolicySet ps : subject.getPolicySets()) {
            for (PolicyExpression exp : ps.getPolicies()) {
                if (exp.getName().equals(policyName)) {
                    policies.add(exp);
                }
            }
        }
        return policies;
    }

    public static Collection<Object> getPolicies(PolicySubject subject, QName policyName) {
        Collection<Object> policies = new ArrayList<Object>();
        for (PolicySet ps : subject.getPolicySets()) {
            for (PolicyExpression exp : ps.getPolicies()) {
                if (exp.getName().equals(policyName)) {
                    policies.add(exp.getPolicy());
                }
            }
        }
        return policies;
    }   

    public static boolean isIntentRequired(PolicySubject subject, QName intent) {

        List<Intent> intents = ((PolicySubject)subject).getRequiredIntents();
        for (Intent i : intents) {
            if (intent.equals(i.getName())) {
                return true;
            }
        }

        return getPolicySet(subject, intent) != null;
    }    

}
