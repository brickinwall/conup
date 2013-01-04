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

package org.apace.tuscany.sca.binding.sca.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test reading/write WSDL interfaces.
 *
 * @version $Rev: 889531 $ $Date: 2009-12-11 08:26:48 +0000 (Fri, 11 Dec 2009) $
 */
public class WriteTestCase {

    private static StAXArtifactProcessor<Object> staxProcessor;
    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;
    private static ProcessorContext context;
    
    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory);
    }

    @Test
    @Ignore // broken in 2.0 bring up
    public void testReadWriteComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("/CalculatorServiceImpl.componentType");
        ComponentType componentType = (ComponentType)staxProcessor.read(inputFactory.createXMLStreamReader(is), context);
        assertNotNull(componentType);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(componentType, outputFactory.createXMLStreamWriter(bos), context);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><componentType xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" ><service name=\"CalculatorService\"><binding.sca /><interface.java interface=\"calculator.CalculatorService\" /></service><reference name=\"addService\"><binding.sca /><interface.java interface=\"calculator.AddService\" /></reference></componentType>",
        		     bos.toString());
        }

    @Test
    @Ignore // broken in 2.0 bring up
    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("/Calculator.composite");
        Composite composite = (Composite)staxProcessor.read(inputFactory.createXMLStreamReader(is), context);
        assertNotNull(composite);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" targetNamespace=\"http://calc\" name=\"Calculator\"><service name=\"CalculatorService\" promote=\"CalculatorServiceComponent\"><binding.sca /><interface.java interface=\"calculator.CalculatorService\" /></service><component name=\"CalculatorServiceComponent\"><implementation.java class=\"calculator.CalculatorServiceImpl\" /><reference name=\"addService\" target=\"AddServiceComponent\"><binding.sca /></reference><reference name=\"subtractService\" target=\"SubtractServiceComponent\" /><reference name=\"multiplyService\" target=\"MultiplyServiceComponent\" /><reference name=\"divideService\" target=\"DivideServiceComponent\" /></component><component name=\"AddServiceComponent\"><implementation.java class=\"calculator.AddServiceImpl\" /><service name=\"AddService\"><binding.sca /><interface.java interface=\"calculator.AddService\" /></service></component><component name=\"SubtractServiceComponent\"><implementation.java class=\"calculator.SubtractServiceImpl\" /></component><component name=\"MultiplyServiceComponent\"><implementation.java class=\"calculator.MultiplyServiceImpl\" /></component><component name=\"DivideServiceComponent\"><implementation.java class=\"calculator.DivideServiceImpl\" /></component></composite>",
                     bos.toString());
    }

}
