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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test reading SCA XML assemblies.
 * 
 * @version $Rev: 827835 $ $Date: 2009-10-21 00:30:48 +0100 (Wed, 21 Oct 2009) $
 */
public class ReadTestCase {

    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor;
    private static ProcessorContext context;
    
    @BeforeClass
    public static void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        inputFactory = XMLInputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
    }


    @Test
    public void testReadComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorImpl.componentType");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        assertNotNull(staxProcessor.read(reader, context));
        is.close();
    }

    @Test
    public void testReadComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        assertNotNull(staxProcessor.read(reader, context));
        is.close();

    }

    @Test
    public void testReadCompositeAndWireIt() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        assertNotNull(staxProcessor.read(reader, context));
        is.close();
    }

}
