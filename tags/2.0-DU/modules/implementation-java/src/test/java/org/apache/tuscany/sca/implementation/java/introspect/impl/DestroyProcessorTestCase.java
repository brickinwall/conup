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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.junit.Test;
import org.oasisopen.sca.annotation.Destroy;

/**
 * @version $Rev: 925102 $ $Date: 2010-03-19 06:27:48 +0000 (Fri, 19 Mar 2010) $
 */
public class DestroyProcessorTestCase {
    
    private AssemblyFactory assemblyFactory = new DefaultAssemblyFactory();
    private JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();

    @Test
    public void testDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Foo.class.getMethod("destroy");
        processor.visitMethod(method, type);
        assertNotNull(type.getDestroyMethod());
    }

    @Test
    public void testBadDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Bar.class.getMethod("badDestroy", String.class);
        try {
            processor.visitMethod(method, type);
            fail();
        } catch (IllegalDestructorException e) {
            // expected
        }
    }

    @Test
    public void testTwoDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Bar.class.getMethod("destroy");
        Method method2 = Bar.class.getMethod("destroy2");
        processor.visitMethod(method, type);
        try {
            processor.visitMethod(method2, type);
            fail();
        } catch (DuplicateDestructorException e) {
            // expected
        }
    }

    @Test
    public void testProtectedDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Bar.class.getDeclaredMethod("protectedDestroy");
        try {
            processor.visitMethod(method, type);
        } catch (IllegalDestructorException e) {
            fail();
        }
    }

    @Test
    public void testPrivateDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Bar.class.getDeclaredMethod("privateDestroy");
        try {
            processor.visitMethod(method, type);
        } catch (IllegalDestructorException e) {
            fail();
        }
    }

    @Test
    public void testDefaultVisibilityDestroy() throws Exception {
        DestroyProcessor processor = new DestroyProcessor(assemblyFactory);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = Bar.class.getDeclaredMethod("defaultVisibilityDestroy");
        try {
            processor.visitMethod(method, type);
        } catch (IllegalDestructorException e) {
            fail();
        }
    }

    private class Foo {

        @Destroy
        public void destroy() {
        }
    }


    private class Bar {

        @Destroy
        public void destroy() {
        }

        @Destroy
        public void destroy2() {
        }

        @Destroy
        public void badDestroy(String foo) {
        }

        @Destroy
        protected void protectedDestroy(){
        }

        @Destroy
        private void privateDestroy(){
        }

        @Destroy
        void defaultVisibilityDestroy(){
        }

    }
}
