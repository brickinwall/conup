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

package org.apache.tuscany.sca.implementation.spring.provider;

import java.net.URL;
import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.implementation.spring.SpringImplementation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.springframework.context.ApplicationContext;

/**
 * Wrapper for SpringImplementation
 */
public class SpringImplementationWrapper {

    private SpringImplementation implementation;
    private ApplicationContext parentApplicationContext;
    private RuntimeComponent component;
    private PropertyValueFactory propertyFactory;

    public SpringImplementationWrapper(SpringImplementation implementation,
                                   RuntimeComponent component,
                                   PropertyValueFactory propertyFactory) {
        this.implementation = implementation;
        this.component = component;
        this.propertyFactory = propertyFactory;
    }

    public String getURI() {
        return implementation.getURI();
    }
    
    public List<URL> getResource() {
        return implementation.getResource();
    }

    public String getComponentName() {
        return component.getName();
    }

    /**
     * Method to create a Java Bean for a Property value
     * @param <B> the class type of the Bean
     * @param requiredType - a Class object for the required type
     * @param name - the Property name
     * @return - a Bean of the specified property, with value set
     */
    private <B> B getPropertyBean(Class<?> requiredType, String name) {
        B propertyObject = null;
        // Get the component's list of properties
        List<ComponentProperty> props = component.getProperties();
        for (ComponentProperty prop : props) {
            if (prop.getName().equals(name)) {
                // On finding the property, create a factory for it and create a Bean using
                // the factory
                propertyObject = (B)propertyFactory.createPropertyValue(prop, requiredType);
            } // end if
        } // end for

        return propertyObject;
    }

    /**
     * Creates a proxy Bean for a reference
     * @param <B> the Business interface type for the reference
     * @param businessInterface - the business interface as a Class
     * @param referenceName - the name of the Reference
     * @return an Bean of the type defined by <B>
     */
    private <B> B getService(Class<B> businessInterface, String referenceName) {
        return component.getComponentContext().getService(businessInterface, referenceName);
    }

    /**
     * Get a Bean for a reference or for a property.
     *
     * @param name - the name of the Bean required
     * @param requiredType - the required type of the Bean (either a Java class or a Java interface)
     * @return Object - a Bean which matches the requested bean
     */
    public Object getBean(String name, Class<?> requiredType) {
        // The expectation is that the requested Bean is either a reference or a property
        // from the Spring context
        for (Reference reference : implementation.getReferences()) {
            if (reference.getName().equals(name)) {
                // Extract the Java interface for the reference (it can't be any other interface type
                // for a Spring application context)
                if (requiredType == null) {
                    JavaInterface javaInterface = (JavaInterface)reference.getInterfaceContract().getInterface();
                    requiredType = javaInterface.getJavaClass();
                }
                // Create and return the proxy for the reference
                return getService(requiredType, reference.getName());
            } // end if
        } // end for

        // For a property, get the name and the required Java type and create a Bean
        // of that type with the value inserted.
        for (Property property : implementation.getProperties()) {
            if (property.getName().equals(name)) {
                if (requiredType == null) {
                    // The following code only deals with a subset of types and was superceded
                    // by the information from the implementation (which uses Classes as found
                    // in the Spring implementation itself.
                    //requiredType = JavaXMLMapper.getJavaType( property.getXSDType() );
                    requiredType = implementation.getPropertyClass(name);
                }
                return getPropertyBean(requiredType, property.getName());
            } // end if
        } // end for
          // TODO: NoSuchBeanException
          // throw new RuntimeException("Unable to find Bean with name " + name);
        return null;

    } // end method getBean( String, Class )

    public ComponentWrapper getComponentWrapper() {
        return new ComponentWrapper(component);
    }

    public PropertyValueWrapper getPropertyValueWrapper() {
        return new PropertyValueWrapper(component, propertyFactory);
    }

    public ClassLoader getClassLoader() {
        return implementation.getClassLoader();
    }

    public ApplicationContext getParentApplicationContext() {
        return parentApplicationContext;
    }

    public void setParentApplicationContext(ApplicationContext parentApplicationContext) {
        this.parentApplicationContext = parentApplicationContext;
    }

}
