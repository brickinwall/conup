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
package org.apache.tuscany.sca.policy.authentication.basic;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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

/**
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class BasicAuthenticationPolicyProcessor implements StAXArtifactProcessor<BasicAuthenticationPolicy> {
    private static final String SCA10_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";

    public QName getArtifactType() {
        return BasicAuthenticationPolicy.BASIC_AUTHENTICATION_POLICY_QNAME;
    }
    
    public BasicAuthenticationPolicyProcessor(FactoryExtensionPoint modelFactories) {
    }

    
    public BasicAuthenticationPolicy read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        int event = reader.getEventType();
        QName name = null;
        
        while (reader.hasNext()) {
            event = reader.getEventType();
            switch (event) {
                case START_ELEMENT : {
                    name = reader.getName();
                    if ( name.equals(getArtifactType()) ) {
                        // no attributes at the moment
                    } else if ( BasicAuthenticationPolicy.BASIC_AUTHENTICATION_USERNAME.equals(name.getLocalPart()) ) {
                        policy.setUserName(reader.getElementText());
                    } else if ( BasicAuthenticationPolicy.BASIC_AUTHENTICATION_PASSWORD.equals(name.getLocalPart()) ) {
                        policy.setPassword(reader.getElementText());
                    }
                    break;
                }
            }
            
            if ( event == END_ELEMENT ) {
                if ( getArtifactType().equals(reader.getName()) ) {
                    break;
                } 
            }
            
            //Read the next element
            if (reader.hasNext()) {
                reader.next();
            }
        }
         
        return policy;
    }

    public void write(BasicAuthenticationPolicy policy, XMLStreamWriter writer, ProcessorContext context) 
        throws ContributionWriteException, XMLStreamException {
        String prefix = "tuscany";
        writer.writeStartElement(prefix, 
                                 getArtifactType().getLocalPart(),
                                 getArtifactType().getNamespaceURI());
        writer.writeNamespace("tuscany", SCA10_TUSCANY_NS);

        if ( policy.getUserName() != null ) {
            writer.writeStartElement(prefix, 
                                     BasicAuthenticationPolicy.BASIC_AUTHENTICATION_USERNAME,
                                     getArtifactType().getNamespaceURI());
            writer.writeCharacters(policy.getUserName());
            writer.writeEndElement();
        }
        
        if ( policy.getPassword() != null ) {
            writer.writeStartElement(prefix, 
                                     BasicAuthenticationPolicy.BASIC_AUTHENTICATION_PASSWORD,
                                     getArtifactType().getNamespaceURI());
            writer.writeCharacters(policy.getPassword());
            writer.writeEndElement();
        }        
        
        writer.writeEndElement();
    }

    public Class<BasicAuthenticationPolicy> getModelType() {
        return BasicAuthenticationPolicy.class;
    }

    public void resolve(BasicAuthenticationPolicy arg0, ModelResolver arg1, ProcessorContext context) throws ContributionResolveException {

    }
    
}
