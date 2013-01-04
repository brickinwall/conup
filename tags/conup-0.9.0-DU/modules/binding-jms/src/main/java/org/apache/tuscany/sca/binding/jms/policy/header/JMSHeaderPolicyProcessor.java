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
package org.apache.tuscany.sca.binding.jms.policy.header;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.assembly.xml.Messages;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class JMSHeaderPolicyProcessor extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<JMSHeaderPolicy> {
    
    
    
    public QName getArtifactType() {
        return JMSHeaderPolicy.JMS_HEADER_POLICY_QNAME;
    }
    
    public JMSHeaderPolicyProcessor(ExtensionPointRegistry modelFactories) {
    }
    
    /**
     * Marshals warnings into the monitor
     * 
     * @param message
     * @param model
     * @param messageParameters
     */
    protected void warning(Monitor monitor, String message, Object model, String... messageParameters) {
        if (monitor != null){
            Problem problem = monitor.createProblem(this.getClass().getName(), Messages.RESOURCE_BUNDLE, Severity.WARNING, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }
    }
    
    /**
     * Marshals errors into the monitor
     * 
     * @param problems
     * @param message
     * @param model
     */
    protected void error(Monitor monitor, String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), Messages.RESOURCE_BUNDLE, Severity.ERROR, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }
    }    

    
    public JMSHeaderPolicy read(XMLStreamReader reader, ProcessorContext context) throws ContributionReadException, XMLStreamException {
        JMSHeaderPolicy policy = new JMSHeaderPolicy();
        int event = reader.getEventType();
        QName name = null;
        Monitor monitor = context.getMonitor();
        while (reader.hasNext()) {
            event = reader.getEventType();
            switch (event) {
                case START_ELEMENT : {
                    name = reader.getName();
                    if ( name.equals(getArtifactType()) ) {
                        
                        policy.setJmsType(getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_TYPE));
                        policy.setJmsCorrelationId(getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_CORRELATION_ID));
                        
                        String deliveryMode = getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_DELIVERY_MODE);
                        if (deliveryMode != null){
                            if (deliveryMode.equals("PERSISTENT")) {
                                policy.setDeliveryModePersistent(true);
                            } else if (deliveryMode.equals("NON_PERSISTENT")){
                                policy.setDeliveryModePersistent(false);
                            } else {
                                error(monitor, "InvalidDeliveryMode", policy, deliveryMode);
                            }  
                        }
                        
                        String timeToLive = getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_TIME_TO_LIVE);
                        
                        if ( timeToLive != null){
                            try {
                                policy.setTimeToLive(Long.valueOf(timeToLive));
                            } catch (NumberFormatException ex){
                                error(monitor, "InvalidTimeToLive", policy, timeToLive);
                            }
                        }
                        
                        String priority = getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_PRIORITY);
                        
                        if ( priority != null){
                            try {
                                policy.setJmsPriority(Integer.valueOf(priority));
                            } catch (NumberFormatException ex){
                                error(monitor, "InvalidPriority", policy, priority);
                            }                                
                        }
                    } else if (name.getLocalPart().equals(JMSHeaderPolicy.JMS_HEADER_JMS_PROPERTY)) {
                        String propertyName = getString(reader, JMSHeaderPolicy.JMS_HEADER_JMS_PROPERTY_NAME);
                        String propertyValue = reader.getElementText();
                        policy.getProperties().put(propertyName, propertyValue);
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

    public void write(JMSHeaderPolicy policy, XMLStreamWriter writer, ProcessorContext context) 
        throws ContributionWriteException, XMLStreamException {
        String prefix = "tuscany";
        writer.writeStartElement(prefix, 
                                 getArtifactType().getLocalPart(),
                                 getArtifactType().getNamespaceURI());
        writer.writeNamespace("tuscany", Constants.SCA11_TUSCANY_NS);
        
        if (policy.getJmsType() != null){
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_TYPE, policy.getJmsType());
        }
        
        if (policy.getJmsCorrelationId() != null){
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_CORRELATION_ID, policy.getJmsCorrelationId());
        } 
        
        if (policy.getDeliveryModePersistent() == true){
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_DELIVERY_MODE, "PERSISTENT");
        } else {
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_DELIVERY_MODE, "NON_PERSISTENT");
        }
        
        if (policy.getTimeToLive()!= null){
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_TIME_TO_LIVE, policy.getTimeToLive().toString());
        }    
        
        if (policy.getJmsPriority()!= null){
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_PRIORITY, policy.getJmsPriority().toString());
        }          
        
        for (String propertyName : policy.getProperties().keySet()){
            writer.writeStartElement(prefix, 
                                     JMSHeaderPolicy.JMS_HEADER_JMS_PROPERTY,
                                     getArtifactType().getNamespaceURI());
            writer.writeAttribute(JMSHeaderPolicy.JMS_HEADER_JMS_PROPERTY_NAME, propertyName);
            writer.writeCharacters(policy.getProperties().get(propertyName));
            writer.writeEndElement();
        }    
        
        writer.writeEndElement();
    }

    public Class<JMSHeaderPolicy> getModelType() {
        return JMSHeaderPolicy.class;
    }

    public void resolve(JMSHeaderPolicy arg0, ModelResolver arg1, ProcessorContext context) throws ContributionResolveException {

    }
    
}
