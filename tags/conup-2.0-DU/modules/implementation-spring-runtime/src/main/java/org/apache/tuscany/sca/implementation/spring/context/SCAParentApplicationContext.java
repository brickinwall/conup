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
package org.apache.tuscany.sca.implementation.spring.context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import org.apache.tuscany.sca.implementation.spring.provider.SpringImplementationWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

/**
 * A Spring ParentApplicationContext for a given Spring Implementation
 *
 * The Parent application context is responsible for handling those entities within a Spring
 * application context that actually belong to SCA rather than to Spring.  The principal things
 * are Properties and References.  These may be present either through explicit <sca:property/>
 * and <sca:reference/> elements in the application context or they may be implicit through
 * unresolved Spring bean <property.../> elements.  In either case, it is the Parent application
 * context that must provide Spring beans that correspond to the property or reference, as derived
 * from the SCA composite in which the Spring application context is an implementation.
 *
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $
 */
public class SCAParentApplicationContext implements ApplicationContext {

    // The Spring implementation for which this is the parent application context
    private SpringImplementationWrapper implementation;

    private static final String[] EMPTY_ARRAY = new String[0];

    public SCAParentApplicationContext(SpringImplementationWrapper implementation) {
        this.implementation = implementation;
    } // end constructor

    public Object getBean(String name) throws BeansException {
        return getBean(name, (Class)null);
    }

    /**
     * Get a Bean for a reference or for a property.
     *
     * @param name - the name of the Bean required
     * @param requiredType - the required type of the Bean (either a Java class or a Java interface)
     * @return Object - a Bean which matches the requested bean
     */
    public Object getBean(String name, Class requiredType) throws BeansException {
        Object bean = implementation.getBean(name, requiredType);
        if (bean == null && getParent() != null) {
            bean = getParent().getBean(name, requiredType);
        }
        if (bean == null) {
            throw new NoSuchBeanDefinitionException("Unable to find Bean with name " + name);
        } else {
            return bean;
        }
    } // end method getBean( String, Class )

    public Object getBean(String name, Object[] args) throws BeansException {
        return getBean(name, ((Class)null));
    }

    public <T> T getBean(Class<T> clazz) throws BeansException {
        return clazz.cast(getBean(clazz.getName(), clazz));
    }

    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> clazz) throws BeansException {
        return null;
    }

    public <A extends Annotation> A findAnnotationOnBean(String arg0, Class<A> clazz) {
        return null;
    }

    public boolean containsBean(String name) {
        // TODO
        return false;
    }

    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        // TODO
        return false;
    }

    public boolean isTypeMatch(String name, Class targetType) throws NoSuchBeanDefinitionException {
        throw new UnsupportedOperationException();
    }

    public Class getType(String name) throws NoSuchBeanDefinitionException {
        return null;
    }

    public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return EMPTY_ARRAY;
    }

    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return null;
    }

    public String getId() {
        return this.toString();
    }

    public String getDisplayName() {
        return implementation.getURI();
    }

    public long getStartupDate() {
        return 0;
    }

    public boolean containsBeanDefinition(String beanName) {
        return false;
    }

    public int getBeanDefinitionCount() {
        return 0;
    }

    public String[] getBeanDefinitionNames() {
        return new String[0];
    }

    public String[] getBeanNamesForType(Class type) {
        return new String[0];
    }

    public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
        return new String[0];
    }

    public Map getBeansOfType(Class type) throws BeansException {
        return null;
    }

    public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) throws BeansException {
        return null;
    }

    public boolean isPrototype(String theString) {
        return false;
    }

    public BeanFactory getParentBeanFactory() {
        return null;
    }

    public boolean containsLocalBean(String name) {
        return false;
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return null;
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return null;
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return null;
    }

    public void publishEvent(ApplicationEvent event) {

    }

    public Resource[] getResources(String locationPattern) throws IOException {
        return new Resource[0];
    }

    public Resource getResource(String location) {
        return null;
    }

    public ClassLoader getClassLoader() {
        // REVIEW: this is almost certainly flawed, but it's not clear how the SCA runtime's
        // resource loading mechanism is exposed right now.
        return this.getClass().getClassLoader();
    }

    @Override
    public ApplicationContext getParent() {
        return implementation.getParentApplicationContext();
    }

}
