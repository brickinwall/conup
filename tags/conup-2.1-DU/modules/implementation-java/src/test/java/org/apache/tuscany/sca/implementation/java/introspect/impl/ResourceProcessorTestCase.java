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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaResourceImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev: 722852 $ $Date: 2008-12-03 12:09:11 +0000 (Wed, 03 Dec 2008) $
 */
public class ResourceProcessorTestCase {

    JavaImplementation type;
    ResourceProcessor processor = new ResourceProcessor(new DefaultAssemblyFactory());

    @Test
    public void testVisitField() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type);
        JavaResourceImpl resource = type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertNull(resource.getMappedName());
        assertEquals(field.getType(), resource.getElement().getType());
    }

    @Test
    public void testVisitMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar", Bar.class);
        processor.visitMethod(method, type);
        JavaResourceImpl resource = type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertNull(resource.getMappedName());
        assertEquals(method.getParameterTypes()[0], resource.getElement().getType());
    }

    @Test
    public void testVisitNamedMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar2", Bar.class);
        processor.visitMethod(method, type);
        JavaResourceImpl resource = type.getResources().get("someName");
        assertFalse(resource.isOptional());
        assertEquals("mapped", resource.getMappedName());
    }

    @Test
    public void testVisitBadMethod() throws Exception {
        Method method = Foo.class.getMethod("setBad");
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
    }

    @Test
    public void testDuplicateResources() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type);
        try {
            processor.visitField(field, type);
            fail();
        } catch (DuplicateResourceException e) {
            //expected
        }
    }

    @Before
    public void setUp() throws Exception {
        JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
        type = javaImplementationFactory.createJavaImplementation();
    }

    private class Foo {

        @org.apache.tuscany.sca.implementation.java.introspect.impl.Resource
        protected Bar bar;

        @org.apache.tuscany.sca.implementation.java.introspect.impl.Resource(optional = true)
        protected Bar barNotRequired;

        @org.apache.tuscany.sca.implementation.java.introspect.impl.Resource
        public void setBar(Bar bar) {
        }

        @org.apache.tuscany.sca.implementation.java.introspect.impl.Resource(name = "someName", mappedName = "mapped")
        public void setBar2(Bar bar) {
        }

        @org.apache.tuscany.sca.implementation.java.introspect.impl.Resource
        public void setBad() {
        }

    }

    private interface Bar {

    }
}
