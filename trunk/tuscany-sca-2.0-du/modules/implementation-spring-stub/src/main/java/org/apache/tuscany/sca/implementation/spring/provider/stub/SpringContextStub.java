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

package org.apache.tuscany.sca.implementation.spring.provider.stub;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.implementation.spring.SpringImplementation;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * This is the Tuscany side stub for the corresponding runtime tie class.
 * It enables the Tuscany code to invoke methods on a Spring context without
 * needing to know about any Spring classes. See the SpringContextTie class
 * in the implementation-spring-runtime module for what the tie does. 
 */
public class SpringContextStub {

    private static final String SPRING_IMPLEMENTATION_STUB =
        "org.apache.tuscany.sca.implementation.spring.context.tie.SpringImplementationStub";
    private static final String SPRING_CONTEXT_TIE =
        "org.apache.tuscany.sca.implementation.spring.context.tie.SpringContextTie";
    private Object tie;
    private Method startMethod;
    private Method closeMethod;
    private Method getBeanMethod;

    public SpringContextStub(RuntimeComponent component,
                             SpringImplementation implementation,
                             Object parentApplicationContext,
                             ProxyFactory proxyService,
                             PropertyValueFactory propertyValueObjectFactory) {

        initTie(component, implementation, parentApplicationContext, propertyValueObjectFactory);

    }

    private void initTie(RuntimeComponent component,
                         SpringImplementation implementation,
                         Object parentApplicationContext,
                         PropertyValueFactory propertyValueObjectFactory) {

        // TODO: what class loader to use?
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {

            Class<?> stubClass = Class.forName(SPRING_IMPLEMENTATION_STUB, true, cl);
            Constructor<?> stubConstructor = stubClass.getConstructor(new Class<?>[] {Object.class});
            Object stub =
                stubConstructor.newInstance(new SpringImplementationTie(implementation, parentApplicationContext,
                                                                        component, propertyValueObjectFactory));

            Class<?> tieClass = Class.forName(SPRING_CONTEXT_TIE, true, cl);
            Constructor<?> tieConstructor = tieClass.getConstructor(new Class<?>[] {stubClass, List.class});
            this.tie = tieConstructor.newInstance(stub, implementation.getResource());

            this.startMethod = tieClass.getMethod("start");
            this.closeMethod = tieClass.getMethod("close");
            this.getBeanMethod = tieClass.getMethod("getBean", String.class);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            startMethod.invoke(tie);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            closeMethod.invoke(tie);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String id) throws SpringInvocationException {
        try {

            return getBeanMethod.invoke(tie, id);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
