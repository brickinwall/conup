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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test writing SCA XML assemblies.
 * 
 * @version $Rev: 1052251 $ $Date: 2010-12-23 12:59:11 +0000 (Thu, 23 Dec 2010) $
 */
public class WriteNamespacesTestCase {
    private static StAXArtifactProcessor<Composite> compositeProcessor;
    private static XMLOutputFactory outputFactory;
    private static XMLInputFactory inputFactory;
    private static ProcessorContext context;
    
    @BeforeClass
    public static void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        outputFactory = modelFactories.getFactory(XMLOutputFactory.class);
        //outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        inputFactory = modelFactories.getFactory(XMLInputFactory.class);
        
        StAXArtifactProcessorExtensionPoint artifactProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        compositeProcessor = artifactProcessors.getProcessor(Composite.class);
    }

    @Test
    public void testReadWriteComposite() throws Exception {
        
        // Read
        InputStream is = getClass().getResourceAsStream("NestedCalculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeProcessor.read(reader, context);
        Component component = composite.getComponents().get(0);
        Composite implementation = (Composite)component.getImplementation();
        QName qname = implementation.getName();

        // Write
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(bos);
        compositeProcessor.write(composite, writer, context);
        writer.close();
        System.out.println("Writtent ouput is:\n" + bos);
        
        // Read again
        is = new ByteArrayInputStream(bos.toByteArray());
        reader = inputFactory.createXMLStreamReader(is);
        composite = compositeProcessor.read(reader, context);
        
        // Compare
        component = composite.getComponents().get(0);
        implementation = (Composite)component.getImplementation();
        
        assertEquals(qname, implementation.getName());
    }

}
