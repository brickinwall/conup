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

package org.apache.tuscany.sca.binding.ws.xml;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test reading/write WSDL interfaces.
 * 
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class WriteTestCase {

    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor;
    private static ProcessorContext context;

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(extensionPoints);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory);
    }

    @Test
    public void testReadWriteComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorImpl.componentType");
        ComponentType componentType = (ComponentType)staxProcessor.read(inputFactory.createXMLStreamReader(is), context);
        assertNotNull(componentType);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(componentType, outputFactory.createXMLStreamWriter(bos), context);
    }

    @Test
    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        Composite composite = (Composite)staxProcessor.read(inputFactory.createXMLStreamReader(is), context);
        assertNotNull(composite);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos), context);
        System.out.println(bos.toString());
    }

}
