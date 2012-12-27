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

package org.apache.tuscany.sca.binding.jms;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;

/**
 * Tests for JMS binding XML writes.
 * In general, for each JMS binding XML read test case, there
 * is a write test case. 
 */
public class JMSBindingProcessorWriteTestCase extends TestCase {
    
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private StAXArtifactProcessor<Object> staxProcessor;
    
    public static final String DEFAULT =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" 
        + "<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" targetNamespace=\"http://binding-jms\" name=\"binding-jms\">"
            + " <component name=\"HelloWorldComponent\">"
            + "      <service name=\"HelloWorldService\">"
            + "          <binding.jms>"
            + "            <destination jndiName=\"AAA\">"
            + "                <property name=\"AAAProp\" type=\"string\"/>"
            + "            </destination>"
            + "            <connectionFactory jndiName=\"ABC\"/>"
            + "            <response/>"
            + "            <headers/>"
            + "            <resourceAdapter name=\"GHI\"/>"
            + "            <operationProperties name=\"JKL\">"
            + "            </operationProperties>"
            + "          </binding.jms>"
            + "      </service>"
            + " </component>"
            + "</composite>";

    private ProcessorContext context;

    @Override
    protected void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(extensionPoints);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory);
    }

    /**
     * Test parsing valid composite definition. Valid composite populated with correct values expected.
     * @throws Exception
     */
    public void testLoadValidComposite() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.COMPOSITE));       
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2);
    }

    public void testHeaders1() throws Exception {
        Composite composite = (Composite)staxProcessor.read(inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.HEADERS1)), context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2 );        
    }

    public void testProperties1() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.PROPERTIES1));        
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2 );        
    }

    public void testOpProperties1() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.OP_PROPERTIES1));       
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);
  
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);        
    }

    public void testSubscriptionHeaders() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.SELECTOR));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testDestinationProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.DEST_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testConnectionFactoryProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.CF_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testActivationSpecProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.AS_PROPS));       
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testResponseDestinationProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_DEST_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testResponseConnectionFactoryProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_CF_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testResponseActivationSpecProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_AS_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testOperationPropertiesProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.OP_PROPS_PROPS));       
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testResouceAdapterProperties() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RES_ADPT_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testConfiguredOperations() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.CONFIGURED_OPERATIONS));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testWireFormat() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.WIRE_FORMAT));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }   

    // TUSCANY-3120
    // Checking we don't write out values unless the user has specified them on input
    public void testDefault() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(DEFAULT));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);
        
        String outputString = bos.toString();
        System.out.println(outputString);
        /* replace with slightly different test so any ordering differences in written
         * XML don't fail the test
        assertEquals(bos.toString(),
                     "<?xml version=\'1.0\' encoding=\'UTF-8\'?>" +
                     "<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" targetNamespace=\"http://binding-jms\" name=\"binding-jms\">" +
                       "<component name=\"HelloWorldComponent\">" + 
                           "<service name=\"HelloWorldService\">" +
                             "<binding.jms><operationProperties name=\"JKL\" /> " +
                               "<destination jndiName=\"AAA\" type=\"queue\">" +
                                 "<property name=\"AAAProp\" type=\"string\"></property>" +
                               "</destination> " +
                               "<connectionFactory jndiName=\"ABC\" /> "+
                               "<resourceAdapter name=\"GHI\" /> " +
                             "</binding.jms>" +
                           "</service>" +
                         "</component>" +
                       "</composite>");
        */
        assertEquals(true, outputString.contains("binding.jms"));
        assertEquals(true, outputString.contains("operationProperties"));
        assertEquals(true, outputString.contains("destination"));
        assertEquals(true, outputString.contains("jndiName"));
        assertEquals(true, outputString.contains("type"));
        assertEquals(true, outputString.contains("property"));
        assertEquals(true, outputString.contains("connectionFactory"));
        assertEquals(true, outputString.contains("resourceAdapter"));
        
        assertEquals(false, outputString.contains("headers"));
        assertEquals(false, outputString.contains("response"));
        assertEquals(false, outputString.contains("activationSpec"));
        assertEquals(false, outputString.contains("messageSelection"));
        assertEquals(false, outputString.contains("initialContextFactory"));
        assertEquals(false, outputString.contains("correlationScheme"));
        assertEquals(false, outputString.contains("wireFormat"));
        assertEquals(false, outputString.contains("operationSelector"));
    }  

    public void testOperationPropertiesName() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.OP_PROP_NAME));
        Composite composite = (Composite)staxProcessor.read(reader, context);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2, context);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals("foo", binding2.getOperationPropertiesName().getLocalPart());
    }
}
