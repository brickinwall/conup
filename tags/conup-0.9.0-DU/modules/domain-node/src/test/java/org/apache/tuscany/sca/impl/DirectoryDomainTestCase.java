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
package org.apache.tuscany.sca.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.apache.tuscany.sca.runtime.ContributionDescription;
import org.junit.Test;
import org.oasisopen.sca.NoSuchDomainException;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.annotation.Remotable;

public class DirectoryDomainTestCase {

    @Test
    public void testDefaultDomain() throws NoSuchServiceException, NoSuchDomainException, ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/test-domains/default"));

        Assert.assertEquals("default", node.getDomainName());
        Assert.assertEquals(1, node.getInstalledContributionURIs().size());
        Assert.assertEquals("sample-helloworld", node.getInstalledContributionURIs().get(0));
        Assert.assertEquals("helloworld.composite", node.getStartedCompositeURIs().get("sample-helloworld").get(0));
    }
    
    @Test
    public void testMyDomain() throws NoSuchServiceException, NoSuchDomainException, ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/test-domains/MyDomain"));

        Assert.assertEquals("foo", node.getDomainName());
        Assert.assertEquals(1, node.getInstalledContributionURIs().size());
        Assert.assertEquals("helloworld-contribution", node.getInstalledContributionURIs().get(0));

        // validate additional deployable composite
        Map<String, List<String>> scs = node.getStartedCompositeURIs();
        Assert.assertEquals(2, scs.get("helloworld-contribution").size());
        Assert.assertTrue(scs.get("helloworld-contribution").contains("helloworld.composite"));
        Assert.assertTrue(scs.get("helloworld-contribution").contains("helloworld2.composite"));
        
        // validate metadata side file
        ContributionDescription ic = node.getInstalledContribution("helloworld-contribution");
        Assert.assertEquals(1, ic.getJavaExports().size());
        Assert.assertEquals("sample", ic.getJavaExports().get(0));
    }

    @Test
    public void testNodeXMLFile() throws ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/helloworldNode.xml"));
        Assert.assertEquals("helloworld", node.getDomainName());
        List<String> cs = node.getInstalledContributionURIs();
        Assert.assertEquals(1, cs.size());
        Assert.assertEquals("sample-helloworld", cs.get(0));
        Map<String, List<String>> startedComposites = node.getStartedCompositeURIs();
        Assert.assertEquals(1, startedComposites.size());
        Assert.assertEquals("helloworld.composite", startedComposites.get("sample-helloworld").get(0));
    }

    @Test
    public void testNodeXMLDomain() throws ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/test-domains/NodeXMLDomain"));
        Assert.assertEquals("helloworld", node.getDomainName());
        List<String> cs = node.getInstalledContributionURIs();
        Assert.assertEquals(1, cs.size());
        Assert.assertEquals("sample-helloworld", cs.get(0));
        Map<String, List<String>> startedComposites = node.getStartedCompositeURIs();
        Assert.assertEquals(1, startedComposites.size());
        Assert.assertEquals("helloworld.composite", startedComposites.get("sample-helloworld").get(0));
    }

    @Test
    public void testExploded() throws ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/test-domains/exploded"));
        Assert.assertEquals("exploded", node.getDomainName());
        List<String> cs = node.getInstalledContributionURIs();
        Assert.assertEquals(1, cs.size());
        Assert.assertEquals("sample-helloworld", cs.get(0));
        Map<String, List<String>> startedComposites = node.getStartedCompositeURIs();
        Assert.assertEquals(1, startedComposites.size());
        Assert.assertEquals("helloworld.composite", startedComposites.get("sample-helloworld").get(0));
    }

    @Test
    public void testDependencies() throws ContributionReadException, ActivationException, ValidationException, XMLStreamException, IOException, NoSuchServiceException {
        Node node = TuscanyRuntime.newInstance().createNode(new File("src/test/resources/test-domains/dependencies"));
        Assert.assertEquals("dependencies", node.getDomainName());
        List<String> cs = node.getInstalledContributionURIs();
        Assert.assertEquals(4, cs.size());
        Assert.assertEquals("Hello 1 Petra", node.getService(TestIface.class, "Helloworld1Component").sayHello("Petra"));
        Assert.assertEquals("Hello 2 Amelia", node.getService(TestIface.class, "Helloworld2Component").sayHello("Amelia"));
    }

    @Remotable
    interface TestIface {
        String sayHello(String name);
    }
}
