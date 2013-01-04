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
package org.apache.tuscany.sca.databinding.impl;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.apache.tuscany.sca.databinding.annotation.DataBinding;

/**
 *
 * @version $Rev: 776759 $ $Date: 2009-05-20 17:46:55 +0100 (Wed, 20 May 2009) $
 */
public class DataBindingTestCase {
    @org.junit.Test
    public void testDataType() throws Exception {
        Class<Test> testClass = Test.class;
        DataBinding d = testClass.getAnnotation(DataBinding.class);
        assertEquals(d.value(), "sdo");

        Method method = testClass.getMethod("test", new Class[] {Object.class});
        DataBinding d2 = method.getAnnotation(DataBinding.class);
        assertEquals(d2.value(), "jaxb");
    }

    @DataBinding("sdo")
    private static interface Test {
        @DataBinding("jaxb")
        Object test(Object object);
    }
}
