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
package org.apache.tuscany.sca.implementation.spring;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;

/**
 * Represents a Spring implementation.
 *
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $
 */
public class SpringImplementation extends ImplementationImpl implements Implementation, Extensible {
    public final static QName TYPE = new QName(SCA11_NS, "implementation.spring");
    // The location attribute which points to the Spring application-context XML file
    private String location;
    // The application-context file as a Spring Resource
    private List<URL> resource;
    private ComponentType componentType;
    // Mapping of Services to Beans
    private Map<String, SpringBeanElement> serviceMap;
    // Mapping of property names to Java class
    private Map<String, Class<?>> propertyMap;
    // List of unresolved bean property references
    private Map<String, Reference> unresolvedBeanRef;
    private ClassLoader classLoader;

    public SpringImplementation() {
        super(TYPE);
        this.location = null;
        this.resource = null;
        setUnresolved(true);
        serviceMap = new HashMap<String, SpringBeanElement>();
        propertyMap = new HashMap<String, Class<?>>();
        unresolvedBeanRef = new HashMap<String, Reference>();
    } // end method SpringImplementation

    /* Returns the location attribute for this Spring implementation */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location attribute for this Spring implementation
     * location - a URI to the Spring application-context file
     */
    public void setLocation(String location) {
        this.location = location;
        return;
    }

    public void setResource(List<URL> resource) {
        this.resource = resource;
    }

    public List<URL> getResource() {
        return resource;
    }

    /*
     * Returns the componentType for this Spring implementation
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /*
     * Sets the componentType for this Spring implementation
     */
    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    @Override
    public List<Service> getServices() {
        return componentType.getServices();
    }

    @Override
    public List<Reference> getReferences() {
        return componentType.getReferences();
    }

    @Override
    public List<Property> getProperties() {
        return componentType.getProperties();
    }

    /**
     * Returns the Spring Bean which implements a particular service
     * @param service the service
     * @return the bean which implements the service, as a SpringBeanElement
     */
    public SpringBeanElement getBeanFromService(Service service) {
        SpringBeanElement theBean = serviceMap.get(service.getName());
        return theBean;
    }

    /**
     * Sets the mapping from a service to the Spring Bean that implements the service
     * @param service the service
     * @param theBean a SpringBeanElement for the Bean implementing the service
     */
    public void setBeanForService(Service service, SpringBeanElement theBean) {
        serviceMap.put(service.getName(), theBean);
    }

    /**
     * Add a mapping from a SCA property name to a Java class for the property
     * @param propertyName
     * @param propertyClass
     */
    public void setPropertyClass(String propertyName, Class<?> propertyClass) {
        if (propertyName == null || propertyClass == null)
            return;
        propertyMap.put(propertyName, propertyClass);
        return;
    } // end method setPropertyClass

    /**
     * Gets the Java Class for an SCA property
     * @param propertyName - the property name
     * @return - a Class object for the type of the property
     */
    public Class<?> getPropertyClass(String propertyName) {
        return propertyMap.get(propertyName);
    } // end method getPropertyClass

    public void setUnresolvedBeanRef(String refName, Reference reference) {
        if (refName == null || reference == null)
            return;
        unresolvedBeanRef.put(refName, reference);
        return;
    } // end method setUnresolvedBeanRef

    public Reference getUnresolvedBeanRef(String refName) {
        return unresolvedBeanRef.get(refName);
    } // end method getUnresolvedBeanRef

    /**
     * Use preProcess to validate and map the references and properties dynamically
     */
    public void build(Component component) {

        for (Reference reference : component.getReferences()) {
            if (unresolvedBeanRef.containsKey(reference.getName())) {
                Reference ref = unresolvedBeanRef.get(reference.getName());
                componentType.getReferences().add(createReference(reference, ref.getInterfaceContract()));
                unresolvedBeanRef.remove(reference.getName());
            }
        }

        for (Property property : component.getProperties()) {
            if (unresolvedBeanRef.containsKey(property.getName())) {
                componentType.getProperties().add(createProperty(property));
                this.setPropertyClass(property.getName(), property.getClass());
                unresolvedBeanRef.remove(property.getName());
            }
        }
    }

    protected Reference createReference(Reference reference, InterfaceContract interfaze) {
        Reference newReference;
        try {
            newReference = (Reference)reference.clone();
            if (newReference.getInterfaceContract() == null)
                newReference.setInterfaceContract(interfaze);
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // should not ever happen
        }
        return newReference;
    }

    protected Property createProperty(Property property) {
        Property newProperty;
        try {
            newProperty = (Property)property.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // should not ever happen
        }
        return newProperty;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SpringImplementation)) {
            return false;
        }
        SpringImplementation other = (SpringImplementation)obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpringImplementation [location=").append(location).append(", resource=").append(resource)
            .append("]");
        return builder.toString();
    }
}
