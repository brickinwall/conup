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
package org.apache.tuscany.sca.implementation.java.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuscany.sca.implementation.java.introspect.JavaIntrospectionHelper;
import org.junit.Test;

/**
 *
 * @version $Rev: 1242718 $ $Date: 2012-02-10 09:43:24 +0000 (Fri, 10 Feb 2012) $
 */
public class JavaIntrospectionHelperTestCase {

    private List testNoGenericsList;
    private List<String> testList;
    private Map<String, Bean1> testMap;
    private Entry[] testArray;
    private String[] testStringArray;

    @Test
    public void testBean1AllPublicProtectedFields() throws Exception {
        Set<Field> beanFields = JavaIntrospectionHelper.getAllPublicAndProtectedFields(Bean1.class, true);
        assertEquals(4, beanFields.size());                //Bean1.ALL_BEAN1_PUBLIC_PROTECTED_FIELDS
    }

    @Test
    public void testGetSuperAllMethods() throws Exception {
        Set<Method> superBeanMethods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(SuperBean.class, true);
        assertEquals(SuperBean.ALL_SUPER_METHODS, superBeanMethods.size());
    }

    @Test
    public void testGetBean1AllMethods() throws Exception {
        Set<Method> beanMethods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class, true);
        assertEquals(Bean1.ALL_BEAN1_METHODS, beanMethods.size());
    }

    @Test
    public void testOverrideMethod() throws Exception {
        Set<Method> beanFields = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class, true);
        boolean invoked = false;
        for (Method method : beanFields) {
            if (method.getName().equals("override")) {
                method.invoke(new Bean1(), "foo");
                invoked = true;
            }
        }
        if (!invoked) {
            throw new Exception("Override never invoked");
        }
    }

    @Test
    public void testNoOverrideMethod() throws Exception {
        Set<Method> beanFields = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class, true);
        boolean found = false;
        for (Method method : beanFields) {
            if (method.getName().equals("noOverride") && method.getParameterTypes().length == 0) {
                found = true;
            }
        }
        if (!found) {
            throw new Exception("No override not found");
        }
    }

    @Test
    public void testDefaultConstructor() throws Exception {
        Constructor<Bean2> ctr = JavaIntrospectionHelper.getDefaultConstructor(Bean2.class);
        assertEquals(ctr, Bean2.class.getConstructor());
        assertTrue(Bean2.class == ctr.newInstance((Object[]) null).getClass());
    }


    @Test
    public void testGetAllInterfaces() {
        Set<Class<?>> interfaces = JavaIntrospectionHelper.getAllInterfaces(Z.class);
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.contains(W.class));
        assertTrue(interfaces.contains(W2.class));
    }


    @Test
    public void testGetAllInterfacesObject() {
        Set<Class<?>> interfaces = JavaIntrospectionHelper.getAllInterfaces(Object.class);
        assertEquals(0, interfaces.size());
    }

    @Test
    public void testGetAllInterfacesNoInterfaces() {
        Set<Class<?>> interfaces = JavaIntrospectionHelper.getAllInterfaces(NoInterface.class);
        assertEquals(0, interfaces.size());
    }

    /**
     * Tests generics introspection capabilities
     */
    @Test
    public void testGenerics() throws Exception {

        List classes = JavaIntrospectionHelper.getGenerics(getClass().getDeclaredField("testList").getGenericType());
        assertEquals(1, classes.size());
        assertEquals(String.class, classes.get(0));

        classes =
            JavaIntrospectionHelper.getGenerics(getClass().getDeclaredField("testNoGenericsList").getGenericType());
        assertEquals(0, classes.size());

        classes = JavaIntrospectionHelper.getGenerics(getClass().getDeclaredField("testMap").getGenericType());
        assertEquals(2, classes.size());
        assertEquals(String.class, classes.get(0));
        assertEquals(Bean1.class, classes.get(1));

        classes = JavaIntrospectionHelper
            .getGenerics(getClass().getDeclaredMethod("fooMethod", Map.class).getGenericParameterTypes()[0]);
        assertEquals(2, classes.size());
        assertEquals(String.class, classes.get(0));
        assertEquals(Bean1.class, classes.get(1));

        classes = JavaIntrospectionHelper
            .getGenerics(getClass().getDeclaredMethod("fooMethod", List.class).getGenericParameterTypes()[0]);
        assertEquals(1, classes.size());
        assertEquals(String.class, classes.get(0));

    }

    private void fooMethod(List<String> foo) {

    }

    private void fooMethod(Map<String, Bean1> foo) {

    }

    public void setTestArray(Entry[] array) {
    }

    private interface W {

    }

    private interface W2 {

    }

    private class X implements W {

    }

    private class Y extends X implements W, W2 {

    }

    private class Z extends Y {

    }

    private class NoInterface {

    }

}
