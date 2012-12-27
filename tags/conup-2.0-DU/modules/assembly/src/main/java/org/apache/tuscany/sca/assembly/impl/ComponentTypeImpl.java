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

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

/** 
 * Represents a component type.
 * 
 * @version $Rev: 924089 $ $Date: 2010-03-17 01:40:44 +0000 (Wed, 17 Mar 2010) $
 */
public class ComponentTypeImpl extends ExtensibleImpl implements ComponentType, Cloneable {
    private String uri;
    private List<Property> properties = new ArrayList<Property>();
    private List<Reference> references = new ArrayList<Reference>();
    private List<Service> services = new ArrayList<Service>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();

    /**
     * Constructs a new component type.
     */
    protected ComponentTypeImpl() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ComponentTypeImpl clone = (ComponentTypeImpl)super.clone();

        clone.services = new ArrayList<Service>();
        for (Service service : getServices()) {
            clone.services.add((Service)service.clone());
        }
        clone.references = new ArrayList<Reference>();
        for (Reference reference : getReferences()) {
            clone.references.add((Reference)reference.clone());
        }
        clone.properties = new ArrayList<Property>();
        for (Property property : getProperties()) {
            clone.properties.add((Property)property.clone());
        }
        clone.requiredIntents = new ArrayList<Intent>(requiredIntents);
        clone.policySets = new ArrayList<PolicySet>(policySets);
        return clone;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public List<Property> getProperties() {
        return properties;
    }
    
    public Property getProperty(String name){
        Property property = null;
        
        for (Property tmp : getProperties()){
            if (tmp.getName().equals(name)){
                property = tmp;
                break;
            }
        }
        
        return property;
    }

    public List<Reference> getReferences() {
        return references;
    }
    
    public Reference getReference(String name){
        Reference reference = null;
        
        for (Reference tmp : getReferences()){
            if (tmp.getName().equals(name)){
                reference = tmp;
                break;
            }
        }
        
        return reference;
    }    

    public List<Service> getServices() {
        return services;
    }
    
    public Service getService(String name){
        Service service = null;
        
        for (Service tmp : getServices()){
            if (tmp.getName().equals(name)){
                service = tmp;
                break;
            }
        }
        
        return service;
    }  


    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public ExtensionType getExtensionType() {
        return null;
    }

    public void setExtensionType(ExtensionType type) {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentTypeImpl other = (ComponentTypeImpl)obj;
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }
}
