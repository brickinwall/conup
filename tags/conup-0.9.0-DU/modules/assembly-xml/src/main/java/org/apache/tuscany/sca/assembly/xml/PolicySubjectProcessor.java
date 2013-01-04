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

package org.apache.tuscany.sca.assembly.xml;

import static org.apache.tuscany.sca.assembly.xml.Constants.POLICY_SETS;
import static org.apache.tuscany.sca.assembly.xml.Constants.REQUIRES;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * A Policy Attach Point processor.
 *
 * @version $Rev: 937291 $ $Date: 2010-04-23 14:41:24 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class PolicySubjectProcessor extends BaseStAXArtifactProcessor {
    
    private PolicyFactory policyFactory;
    
    public PolicySubjectProcessor(PolicyFactory policyFactory) {
        this.policyFactory = policyFactory;
    }
    
    public PolicySubjectProcessor(ExtensionPointRegistry registry) {
        FactoryExtensionPoint factories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        this.policyFactory = factories.getFactory(PolicyFactory.class);
    }

    /**
     * Read policy intents associated with an operation.
     * @param subject
     * @param operation
     * @param reader
     */
    private void readIntents(Object subject, Operation operation, XMLStreamReader reader) {
        if (!(subject instanceof PolicySubject))
            return;
        PolicySubject intentAttachPoint = (PolicySubject)subject;
        String value = reader.getAttributeValue(null, REQUIRES);
        if (value != null) {
            List<Intent> requiredIntents = intentAttachPoint.getRequiredIntents();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                Intent intent = policyFactory.createIntent();
                intent.setName(qname);
                if (operation != null) {
                    //FIXME Don't we need to handle intent specification
                    // on an operation basis?
                    //intent.getOperations().add(operation);
                }
                requiredIntents.add(intent);
            }
        }
    }

    /**
     * Reads policy intents and policy sets associated with an operation.
     * @param subject
     * @param operation
     * @param reader
     */
    public void readPolicies(Object subject, Operation operation, XMLStreamReader reader) {
        readIntents(subject, operation, reader);
        readPolicySets(subject, operation, reader);
    }

    /**
     * Reads policy intents and policy sets.
     * @param subject
     * @param reader
     */
    public void readPolicies(Object subject, XMLStreamReader reader) {
        readPolicies(subject, null, reader);
    }

    /**
     * Reads policy sets associated with an operation.
     * @param subject
     * @param operation
     * @param reader
     */
    private void readPolicySets(Object subject, Operation operation, XMLStreamReader reader) {
        if (!(subject instanceof PolicySubject)) {
            return;
        }
        PolicySubject policySubject = (PolicySubject)subject;
        String value = reader.getAttributeValue(null, POLICY_SETS);
        if (value != null) {
            List<PolicySet> policySets = policySubject.getPolicySets();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                PolicySet policySet = policyFactory.createPolicySet();
                policySet.setName(qname);
                if (operation != null) {
                    //FIXME Don't we need to handle policySet specification
                    // on an operation basis?
                    //policySet.getOperations().add(operation);
                }
                policySets.add(policySet);
            }
        }
    }
    
    /**
     * Write policies
     * @param subject
     * @return
     */
    XAttr writePolicies(Object subject) throws XMLStreamException {
        return writePolicies(subject, (Operation)null);
    }

    /**
     * Write policies
     * @param subject
     * @return
     */
    public void writePolicyAttributes(Object subject, XMLStreamWriter writer) throws XMLStreamException {
        writePolicyAttributes(subject, (Operation)null, writer);
    }

    /**
     * Write policies associated with an operation
     * @param subject
     * @param operation
     * @return
     */
    XAttr writePolicies(Object subject, Operation operation) {
        List<XAttr> attrs =new ArrayList<XAttr>();
        attrs.add(writeIntents(subject, operation));
        attrs.add(writePolicySets(subject, operation));
        return new XAttr(null, attrs);
    }

    /**
     * Write policies
     * @param subject
     * @return
     */
    public void writePolicyAttributes(Object subject, Operation operation, XMLStreamWriter writer) throws XMLStreamException {
        XAttr attr = writePolicies(subject, operation);
        attr.write(writer);
    }

    /**
     * Write policy intents associated with an operation.
     * @param subject
     * @param operation
     */
    private XAttr writeIntents(Object subject, Operation operation) {
        if (!(subject instanceof PolicySubject)) {
            return null;
        }
        PolicySubject intentAttachPoint = (PolicySubject)subject;
        List<QName> qnames = new ArrayList<QName>();
        for (Intent intent: intentAttachPoint.getRequiredIntents()) {
            qnames.add(intent.getName());
        }
        return new XAttr(Constants.REQUIRES, qnames);
    }

    /**
     * Write policy sets associated with an operation.
     * @param subject
     * @param operation
     */
    private XAttr writePolicySets(Object subject, Operation operation) {
        if (!(subject instanceof PolicySubject)) {
            return null;
        }
        PolicySubject policySetAttachPoint = (PolicySubject)subject;
        List<QName> qnames = new ArrayList<QName>();
        for (PolicySet policySet: policySetAttachPoint.getPolicySets()) {
            qnames.add(policySet.getName());
        }
        return new XAttr(Constants.POLICY_SETS, qnames);
    }
    
    public void resolvePolicies(Object subject, ModelResolver resolver, ProcessorContext context) {
        if ( subject instanceof PolicySubject ) {
            PolicySubject policySetAttachPoint = (PolicySubject)subject;
            
            List<Intent> requiredIntents = new ArrayList<Intent>();
            Intent resolvedIntent = null;
            
            if ( policySetAttachPoint.getRequiredIntents() != null && policySetAttachPoint.getRequiredIntents().size() > 0 ) {
                for ( Intent intent : policySetAttachPoint.getRequiredIntents() ) {
                    resolvedIntent = resolver.resolveModel(Intent.class, intent, context);
                    requiredIntents.add(resolvedIntent);
                }
                policySetAttachPoint.getRequiredIntents().clear();
                policySetAttachPoint.getRequiredIntents().addAll(requiredIntents);
            }
            
            if ( policySetAttachPoint.getPolicySets() != null && policySetAttachPoint.getPolicySets().size() > 0 ) {
                List<PolicySet> resolvedPolicySets = new ArrayList<PolicySet>();
                PolicySet resolvedPolicySet = null;
                for ( PolicySet policySet : policySetAttachPoint.getPolicySets() ) {
                    resolvedPolicySet = resolver.resolveModel(PolicySet.class, policySet, context);
                    resolvedPolicySets.add(resolvedPolicySet);
                }
                policySetAttachPoint.getPolicySets().clear();
                policySetAttachPoint.getPolicySets().addAll(resolvedPolicySets);
            }
        }
    }
}
