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

package org.apache.tuscany.sca.contribution.resource.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resource.ResourceImport;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test NamespaceImportProcessorTestCase
 *
 * @version $Rev: 993091 $ $Date: 2010-09-06 17:49:51 +0100 (Mon, 06 Sep 2010) $
 */
public class ResourceImportProcessorTestCase {

    private static final String VALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>"
            + "<import.resource xmlns=\"http://tuscany.apache.org/xmlns/sca/1.1\" xmlns:ns=\"http://ns\" uri=\"helloworld/HelloWorldService.componentType\" location=\"sca://contributions/001\"/>";

    private static final String INVALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>"
            + "<import.resource xmlns=\"http://tuscany.apache.org/xmlns/sca/1.1\" xmlns:ns=\"http://ns\" location=\"sca://contributions/001\"/>";

    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor;
    private static Monitor monitor;
    private static ProcessorContext context;

    @BeforeClass
    public static void setUp() throws Exception {
    	ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);
        monitor = context.getMonitor();
        inputFactory = XMLInputFactory.newInstance();

        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null);
    }

    /**
     * Test loading a valid import element from a contribution metadata stream
     * @throws Exception
     */
    @Test
    public void testLoad() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(VALID_XML));
        ResourceImport namespaceImport = (ResourceImport)staxProcessor.read(reader, context);
        assertEquals("helloworld/HelloWorldService.componentType", namespaceImport.getURI());
        assertEquals("sca://contributions/001", namespaceImport.getLocation());
    }

    /**
     * Test loading a INVALID import element from a contribution metadata stream
     * @throws Exception
     */
    @Test
    public void testLoadInvalid() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(INVALID_XML));
        /*try {
            staxProcessor.read(reader);
            fail("readerException should have been thrown");
        } catch (ContributionReadException e) {
            assertTrue(true);
        }*/
        staxProcessor.read(reader, context);
        Problem problem = monitor.getLastProblem();
        assertNotNull(problem);
        assertEquals("AttributeURIMissing", problem.getMessageId());
    }
}
