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
package org.apache.tuscany.sca.policy.impl;

import org.apache.tuscany.sca.policy.BindingType;
import org.apache.tuscany.sca.policy.DefaultIntent;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.ImplementationType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentMap;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.Qualifier;

import org.apache.tuscany.sca.policy.ExternalAttachment;

/**
 * A factory for the policy model.
 * 
 * @version $Rev: 1209146 $ $Date: 2011-12-01 16:45:57 +0000 (Thu, 01 Dec 2011) $
 */
public abstract class PolicyFactoryImpl implements PolicyFactory {

    public Intent createIntent() {
        return new IntentImpl();
    }

    public PolicySet createPolicySet() {
        return new PolicySetImpl();
    }

    public IntentMap createIntentMap() {
        return new IntentMapImpl();
    }

    public Qualifier createQualifier() {
        return new QualifierImpl();
    }

    public PolicyExpression createPolicyExpression() {
        return new PolicyExpressionImpl();
    }
    
    public BindingType createBindingType() {
        return new BindingTypeImpl();
    }
    
    public ImplementationType createImplementationType() {
        return new ImplementationTypeImpl();
    }

    public ExtensionType createExtensionType() {
        return new ExtensionTypeImpl();
    }
    
    public ExternalAttachment createExternalAttachment() {
    	return new ExternalAttachmentImpl();
    }

    public DefaultIntent createDefaultIntent() {
        return new DefaultIntentImpl();
    }
}
