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

import static org.apache.tuscany.sca.implementation.java.introspect.impl.ModelHelper.getService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.InvalidCallbackException;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.junit.Before;
import org.junit.Test;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Service;

/**
 * @version $Rev: 826368 $ $Date: 2009-10-18 08:22:23 +0100 (Sun, 18 Oct 2009) $
 */
public class ServiceCallbackTestCase {
    private ServiceProcessor processor;
    private JavaImplementationFactory javaImplementationFactory;

    @Before
    public void setUp() throws Exception {
        ExtensionPointRegistry registry = new DefaultExtensionPointRegistry();
        processor = new ServiceProcessor(new DefaultAssemblyFactory(), new DefaultJavaInterfaceFactory(registry));
        javaImplementationFactory = new DefaultJavaImplementationFactory();
    }

    @Test
    public void testMethodCallbackInterface() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooImpl.class, type);
        org.apache.tuscany.sca.assembly.Service service = getService(type, Foo.class.getSimpleName());
        assertNotNull(service);
        Method method = FooImpl.class.getMethod("setCallback", FooCallback.class);
        processor.visitMethod(method, type);
        assertEquals(method, type.getCallbackMembers().get(FooCallback.class.getName()).iterator().next().getAnchor());
    }

    @Test
    public void testFieldCallbackInterface() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooImpl.class, type);
        org.apache.tuscany.sca.assembly.Service service = getService(type, Foo.class.getSimpleName());
        assertNotNull(service);
        Field field = FooImpl.class.getDeclaredField("callback");
        processor.visitField(field, type);
        assertEquals(field, type.getCallbackMembers().get(FooCallback.class.getName()).iterator().next().getAnchor());
    }

    @Test
    public void testFieldCallbackInterface1() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooImpl1.class, type);
        org.apache.tuscany.sca.assembly.Service service = getService(type, Foo.class.getSimpleName());
        assertNotNull(service);
        Field field1 = FooImpl1.class.getDeclaredField("callbackRef");
        processor.visitField(field1, type);
        assertEquals(field1, type.getCallbackMembers().get(FooCallback.class.getName()).iterator().next().getAnchor());
        
    }
    
    @Test
    public void testMethodDoesNotMatchCallback() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(BadBarImpl.class, type);
        Method method = BadBarImpl.class.getMethod("setWrongInterfaceCallback", String.class);
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalCallbackReferenceException e) {
            // expected
        }
    }

    @Test
    public void testNoParamCallback() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(BadBarImpl.class, type);
        Method method = BadBarImpl.class.getMethod("setNoParamCallback");
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalCallbackReferenceException e) {
            // expected
        }
    }

    @Test
    public void testFieldDoesNotMatchCallback() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(BadBarImpl.class, type);
        Field field = BadBarImpl.class.getDeclaredField("wrongInterfaceCallback");
        try {
            processor.visitField(field, type);
            fail();
        } catch (IllegalCallbackReferenceException e) {
            // expected
        }
    }

    @Test
    public void testBadCallbackInterfaceAnnotation() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitClass(BadFooImpl.class, type);
            fail();
        } catch (IntrospectionException e) {
            // expected
            assertTrue(e.getCause() instanceof InvalidCallbackException);
        }
    }

    @Callback(FooCallback.class)
    private interface Foo {

    }

    private interface FooCallback {

    }

    @Service(Foo.class)
    private static class FooImpl implements Foo {

        @Callback
        protected FooCallback callback;
        
        @Callback
        public void setCallback(FooCallback cb) {

        }
    }
    
    @Service(Foo.class)
    private static class FooImpl1 implements Foo {
        @Callback
        protected ServiceReference<FooCallback> callbackRef;
    }    

    private static class BadBarImpl implements Foo {
        @Callback
        protected String wrongInterfaceCallback;

        @Callback
        public void setWrongInterfaceCallback(String cb) {

        }

        @Callback
        public void setNoParamCallback() {

        }

    }

    @Callback
    private interface BadFoo {

    }

    @Service(BadFoo.class)
    private static class BadFooImpl implements BadFoo {

    }

}
