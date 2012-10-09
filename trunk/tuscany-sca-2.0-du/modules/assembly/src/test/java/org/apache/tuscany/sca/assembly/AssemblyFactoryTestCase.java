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
package org.apache.tuscany.sca.assembly;

import javax.xml.namespace.QName;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test building of assembly model instances using the assembly factory.
 * 
 * @version $Rev: 827835 $ $Date: 2009-10-21 00:30:48 +0100 (Wed, 21 Oct 2009) $
 */
public class AssemblyFactoryTestCase {

    private static AssemblyFactory assemblyFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        assemblyFactory = new DefaultAssemblyFactory();
    }

    @Test
    public void testCreateComponent() {
        createComponent("AccountServiceComponent1");
    }

    @Test
    public void testCreateComponentType() {
        createComponentType();
    }

    @Test
    public void testCreateComposite() {
        createComposite();
    }

    /**
     * Create a composite
     */
    Composite createComposite() {
        Composite c = assemblyFactory.createComposite();

        Component c1 = createComponent("AccountServiceComponent1");
        c.getComponents().add(c1);
        Component c2 = createComponent("AccountServiceComponent2");
        c.getComponents().add(c2);

        Wire w = assemblyFactory.createWire();
        w.setSource(c1.getReferences().get(0));
        w.setTarget(c2.getServices().get(0));
        c.getWires().add(w);

        CompositeService cs = assemblyFactory.createCompositeService();
        cs.setName("AccountService");
        cs.setPromotedService(c1.getServices().get(0));
        cs.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        c.getServices().add(cs);
        cs.getBindings().add(new TestBinding(assemblyFactory));

        CompositeReference cr = assemblyFactory.createCompositeReference();
        cr.setName("StockQuoteService");
        cr.getPromotedReferences().add(c2.getReferences().get(1));
        cr.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        c.getReferences().add(cr);
        cr.getBindings().add(new TestBinding(assemblyFactory));

        return c;
    }

    /**
     * Create a new component
     */
    Component createComponent(String name) {
        Component c = assemblyFactory.createComponent();
        c.setName(name);

        Implementation i = new TestImplementation(assemblyFactory);
        c.setImplementation(i);

        ComponentProperty p = assemblyFactory.createComponentProperty();
        p.setName("currency");
        p.setValue("USD");
        p.setMustSupply(true);
        p.setXSDType(new QName("", ""));
        p.setProperty(i.getProperties().get(0));
        c.getProperties().add(p);

        ComponentReference ref1 = assemblyFactory.createComponentReference();
        ref1.setName("accountDataService");
        ref1.setMultiplicity(Multiplicity.ONE_ONE);
        ref1.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        ref1.setReference(i.getReferences().get(0));
        c.getReferences().add(ref1);
        ref1.getBindings().add(new TestBinding(assemblyFactory));

        ComponentReference ref2 = assemblyFactory.createComponentReference();
        ref2.setName("stockQuoteService");
        ref2.setMultiplicity(Multiplicity.ONE_ONE);
        ref2.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        ref2.setReference(i.getReferences().get(1));
        c.getReferences().add(ref2);
        ref2.getBindings().add(new TestBinding(assemblyFactory));

        ComponentService s = assemblyFactory.createComponentService();
        s.setName("AccountService");
        s.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        s.setService(i.getServices().get(0));
        c.getServices().add(s);
        s.getBindings().add(new TestBinding(assemblyFactory));

        return c;
    }

    /**
     * Create a new component type
     * 
     * @return
     */
    ComponentType createComponentType() {
        ComponentType ctype = assemblyFactory.createComponentType();

        Property p = assemblyFactory.createProperty();
        p.setName("currency");
        p.setValue("USD");
        p.setMustSupply(true);
        p.setXSDType(new QName("", ""));
        ctype.getProperties().add(p);

        Reference ref1 = assemblyFactory.createReference();
        ref1.setName("accountDataService");
        ref1.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        ref1.setMultiplicity(Multiplicity.ONE_ONE);
        ctype.getReferences().add(ref1);
        ref1.getBindings().add(new TestBinding(assemblyFactory));

        Reference ref2 = assemblyFactory.createReference();
        ref2.setName("stockQuoteService");
        ref2.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        ref2.setMultiplicity(Multiplicity.ONE_ONE);
        ctype.getReferences().add(ref2);
        ref2.getBindings().add(new TestBinding(assemblyFactory));

        Service s = assemblyFactory.createService();
        s.setName("AccountService");
        s.setInterfaceContract(new TestInterfaceContract(assemblyFactory));
        ctype.getServices().add(s);
        s.getBindings().add(new TestBinding(assemblyFactory));

        return ctype;
    }

}
