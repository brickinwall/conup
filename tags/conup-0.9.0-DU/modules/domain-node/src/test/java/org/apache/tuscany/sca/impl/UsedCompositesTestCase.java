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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.junit.Assert;
import org.junit.Test;
import org.oasisopen.sca.NoSuchDomainException;
import org.oasisopen.sca.NoSuchServiceException;

public class UsedCompositesTestCase {

    @Test
    public void includeTest1() throws NoSuchServiceException, NoSuchDomainException, ContributionReadException, ActivationException, ValidationException, XMLStreamException, FileNotFoundException {
        Node node = TuscanyRuntime.newInstance().createNode("localTest");
        String curi = node.installContribution("src/test/resources/sample-helloworld.jar");
        String compositeURI = node.addDeploymentComposite(curi, new FileReader("src/test/resources/include.composite"));

        node.startComposite(curi, compositeURI);
        
        List<String> xs = ((NodeImpl)node).updateUsingComposites(curi, "helloworld.composite");
        Assert.assertEquals(1, xs.size());
        Assert.assertEquals("sample-helloworld/include.composite", xs.get(0));
    }
    @Test
    public void implCompositeTest() throws NoSuchServiceException, NoSuchDomainException, ContributionReadException, ActivationException, ValidationException, XMLStreamException, FileNotFoundException {
        Node node = TuscanyRuntime.newInstance().createNode("localTest");
        String curi = node.installContribution("src/test/resources/sample-helloworld.jar");
        String compositeURI = node.addDeploymentComposite(curi, new FileReader("src/test/resources/compositeImpl.composite"));

        node.startComposite(curi, compositeURI);
        
        List<String> xs = ((NodeImpl)node).updateUsingComposites(curi, "helloworld.composite");
        Assert.assertEquals(1, xs.size());
        Assert.assertEquals("sample-helloworld/compositeImpl.composite", xs.get(0));
    }

    @Test
    public void nestedTest1() throws NoSuchServiceException, NoSuchDomainException, ContributionReadException, ActivationException, ValidationException, XMLStreamException, FileNotFoundException {
        Node node = TuscanyRuntime.newInstance().createNode("NestedTest");
        String curi = node.installContribution("src/test/resources/helloworld2.jar");

        node.startComposite(curi, "compositeImpl.composite");

        String compositeURI = node.addDeploymentComposite(curi, new FileReader("src/test/resources/nested.composite"));
        node.startComposite(curi, compositeURI);
        
        List<String> xs = ((NodeImpl)node).updateUsingComposites(curi, "helloworld.composite");
        Assert.assertEquals(2, xs.size());
        Assert.assertTrue(xs.contains("helloworld2/compositeImpl.composite"));
        Assert.assertTrue(xs.contains("helloworld2/nested.composite"));
    }
}
