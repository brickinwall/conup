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

package org.apache.tuscany.sca.contribution.java.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.contribution.java.JavaExport;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test JavaExportProcessorTestCase
 * 
 * @version $Rev: 889531 $ $Date: 2009-12-11 08:26:48 +0000 (Fri, 11 Dec 2009) $
 */
public class JavaExportProcessorTestCase {

    private static final String VALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" 
            + "<export.java  xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" xmlns:ns=\"http://ns\" package=\"org.apache.tuscany.sca.contribution.java\"/>";

    private static final String INVALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" 
            + "<export.java  xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"/>";

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
     * Test loading a valid export element from a contribution metadata stream
     * @throws Exception
     */
    @Test
    public void testLoad() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(VALID_XML));
        JavaExport javaExport = (JavaExport)staxProcessor.read(reader, context);
        Assert.assertEquals("org.apache.tuscany.sca.contribution.java", javaExport.getPackage());
    }

    /**
     * Test loading an INVALID export element from a contribution metadata stream
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
        assertEquals("AttributePackageMissing", problem.getMessageId());
    }    
}
