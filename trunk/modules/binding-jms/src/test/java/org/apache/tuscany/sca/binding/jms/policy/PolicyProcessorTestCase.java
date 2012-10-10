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

package org.apache.tuscany.sca.binding.jms.policy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.apache.tuscany.sca.binding.jms.policy.authentication.token.JMSTokenAuthenticationPolicy;
import org.apache.tuscany.sca.binding.jms.policy.authentication.token.JMSTokenAuthenticationPolicyProcessor;
import org.apache.tuscany.sca.binding.jms.policy.header.JMSHeaderPolicy;
import org.apache.tuscany.sca.binding.jms.policy.header.JMSHeaderPolicyProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
@Ignore("TODO: 2.x migration")
public class PolicyProcessorTestCase {
    private final static List<String> SEQ =
        Arrays.asList("<tuscany:jmsHeader xmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.1\" JMSType=\"ABC\" JMSDeliveryMode=\"PERSISTENT\" JMSTimeToLive=\"123\" JMSPriority=\"4\"><tuscany:property name=\"aProperty\">property value</tuscany:property></tuscany:jmsHeader>",
                      "<tuscany:jmsTokenAuthentication xmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.1\" tuscany:tokenName=\"{http://tuscany.apache.org/foo}myname\" />");
    
    @Test
    public void testRead() throws Exception {
        List<String> results = new ArrayList<String>();
        Map<QName, StAXArtifactProcessor> processors = new HashMap<QName, StAXArtifactProcessor>();
        processors.put(JMSHeaderPolicy.JMS_HEADER_POLICY_QNAME, new JMSHeaderPolicyProcessor(null));
        processors.put(JMSTokenAuthenticationPolicy.JMS_TOKEN_AUTHENTICATION_POLICY_QNAME, new JMSTokenAuthenticationPolicyProcessor(null));
        
        InputStream is = getClass().getResourceAsStream("mock_policy_definitions.xml");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        ProcessorContext context =new ProcessorContext();
        while (true) {
            int event = reader.getEventType();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("policySet".equals(reader.getName().getLocalPart())) {
                    reader.nextTag();
                    StAXArtifactProcessor processor = processors.get(reader.getName());
                    Object xxx = processor.read(reader, context);
//                    Policy policy = (Policy)processor.read(reader);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream);
                    processor.write(xxx, writer, context);
//                    processor.write(policy, writer);
                    writer.flush();
                    results.add(outputStream.toString());
                }
            }
            if (reader.hasNext()) {
                reader.next();
            } else {
                break;
            }
        }
        Assert.assertEquals(SEQ, results);
    }
}
