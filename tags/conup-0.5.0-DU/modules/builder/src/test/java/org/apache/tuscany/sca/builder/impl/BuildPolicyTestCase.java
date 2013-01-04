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

package org.apache.tuscany.sca.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.assembly.builder.BuilderExtensionPoint;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.contribution.processor.DefaultURLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test reading SCA XML assembly documents.
 *
 * @version $Rev: 908639 $ $Date: 2010-02-10 19:21:20 +0000 (Wed, 10 Feb 2010) $
 */
public class BuildPolicyTestCase {
    private static URLArtifactProcessor<Object> documentProcessor;
    private static URLArtifactProcessor<Definitions> policyDefinitionsProcessor;
    private static ModelResolver resolver;
    private static CompositeBuilder compositeBuilder;
    private static Composite composite;
    private static Monitor monitor;
    private static ProcessorContext context;
    
    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        context = new ProcessorContext(extensionPoints);

        compositeBuilder =
            extensionPoints.getExtensionPoint(BuilderExtensionPoint.class)
                .getCompositeBuilder("org.apache.tuscany.sca.assembly.builder.CompositeBuilder");

        List<Definitions> policyDefinitions = new ArrayList<Definitions>();
        resolver = new DefaultModelResolver();

        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = utilities.getUtility(MonitorFactory.class);
        monitor = monitorFactory.createMonitor();

        URLArtifactProcessorExtensionPoint documentProcessors =
            new DefaultURLArtifactProcessorExtensionPoint(extensionPoints);
        documentProcessor = new ExtensibleURLArtifactProcessor(documentProcessors);
        policyDefinitionsProcessor = documentProcessors.getProcessor(Definitions.class);

        StAXArtifactProcessorExtensionPoint staxProcessors =
            extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessors.addArtifactProcessor(new TestPolicyProcessor());

        URL url = BuildPolicyTestCase.class.getResource("Calculator.composite");
        URI uri = URI.create("TestAllCalculator.composite");
        composite = (Composite)documentProcessor.read(null, uri, url, context);
        assertNotNull(composite);

        url = BuildPolicyTestCase.class.getResource("test_definitions.xml");
        uri = URI.create("test_definitions.xml");
        Definitions definitions = (Definitions)policyDefinitionsProcessor.read(null, uri, url, context);
        assertNotNull(definitions);
        policyDefinitions.add(definitions);

        documentProcessor.resolve(definitions, resolver, context);
        documentProcessor.resolve(composite, resolver, context);

        BuilderContext builderContext = new BuilderContext(definitions, null, monitor);
        compositeBuilder.build(composite, builderContext);
    }

    @Test
    @Ignore("There are so many errors with this composite the builder doesn't have enough to go on")
    public void testPolicyIntentInheritance() throws Exception {
        String namespaceUri = "http://test";

        PolicySubject policiedComposite = composite;
        assertEquals(policiedComposite.getRequiredIntents().size(), 1);
        assertEquals(policiedComposite.getRequiredIntents().get(0).getName(),
                     new QName(namespaceUri, "tuscanyIntent_1"));

        Component component = composite.getComponents().get(0);
        Endpoint ep = component.getServices().get(0).getEndpoints().get(0);
        EndpointReference epr = component.getReferences().get(0).getEndpointReferences().get(0);

        System.out.println(ep.getRequiredIntents());
        System.out.println(epr.getRequiredIntents());
    }

}
