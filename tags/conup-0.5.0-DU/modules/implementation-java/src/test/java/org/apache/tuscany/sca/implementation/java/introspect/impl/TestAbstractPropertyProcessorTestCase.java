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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tuscany.sca.implementation.java.introspect.impl.ModelHelper.getProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaParameterImpl;
import org.apache.tuscany.sca.implementation.java.introspect.JavaClassVisitor;
import org.junit.Before;
import org.junit.Test;


/**
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class TestAbstractPropertyProcessorTestCase {

    private JavaClassVisitor extension;
    private JavaImplementationFactory javaImplementationFactory;

    @Test
    public void testVisitMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        extension.visitMethod(method, type);
        Property prop = getProperty(type, "test");
        assertNotNull(prop);
    }

    @Test
    public void testVisitNoParamsMethod() throws Exception {
        Method method = Foo.class.getMethod("setNoParamsBar");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            extension.visitMethod(method, type);
            fail();
        } catch (IllegalPropertyException e) {
            // expected
        }
    }

    @Test
    public void testVisitNonVoidMethod() throws Exception {
        Method method = Foo.class.getMethod("setBadBar", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            extension.visitMethod(method, type);
            fail();
        } catch (IllegalPropertyException e) {
            // expected
        }
    }

    @Test
    public void testDuplicateMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        extension.visitMethod(method, type);
        try {
            extension.visitMethod(method, type);
            fail();
        } catch (DuplicatePropertyException e) {
            // expected
        }
    }

    @Test
    public void testVisitField() throws Exception {
        Field field = Foo.class.getDeclaredField("d");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        extension.visitField(field, type);
        Property prop = getProperty(type, "test");
        assertNotNull(prop);
    }

    @Test
    public void testVisitConstructor() throws Exception {
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        JavaConstructorImpl<Foo> def = new JavaConstructorImpl<Foo>(ctor);
        JavaParameterImpl parameter = def.getParameters()[0];
        extension.visitConstructorParameter(parameter, type);
        assertEquals("test", def.getParameters()[0].getName());
        assertNotNull(getProperty(type, "test"));
    }

    @Before
    public void setUp() throws Exception {
        extension = new TestProcessor();
        javaImplementationFactory = new DefaultJavaImplementationFactory();
    }

    @Retention(RUNTIME)
    private @interface Bar {

    }

    private class TestProcessor extends AbstractPropertyProcessor<Bar> {

        public TestProcessor() {
            super(new DefaultExtensionPointRegistry(), Bar.class);
        }

        @Override
        protected void initProperty(Property property, Bar annotation) {
            // property.setDefaultValueFactory(EasyMock.createMock(ObjectFactory.class));
            property.setName("test");
        }

        @Override
        protected String getName(Bar annotation) {
            return "test";
        }

        @Override
        protected boolean getRequired(Bar annotation) {
            return true;
        }
    }

    private static class Foo {

        @Bar
        protected String d;

        public Foo(String a, @Bar
        String b) {
        }

        public Foo(@Bar
        String d) {
            this.d = d;
        }

        @Bar
        public void setBar(String d) {
            this.d = d;
        }

        @Bar
        public void setNoParamsBar() {
        }

        @Bar
        public String setBadBar(String d) {
            return null;
        }
    }
}
