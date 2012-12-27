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

import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeReference;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.builder.BuilderContext;
import org.apache.tuscany.sca.monitor.DefaultMonitorFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the CompositeBuilder.
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class CompositeBuilderTestCase {

    private static AssemblyFactory assemblyFactory;
    private static Monitor monitor;

    @BeforeClass
    public static void setUp() throws Exception {
        assemblyFactory = new DefaultAssemblyFactory();
        MonitorFactory mf = new DefaultMonitorFactory();
        monitor = mf.createMonitor();
    }

    @Test
    public void testFuseIncludes() throws Exception {
        Composite c1 = assemblyFactory.createComposite();
        c1.setName(new QName("http://foo", "C1"));
        Component a = assemblyFactory.createComponent();
        a.setName("a");
        c1.getComponents().add(a);
        CompositeService s = assemblyFactory.createCompositeService();
        s.setName("s");
        c1.getServices().add(s);
        CompositeReference r = assemblyFactory.createCompositeReference();
        r.setName("r");
        c1.getReferences().add(r);

        Composite c2 = assemblyFactory.createComposite();
        c2.setName(new QName("http://foo", "C2"));
        c1.getIncludes().add(c2);
        Component b = assemblyFactory.createComponent();
        b.setName("b");
        c2.getComponents().add(b);

        Composite c = assemblyFactory.createComposite();
        c.setName(new QName("http://foo", "C"));
        c.getIncludes().add(c1);

        new CompositeIncludeBuilderImpl().build(c, new BuilderContext(monitor));

        assertTrue(c.getComponents().get(0).getName().equals("a"));
        assertTrue(c.getComponents().get(1).getName().equals("b"));
        assertTrue(c.getServices().get(0).getName().equals("s"));
        assertTrue(c.getReferences().get(0).getName().equals("r"));
    }

    @Test
    public void testExpandComposites() throws Exception {
        Composite c1 = assemblyFactory.createComposite();
        c1.setName(new QName("http://foo", "C1"));
        Component a = assemblyFactory.createComponent();
        a.setName("a");
        c1.getComponents().add(a);
        CompositeService s = assemblyFactory.createCompositeService();
        s.setName("s");
        c1.getServices().add(s);
        CompositeReference r = assemblyFactory.createCompositeReference();
        r.setName("r");
        c1.getReferences().add(r);

        Composite c2 = assemblyFactory.createComposite();
        c2.setName(new QName("http://foo", "C2"));
        Component b = assemblyFactory.createComponent();
        b.setName("b");
        c2.getComponents().add(b);

        Composite c = assemblyFactory.createComposite();
        c.setName(new QName("http://foo", "C"));
        Component x = assemblyFactory.createComponent();
        x.setName("x");
        x.setImplementation(c1);
        c.getComponents().add(x);
        Component y = assemblyFactory.createComponent();
        y.setName("y");
        y.setImplementation(c2);
        c.getComponents().add(y);
        Component z = assemblyFactory.createComponent();
        z.setName("z");
        z.setImplementation(c1);
        c.getComponents().add(z);

        new CompositeCloneBuilderImpl().build(c, new BuilderContext(monitor));

        assertTrue(c.getComponents().get(0).getImplementation() != c1);
        assertTrue(c.getComponents().get(1).getImplementation() != c2);
        assertTrue(c.getComponents().get(2).getImplementation() != c1);

        Composite i = (Composite)c.getComponents().get(0).getImplementation();
        assertTrue(i.getComponents().get(0) != a);
        assertTrue(i.getComponents().get(0).getName().equals("a"));
        assertTrue(i.getServices().get(0).getName().equals("s"));
        assertTrue(i.getServices().get(0) != s);
        assertTrue(i.getReferences().get(0).getName().equals("r"));
        assertTrue(i.getReferences().get(0) != r);
    }

}
