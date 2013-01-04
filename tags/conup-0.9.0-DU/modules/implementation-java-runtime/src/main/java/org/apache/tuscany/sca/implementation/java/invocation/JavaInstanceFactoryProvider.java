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
package org.apache.tuscany.sca.implementation.java.invocation;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.implementation.java.JavaConstructorImpl;
import org.apache.tuscany.sca.implementation.java.JavaElementImpl;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.context.InstanceFactory;
import org.apache.tuscany.sca.implementation.java.context.InstanceFactoryProvider;
import org.apache.tuscany.sca.implementation.java.context.ReflectiveInstanceFactory;
import org.apache.tuscany.sca.implementation.java.injection.ArrayMultiplicityObjectFactory;
import org.apache.tuscany.sca.implementation.java.injection.FieldInjector;
import org.apache.tuscany.sca.implementation.java.injection.Injector;
import org.apache.tuscany.sca.implementation.java.injection.InvalidAccessorException;
import org.apache.tuscany.sca.implementation.java.injection.ListMultiplicityObjectFactory;
import org.apache.tuscany.sca.implementation.java.injection.MethodInjector;
import org.apache.tuscany.sca.implementation.java.introspect.JavaIntrospectionHelper;

/**
 * Encapsulates configuration for a Java-based atomic component
 * 
 * @version $Rev: 1145739 $ $Date: 2011-07-12 20:54:10 +0100 (Tue, 12 Jul 2011) $
 */
public class JavaInstanceFactoryProvider<T> implements InstanceFactoryProvider<T> {
    private JavaImplementation definition;
    private ProxyFactory proxyService;

    private final List<JavaElementImpl> injectionSites;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final Map<JavaElementImpl, Object> factories = new HashMap<JavaElementImpl, Object>();
	private final List<JavaElementImpl> callbackInjectionSites;

    public JavaInstanceFactoryProvider(JavaImplementation definition) {
        this.definition = definition;
        this.initInvoker = definition.getInitMethod() == null ? null : new MethodEventInvoker<T>(definition
            .getInitMethod());
        this.destroyInvoker = definition.getDestroyMethod() == null ? null : new MethodEventInvoker<T>(definition
            .getDestroyMethod());
        injectionSites = new ArrayList<JavaElementImpl>();
        callbackInjectionSites = new ArrayList<JavaElementImpl>();
    }

    ProxyFactory getProxyFactory() {
        return proxyService;
    }

    void setProxyFactory(ProxyFactory proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * @return the definition
     */
    JavaImplementation getImplementation() {
        return definition;
    }

    @SuppressWarnings("unchecked")
    public InstanceFactory<T> createFactory() {
        ObjectFactory<?>[] initArgs = getConstructorArgs();
        Injector<T>[] injectors = getInjectors(false);
        Injector<T>[] callbackInjectors = getInjectors(true);
        return new ReflectiveInstanceFactory<T>((Constructor<T>)definition.getConstructor().getConstructor(),
                                                initArgs, injectors, callbackInjectors, initInvoker, destroyInvoker);
    }

 

	private ObjectFactory<?>[] getConstructorArgs() {
        JavaConstructorImpl<?> constructor = definition.getConstructor();
        ObjectFactory<?>[] initArgs = new ObjectFactory<?>[constructor.getParameters().length];
        for (int i = 0; i < initArgs.length; i++) {
            ObjectFactory<?> factory = (ObjectFactory<?>)factories.get(constructor.getParameters()[i]);
            assert factory != null;
            initArgs[i] = factory;
        }
        return initArgs;
    }

	   
	   
    @SuppressWarnings("unchecked")
    private Injector<T>[] getInjectors(boolean callback) {
    	List<JavaElementImpl> sites = null;
    	if ( callback ) 
    		sites = callbackInjectionSites;
    	else
    		sites = injectionSites;
    	
        // work around JDK1.5 issue with allocating generic arrays
        Injector<T>[] injectors = new Injector[sites.size()];

        int i = 0;
        for (JavaElementImpl element : sites) {
            Object obj = factories.get(element);
            if (obj != null) {
                if (obj instanceof ObjectFactory) {
                    ObjectFactory<?> factory = (ObjectFactory<?>)obj;
                    Member member = (Member)element.getAnchor();
                    if (element.getElementType() == ElementType.FIELD) {
                        injectors[i++] = new FieldInjector<T>((Field)member, factory);
                    } else if (element.getElementType() == ElementType.PARAMETER && member instanceof Method) {
                        injectors[i++] = new MethodInjector<T>((Method)member, factory);
                    } else if (member instanceof Constructor) {
                        // Ignore
                    } else {
                        throw new AssertionError(String.valueOf(element));
                    }
                } else {
                    injectors[i++] = createMultiplicityInjector(element, (List<ObjectFactory<?>>)obj);
                }
            }
        }
        return injectors;
    }

    private Injector<T> createMultiplicityInjector(JavaElementImpl element, List<ObjectFactory<?>> factories) {
        Class<?> interfaceType = JavaIntrospectionHelper.getBaseType(element.getType(), element.getGenericType());

        if (element.getAnchor() instanceof Field) {
            Field field = (Field)element.getAnchor();
            if (field.getType().isArray()) {
                return new FieldInjector<T>(field, new ArrayMultiplicityObjectFactory(interfaceType, factories));
            } else {
                return new FieldInjector<T>(field, new ListMultiplicityObjectFactory(factories, field.getType()));
            }
        } else if (element.getAnchor() instanceof Method) {
            Method method = (Method)element.getAnchor();
            if (method.getParameterTypes()[0].isArray()) {
                return new MethodInjector<T>(method, new ArrayMultiplicityObjectFactory(interfaceType, factories));
            } else {
                return new MethodInjector<T>(method, new ListMultiplicityObjectFactory(factories, method.getParameterTypes()[0]));
            }
        } else {
            throw new InvalidAccessorException("Member must be a field or method: " + element.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public Class<T> getImplementationClass() {
        return (Class<T>)definition.getJavaClass();
    }

    public void setObjectFactory(JavaElementImpl element, ObjectFactory<?> objectFactory) {
        factories.put(element, objectFactory);
    }

    public void setObjectFactories(JavaElementImpl element, List<ObjectFactory<?>> objectFactory) {
        factories.put(element, objectFactory);
    }

    /**
     * @return the injectionSites
     */
    List<JavaElementImpl> getInjectionSites() {
        return injectionSites;
    }

    /**     
     * @return the callbackInjectionSites
     */
    public List<JavaElementImpl> getCallbackInjectionSites() {
		return callbackInjectionSites;
	}
    
    /**
     * @return the factories
     */
    Map<JavaElementImpl, Object> getFactories() {
        return factories;
    }

	

}
