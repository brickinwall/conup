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
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.definitions.Definitions;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test writing SCA XML assemblies.
 * 
 * @version $Rev: 1052250 $ $Date: 2010-12-23 12:58:21 +0000 (Thu, 23 Dec 2010) $
 */
public class WriteAllTestCase {
    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;
    private static ExtensibleStAXArtifactProcessor staxProcessor;
    private static ModelResolver resolver; 
    private static URLArtifactProcessor<Definitions> policyDefinitionsProcessor;
    private static ProcessorContext context;

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory);
        resolver = new DefaultModelResolver();
        
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        SCABindingFactory scaBindingFactory = new TestSCABindingFactoryImpl();
        modelFactories.addFactory(scaBindingFactory);

        URLArtifactProcessorExtensionPoint documentProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        policyDefinitionsProcessor = documentProcessors.getProcessor(Definitions.class);
    }

    @Test
    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("TestAllCalculator.composite");
        Composite composite = staxProcessor.read(is, Composite.class, context);
        
        verifyComposite(composite);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos, context);
        bos.close();
        System.out.println("Writtent ouput is:\n" + bos);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        composite = staxProcessor.read(bis, Composite.class, context);
        
        verifyComposite(composite);
        
    }

    @Test
    public void testReadWireWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("TestAllCalculator.composite");
        Composite composite = staxProcessor.read(is, Composite.class, context);
        
        URL url = getClass().getResource("test_definitions.xml");
        URI uri = URI.create("test_definitions.xml");
        Definitions scaDefns = (Definitions)policyDefinitionsProcessor.read(null, uri, url, context);
        assertNotNull(scaDefns);
        policyDefinitionsProcessor.resolve(scaDefns, resolver, context);
        
        staxProcessor.resolve(composite, resolver, context);
        // compositeBuilder.build(composite, null, monitor);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos, context);
    }
    
    @Test
    public void testReadWriteComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorImpl.componentType");
        ComponentType componentType = staxProcessor.read(is, ComponentType.class, context);
        staxProcessor.resolve(componentType, resolver, context);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(componentType, bos, context);
    }

   
    private void verifyComposite(Composite composite) {
        assertEquals(composite.getProperties().get(0).getName(),"prop1");
        assertEquals(composite.getProperties().get(0).isMany(), true);
        assertEquals(composite.getProperties().get(1).getName(),"prop2");
        assertEquals(composite.getProperties().get(1).isMustSupply(), true);
        assertEquals(composite.getProperties().get(0).getXSDType(), new QName("http://foo", "MyComplexType"));
        assertEquals(composite.getProperties().get(1).getXSDElement(), new QName("http://foo", "MyComplexPropertyValue1"));
    }

}
