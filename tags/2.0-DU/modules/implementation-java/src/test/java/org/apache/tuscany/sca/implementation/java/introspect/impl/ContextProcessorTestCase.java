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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.junit.Before;
import org.junit.Test;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.ComponentName;
import org.oasisopen.sca.annotation.Context;

/**
 * @version $Rev: 738490 $ $Date: 2009-01-28 14:07:54 +0000 (Wed, 28 Jan 2009) $
 */
public class ContextProcessorTestCase  {
    private ContextProcessor processor;
    private ComponentNameProcessor nameProcessor;
    private JavaImplementationFactory javaImplementationFactory;

    @Test
    public void testComponentContextMethod() throws Exception {
        Method method = Foo.class.getMethod("setContext", ComponentContext.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitMethod(method, type);
        assertNotNull(type.getResources().get("context"));
    }

    @Test
    public void testComponentContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("context");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitField(field, type);
        assertNotNull(type.getResources().get("context"));
    }

    @Test
    public void testRequestContextMethod() throws Exception {
        Method method = Foo.class.getMethod("setRequestContext", RequestContext.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitMethod(method, type);
        assertNotNull(type.getResources().get("requestContext"));
    }

    @Test
    public void testRequestContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("requestContext");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitField(field, type);
        assertNotNull(type.getResources().get("requestContext"));
    }

    @Test
    public void testComponentNameMethod() throws Exception {
        Method method = Foo.class.getMethod("setName", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        nameProcessor.visitMethod(method, type);
        assertNotNull(type.getResources().get("name"));
    }

    @Test
    public void testComponentNameField() throws Exception {
        Field field = Foo.class.getDeclaredField("name");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        nameProcessor.visitField(field, type);
        assertNotNull(type.getResources().get("name"));
    }

    @Test
    public void testInvalidParamType() throws Exception {
        Method method = Foo.class.getMethod("setContext", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (UnknownContextTypeException e) {
            // expected
        }
    }

    @Test
    public void testInvalidParamTypeField() throws Exception {
        Field field = Foo.class.getDeclaredField("badContext");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitField(field, type);
            fail();
        } catch (UnknownContextTypeException e) {
            // expected
        }
    }


    @Test
    public void testInvalidParamNum() throws Exception {
        Method method = Foo.class.getMethod("setContext", ComponentContext.class, String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalContextException e) {
            // expected
        }
    }

    @Test
    public void testInvalidNoParams() throws Exception {
        Method method = Foo.class.getMethod("setContext");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalContextException e) {
            // expected
        }
    }

    @Test
    public void testNoContext() throws Exception {
        Method method = Foo.class.getMethod("noContext", ComponentContext.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitMethod(method, type);
        assertEquals(0, type.getResources().size());
    }

    @Test
    public void testNoContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("noContext");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitField(field, type);
        assertEquals(0, type.getResources().size());
    }

    @Before
    public void setUp() throws Exception {
        javaImplementationFactory = new DefaultJavaImplementationFactory();
        processor = new ContextProcessor(new DefaultAssemblyFactory());
        nameProcessor = new ComponentNameProcessor(new DefaultAssemblyFactory());
    }

    private class Foo {
        @Context
        protected ComponentContext context;

        @ComponentName
        protected String name;

        @Context
        protected Object badContext;

        protected ComponentContext noContext;

        @Context
        protected RequestContext requestContext;

        @Context
        public void setContext(ComponentContext context) {

        }

        @ComponentName
        public void setName(String name) {

        }

        @Context
        public void setContext(String context) {

        }

        @Context
        public void setContext(ComponentContext context, String string) {

        }

        @Context
        public void setContext() {

        }

        public void noContext(ComponentContext context) {

        }

        @Context
        public void setRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
        }
    }
}
