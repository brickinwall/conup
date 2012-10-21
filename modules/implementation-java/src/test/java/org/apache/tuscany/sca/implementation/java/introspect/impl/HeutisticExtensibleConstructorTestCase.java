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
package org.apache.tuscany.sca.implementation.java.introspect.impl;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaElementImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.junit.Test;

/**
 * Verifies constructors that have extensible annotation types, i.e. that have
 * parameters marked by annotations which are themselves processed by some other
 * implementation processor
 * 
 * @version $Rev: 826368 $ $Date: 2009-10-18 08:22:23 +0100 (Sun, 18 Oct 2009) $
 */
public class HeutisticExtensibleConstructorTestCase extends AbstractProcessorTest {

    private org.apache.tuscany.sca.implementation.java.introspect.impl.HeuristicPojoProcessor processor;
    private JavaImplementationFactory javaImplementationFactory;

    public HeutisticExtensibleConstructorTestCase() {
        ExtensionPointRegistry registry = new DefaultExtensionPointRegistry();
        processor = new HeuristicPojoProcessor(new DefaultAssemblyFactory(), new DefaultJavaInterfaceFactory(registry));
        javaImplementationFactory = new DefaultJavaImplementationFactory();
    }

    private <T> void visitEnd(Class<T> clazz, JavaImplementation type) throws IntrospectionException {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            visitConstructor(constructor, type);
        }
        processor.visitEnd(clazz, type);
    }

    /**
     * Verifies heuristic processing can be called prior to an extension
     * annotation processors being called.
     */
    @Test
    public void testBarAnnotationProcessedFirst() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        JavaConstructorImpl<Foo> definition = new JavaConstructorImpl<Foo>(ctor);
        type.setConstructor(definition);
        Property property = factory.createProperty();
        property.setName("myBar");
        definition.getParameters()[0].setName("myBar");
        type.getProperties().add(property);
        visitEnd(Foo.class, type);
        assertEquals(2, type.getProperties().size());
    }

    /**
     * Verifies heuristic processing can be called before an extension
     * annotation processors is called. <p/> For example, given:
     * 
     * <pre>
     *  Foo(@Bar String prop, @org.oasisopen.sca.annotation.Property(name = &quot;foo&quot;) String prop2)
     * </pre>
     * 
     * <p/> Heuristic evaluation of
     * @Property can occur prior to another implementation processor evaluating
     * @Bar
     * @throws Exception
     */
    @Test
    public void testBarAnnotationProcessedLast() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        visitEnd(Foo.class, type);

        // now simulate process the bar impl
        JavaConstructorImpl<?> definition = type.getConstructor();
        definition.getParameters()[0].setName("myBar");
        Property property = factory.createProperty();
        property.setName("myBar");
        type.getProperties().add(property);

        assertEquals(2, type.getProperties().size());
        assertEquals("foo", definition.getParameters()[1].getName());
    }

    /**
     * Verifies heuristic processing can be called before an extension
     * annotation processors is called with the extension parameter in a middle
     * position. Specifically, verifies that the heuristic processor updates
     * injection names and preserves their ordering.
     */
    @Test
    public void testBarAnnotationProcessedFirstInMiddle() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<Foo2> ctor = Foo2.class.getConstructor(String.class, String.class, String.class);
        JavaConstructorImpl<Foo2> definition = new JavaConstructorImpl<Foo2>(ctor);
        type.setConstructor(definition);
        // insert placeholder for first param, which would be done by a
        // processor
        definition.getParameters()[0].setName("");
        Property property = factory.createProperty();
        // Hack to add a property member
        JavaElementImpl element = new JavaElementImpl("myBar", String.class, null);
        type.getPropertyMembers().put("myBar", element);
        property.setName("myBar");
        definition.getParameters()[1].setName("myBar");
        type.getProperties().add(property);
        visitEnd(Foo2.class, type);
        assertEquals("baz", definition.getParameters()[0].getName());
        assertEquals(2, type.getProperties().size());
        assertEquals(1, type.getReferences().size());
    }

    public @interface Bar {

    }

    public static class Foo {
        public Foo(@Bar
        String prop, @org.oasisopen.sca.annotation.Property(name = "foo")
        String prop2) {
        }
    }

    public static class Foo2 {
        public Foo2(@org.oasisopen.sca.annotation.Reference(name = "baz")
        String prop1, @Bar
        String prop2, @org.oasisopen.sca.annotation.Property(name = "foo")
        String prop3) {
        }
    }

}
