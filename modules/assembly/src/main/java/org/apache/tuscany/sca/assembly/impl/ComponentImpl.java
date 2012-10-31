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
package org.apache.tuscany.sca.assembly.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Represents a component.
 * 
 * @version $Rev: 827835 $ $Date: 2009-10-21 00:30:48 +0100 (Wed, 21 Oct 2009) $
 */
public class ComponentImpl extends ExtensibleImpl implements Component, Cloneable {
    private Implementation implementation;
    private String name;
    private String uri;
    private List<ComponentProperty> properties = new ArrayList<ComponentProperty>();
    private List<ComponentReference> references = new ArrayList<ComponentReference>();
    private List<ComponentService> services = new ArrayList<ComponentService>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private Boolean autowire;
    private ExtensionType type;

    /**
     * Constructs a new component.
     */
    protected ComponentImpl() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ComponentImpl clone = (ComponentImpl)super.clone();

        clone.properties = new ArrayList<ComponentProperty>();
        for (ComponentProperty property : getProperties()) {
            clone.properties.add((ComponentProperty)property.clone());
        }
        clone.references = new ArrayList<ComponentReference>();
        for (ComponentReference reference : getReferences()) {
            clone.references.add((ComponentReference)reference.clone());
        }
        clone.services = new ArrayList<ComponentService>();
        for (ComponentService service : getServices()) {
            clone.services.add((ComponentService)service.clone());
        }
        
        // Clone the implementation.composite
        if(implementation instanceof Composite) {
            clone.implementation = (Composite) ((Composite) implementation).clone();
        }
        
        // Clone the Lists for intents and policySets
        clone.requiredIntents = new ArrayList<Intent>(getRequiredIntents());
        clone.policySets = new ArrayList<PolicySet>(getPolicySets());
        
        return clone;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public Implementation getImplementation() {
        return implementation;
    }

    public String getName() {
        return name;
    }

    public List<ComponentProperty> getProperties() {
        return properties;
    }
    
    public ComponentProperty getProperty(String name) {
        for (ComponentProperty property : getProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    public List<ComponentReference> getReferences() {
        return references;
    }
    
    public ComponentReference getReference(String name){
        for (ComponentReference ref : getReferences()){
            if (ref.getName().equals(name)){
                return ref;
            }
        }
        return null;
    }      

    public List<ComponentService> getServices() {
        return services;
    }
    
    public ComponentService getService(String name) {
        for (ComponentService service : getServices()) {
            if (service.getName().equals(name)) {
                return service;
            }
        }
        return null;
    }

    public void setImplementation(Implementation implementation) {
        this.implementation = implementation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public boolean isAutowire() {
        return (autowire == null) ? false : autowire.booleanValue();
    }

    public void setAutowire(Boolean autowire) {
        this.autowire = autowire;
    }

    public Boolean getAutowire() {
        return autowire;
    }

    public ExtensionType getExtensionType() {
        return type;
    }

    public void setExtensionType(ExtensionType type) {
        this.type = type;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("Component (");
        buf.append("uri=").append(uri);
        buf.append(",name=").append(name);
        buf.append(",implementation=").append(implementation);
        buf.append(")");
        return buf.toString();
    }

}
