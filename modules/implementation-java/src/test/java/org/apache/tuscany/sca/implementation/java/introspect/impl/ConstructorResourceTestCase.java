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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version $Rev: 1174086 $ $Date: 2011-09-22 13:22:20 +0100 (Thu, 22 Sep 2011) $
 */
public class ConstructorResourceTestCase extends AbstractProcessorTest {
    
    private JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();

    @Test
    public void testResource() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class);
        visitConstructor(ctor, type);
        org.apache.tuscany.sca.implementation.java.JavaResourceImpl resource = type.getResources().get("myResource");
        assertFalse(resource.isOptional());
    }

    @Test
    public void testTwoResourcesSameType() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        visitConstructor(ctor, type);
        assertNotNull(type.getResources().get("myResource1"));
        assertNotNull(type.getResources().get("myResource2"));
    }

    @Test
    public void testDuplicateResource() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<BadFoo> ctor = BadFoo.class.getConstructor(String.class, String.class);
        try {
            visitConstructor(ctor, type);
            fail();
        } catch (DuplicateResourceException e) {
            // expected
        }
    }

    @Test
    public void testNoName() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(String.class);
        try {
            visitConstructor(ctor, type);
            fail();
        } catch (InvalidResourceException e) {
            // expected
        }
    }

    @Test
    public void testNamesOnConstructor() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<Foo> ctor = Foo.class.getConstructor(Integer.class);
        visitConstructor(ctor, type);
        assertNotNull(type.getResources().get("myResource"));
    }

    @Test
    public void testInvalidNumberOfNames() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(Integer.class, Integer.class);
        try {
            visitConstructor(ctor, type);
            fail();
        } catch (InvalidResourceException e) {
            // expected
        }
    }

    @Ignore("TUSCANY-3950") // no names in constructor annotation now
    @Test
    public void testNoMatchingNames() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(List.class, List.class);
        try {
            visitConstructor(ctor, type);
            fail();
        } catch (InvalidConstructorException e) {
            // expected
        }
    }

    private static class Foo {

        @org.oasisopen.sca.annotation.Constructor
        public Foo(@Resource(name = "myResource") String resource) {

        }

        @org.oasisopen.sca.annotation.Constructor()
        public Foo(@Resource(name = "myResource") Integer resource) {

        }

        @org.oasisopen.sca.annotation.Constructor
        public Foo(@Resource(name = "myResource1") String res1, @Resource(name = "myResource2") String res2) {

        }

        @org.oasisopen.sca.annotation.Constructor
        public Foo(@Resource List res) {

        }
    }

    private static class BadFoo {

        @org.oasisopen.sca.annotation.Constructor
        public BadFoo(@Resource(name = "myResource") String res1, @Resource(name = "myResource") String res2) {

        }

        @org.oasisopen.sca.annotation.Constructor
        public BadFoo(@Resource String res) {

        }

        @org.oasisopen.sca.annotation.Constructor()
        public BadFoo(@Resource(name = "myResource") Integer res, @Resource Integer res2) {

        }

        @org.oasisopen.sca.annotation.Constructor()
        public BadFoo(@Resource(name = "myResource") List res, @Resource(name = "myOtherRes") List res2) {

        }

    }

}
