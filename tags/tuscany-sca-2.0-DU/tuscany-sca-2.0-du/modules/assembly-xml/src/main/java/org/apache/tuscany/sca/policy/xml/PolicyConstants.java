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
package org.apache.tuscany.sca.policy.xml;

import javax.xml.namespace.QName;

/**
 * constants related to policy framework
 *
 * @version $Rev: 1295144 $ $Date: 2012-02-29 15:06:57 +0000 (Wed, 29 Feb 2012) $
 */
public interface PolicyConstants {
    String WHITE_SPACE = " ";
    String COLON = ":";
    String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    String TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";
    String INTENT = "intent";
    String POLICY_SET = "policySet";
    String POLICY_SET_REFERENCE = "policySetReference";
    String INTENT_MAP = "intentMap";
    String NAME = "name";
    String TARGET_NAMESPACE = "targetNamespace";
    String SCA_DEFINITIONS = "definitions";
    String CONSTRAINS = "constrains";
    String DESCRIPTION = "description";
    String PROVIDES = "provides";
    String APPLIES_TO = "appliesTo";
    String ATTACH_TO = "attachTo";
    String ALWAYS_APPLIES_TO = "alwaysAppliesTo";
    String QUALIFIER = ".";
    String INTENT_QUALIFIER = "qualifier";
    String INTENT_MAP_QUALIFIER = "qualifier";
    String REQUIRES = "requires";
    String EXCLUDES = "excludes";
    String DEFAULT = "default";
    String EXTERNAL_ATTACHMENT = "externalAttachment";
    String INTENTS = "intents";
    String POLICY_SETS = "policySets";
    String MUTUALLY_EXCLUSIVE = "mutuallyExclusive";
    
    String ALWAYS_PROVIDES = "alwaysProvides";
    String MAY_PROVIDE = "mayProvide";
    String INTENT_TYPE = "intentType";
    String IMPLEMENTATION_TYPE = "implementationType";
    String BINDING_TYPE = "bindingType";
    QName IMPLEMENTATION_TYPE_QNAME = new QName(SCA11_NS, IMPLEMENTATION_TYPE);
    QName BINDING_TYPE_QNAME = new QName(SCA11_NS, BINDING_TYPE);
    String BINDING = "binding";
    String IMPLEMENTATION = "implementation";

    QName POLICY_INTENT_QNAME = new QName(SCA11_NS, INTENT);
    QName POLICY_SET_QNAME = new QName(SCA11_NS, POLICY_SET);
    QName POLICY_INTENT_MAP_QNAME = new QName(SCA11_NS, INTENT_MAP);
    QName SCA_DEFINITIONS_QNAME = new QName(SCA11_NS, SCA_DEFINITIONS);
    QName DESCRIPTION_QNAME = new QName(SCA11_NS, DESCRIPTION);
    QName POLICY_INTENT_MAP_QUALIFIER_QNAME = new QName(SCA11_NS, INTENT_MAP_QUALIFIER);
    QName POLICY_SET_REFERENCE_QNAME = new QName(SCA11_NS, POLICY_SET_REFERENCE);
    QName INTENT_QUALIFIER_QNAME = new QName(SCA11_NS, INTENT_QUALIFIER);
    QName EXTERNAL_ATTACHMENT_QNAME = new QName(SCA11_NS, EXTERNAL_ATTACHMENT);
}
