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
package org.apache.tuscany.sca.policy.authorization;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class AuthorizationPolicyProcessor implements StAXArtifactProcessor<AuthorizationPolicy> {
    private static final String ROLES = "roles";

    public QName getArtifactType() {
        return AuthorizationPolicy.NAME;
    }

    public AuthorizationPolicyProcessor(FactoryExtensionPoint modelFactories) {
    }
    
    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), "policy-security-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }        
    }    

    public AuthorizationPolicy read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        AuthorizationPolicy policy = new AuthorizationPolicy();
        int event = reader.getEventType();
        QName start = reader.getName();
        while (true) {
            switch (event) {
                case START_ELEMENT:
                    String ac = reader.getName().getLocalPart();
                    if ("allow".equals(ac)) {
                        policy.setAccessControl(AuthorizationPolicy.AcessControl.allow);
                        String roleNames = reader.getAttributeValue(null, ROLES);
                        if (roleNames == null) {
                            error(context.getMonitor(), "RequiredAttributeRolesMissing", reader);
                            //throw new IllegalArgumentException("Required attribute 'roles' is missing.");
                        } else {
                            StringTokenizer st = new StringTokenizer(roleNames);
                            while (st.hasMoreTokens()) {
                                policy.getRoleNames().add(st.nextToken());
                            }
                        }
                    } else if ("permitAll".equals(ac)) {
                        policy.setAccessControl(AuthorizationPolicy.AcessControl.permitAll);
                    } else if ("denyAll".endsWith(ac)) {
                        policy.setAccessControl(AuthorizationPolicy.AcessControl.denyAll);
                    }
                    break;
                case END_ELEMENT:
                    if (start.equals(reader.getName())) {
                        if (reader.hasNext()) {
                            reader.next();
                        }
                        return policy;
                    }

            }
            if (reader.hasNext()) {
                event = reader.next();
            } else {
                return policy;
            }
        }
    }

    public void write(AuthorizationPolicy policy, XMLStreamWriter writer, ProcessorContext context) throws ContributionWriteException,
        XMLStreamException {
        writer.writeStartElement(AuthorizationPolicy.NAME.getLocalPart());

        writer.writeStartElement(policy.getAccessControl().name());

        if (policy.getAccessControl() == AuthorizationPolicy.AcessControl.allow) {
            StringBuffer sb = new StringBuffer();
            for (String role : policy.getRoleNames()) {
                sb.append(role);
            }

            if (sb.length() > 0) {
                writer.writeAttribute(ROLES, sb.toString());
            }
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    public Class<AuthorizationPolicy> getModelType() {
        return AuthorizationPolicy.class;
    }

    public void resolve(AuthorizationPolicy policy, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException {
        
        if ((policy.getAccessControl() == AuthorizationPolicy.AcessControl.allow) &&
            (policy.getRoleNames().isEmpty())){
            // role names are required so leave policy unresolved
            return;
        }

        //right now nothing to resolve
        policy.setUnresolved(false);
    }

}
