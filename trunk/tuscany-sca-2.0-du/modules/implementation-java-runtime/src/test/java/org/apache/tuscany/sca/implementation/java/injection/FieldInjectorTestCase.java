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

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev: 722905 $ $Date: 2008-12-03 14:35:23 +0000 (Wed, 03 Dec 2008) $
 */
public class FieldInjectorTestCase {

    protected Field protectedField;

    @Test
    public void testIllegalAccess() throws Exception {
        FieldInjector<Foo> injector = new FieldInjector<Foo>(protectedField, new SingletonObjectFactory<String>("foo"));
        Foo foo = new Foo();
        injector.inject(foo);
        assertEquals("foo", foo.hidden);
    }


    @Before
    public void setUp() throws Exception {
        protectedField = Foo.class.getDeclaredField("hidden");
    }

    private class Foo {
        private String hidden;
    }
}
