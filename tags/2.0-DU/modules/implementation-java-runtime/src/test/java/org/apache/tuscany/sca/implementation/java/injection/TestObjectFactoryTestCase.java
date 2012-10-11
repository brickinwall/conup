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
package org.apache.tuscany.sca.implementation.java.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev: 722905 $ $Date: 2008-12-03 14:35:23 +0000 (Wed, 03 Dec 2008) $
 */
public class TestObjectFactoryTestCase {

    private Constructor<Foo> ctor;

    @Test
    public void testConstructorInjection() throws Exception {
        List<ObjectFactory> initializers = new ArrayList<ObjectFactory>();
        initializers.add(new SingletonObjectFactory<String>("foo"));
        TestObjectFactory<Foo> factory = new TestObjectFactory<Foo>(ctor, initializers);
        Foo foo = factory.getInstance();
        assertEquals("foo", foo.foo);
    }

    /**
     * Verifies null parameters can be passed to a constructor. This is valid when a reference is optional during
     * constructor injection
     */
    @Test
    public void testConstructorInjectionOptionalParam() throws Exception {
        List<ObjectFactory> initializers = new ArrayList<ObjectFactory>();
        initializers.add(null);
        TestObjectFactory<Foo> factory = new TestObjectFactory<Foo>(ctor, initializers);
        Foo foo = factory.getInstance();
        assertNull(foo.foo);
    }

    @Test
    public void testConstructorInitializerInjection() throws Exception {
        TestObjectFactory<Foo> factory = new TestObjectFactory<Foo>(ctor);
        factory.setInitializerFactory(0, new SingletonObjectFactory<String>("foo"));
        Foo foo = factory.getInstance();
        assertEquals("foo", foo.foo);
    }

    @Before
    public void setUp() throws Exception {
        ctor = Foo.class.getConstructor(String.class);
    }

    private static class Foo {

        private String foo;

        public Foo(String foo) {
            this.foo = foo;
        }
    }
}
