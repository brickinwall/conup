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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test reading SCA XML assemblies.
 * 
 * @version $Rev: 1041915 $ $Date: 2010-12-03 17:10:49 +0000 (Fri, 03 Dec 2010) $
 */
public class ReadWriteLocalCompositeTestCase {

    private XMLInputFactory inputFactory;
    private ExtensibleStAXArtifactProcessor staxProcessor;
    private ProcessorContext context;
    
    private static final String LOCAL_COMPOSITE_XML = "<?xml version='1.0' encoding='UTF-8'?>"+
    "<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" targetNamespace=\"http://localcalc\" name=\"LocalCalculator\" local=\"true\">"+
    "</composite>";
    
    @Before
    public void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        inputFactory = XMLInputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);

        StAXAttributeProcessorExtensionPoint staxAttributeProcessors = extensionPoints.getExtensionPoint(StAXAttributeProcessorExtensionPoint.class);
        staxAttributeProcessors.addArtifactProcessor(new TestAttributeProcessor());
        
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
    }

    @After
    public void tearDown() throws Exception {
    	
    }

    @Test
    public void testReadComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("local.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite) staxProcessor.read(reader, context);
        assertNotNull(composite);
        assertTrue(composite.isLocal());
        is.close();
    }
    
    @Test
    public void testWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("local.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite) staxProcessor.read(reader, context);
        assertNotNull(composite);
        assertTrue(composite.isLocal());
        is.close();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos, context);
        System.out.println(bos.toString());
        
      //  assertEquals(LOCAL_COMPOSITE_XML, bos.toString());
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    	composite = staxProcessor.read(bis, Composite.class, context);
    	assertNotNull(composite);
    	assertTrue(composite.isLocal());
    }
}
