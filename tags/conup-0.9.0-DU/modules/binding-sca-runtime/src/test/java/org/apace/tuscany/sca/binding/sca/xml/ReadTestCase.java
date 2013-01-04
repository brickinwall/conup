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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test reading WSDL interfaces.
 * 
 * @version $Rev: 1145115 $ $Date: 2011-07-11 11:41:30 +0100 (Mon, 11 Jul 2011) $
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
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null);
    }

    @Test
    public void testReadComponentType() throws Exception {   	
        InputStream is = getClass().getResourceAsStream("/CalculatorServiceImpl.componentType");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        ComponentType componentType = (ComponentType)staxProcessor.read(reader, context);
        assertNotNull(componentType);
        
        SCABinding referenceSCABinding = (SCABinding) componentType.getReferences().get(0).getBindings().get(0);
        assertNotNull(referenceSCABinding);
        
        SCABinding serviceSCABinding   = (SCABinding) componentType.getServices().get(0).getBindings().get(0);
        assertNotNull(serviceSCABinding); 
        
        //new PrintUtil(System.out).print(componentType);
    }

    @Test
    public void testReadComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("/Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite)staxProcessor.read(reader, context);
        assertNotNull(composite);

        SCABinding referenceSCABinding = (SCABinding) composite.getComponents().get(0).getReferences().get(0).getBindings().get(0);
        SCABinding serviceSCABinding   = (SCABinding) composite.getComponents().get(1).getServices().get(0).getBindings().get(0);
        
        Assert.assertNotNull(referenceSCABinding);
        Assert.assertNotNull(serviceSCABinding);  
        
        assertNotNull(serviceSCABinding.getRequestWireFormat());        
    }

}
