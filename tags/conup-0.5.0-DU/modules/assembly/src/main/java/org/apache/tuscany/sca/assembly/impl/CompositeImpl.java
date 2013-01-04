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

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeReference;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.Wire;

/**
 * Implementation of a Composite.
 *
 * @version $Rev: 1154196 $ $Date: 2011-08-05 13:53:22 +0100 (Fri, 05 Aug 2011) $
 */
public class CompositeImpl extends ImplementationImpl implements Composite, Cloneable {
    private String specVersion = "undefined";
    private String contributionURI;
    private List<Component> components = new ArrayList<Component>();
    private List<Composite> includes = new ArrayList<Composite>();
    private List<Composite> fusedIncludes = new ArrayList<Composite>();
    private QName name;
    private List<Wire> wires = new ArrayList<Wire>();
    private Boolean autowire;
    private boolean local = true;

    /**
     * Constructs a new composite.
     */
    protected CompositeImpl() {
        super(TYPE);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CompositeImpl clone = (CompositeImpl)super.clone();

        // clone the child components
        clone.components = new ArrayList<Component>();
        for (Component component : getComponents()) {
            Component clonedComponent = (Component)component.clone();
            clone.components.add(clonedComponent);
        }
        
        // reset the references and services so they get re-resolved
        for (Service service : clone.getServices()) {
            CompositeService compositeService = (CompositeService)service;
            // Force the promoted component/service to be rebuilt against the clone
            if (compositeService.getPromotedComponent() != null) {
                compositeService.getPromotedComponent().setUnresolved(true);
            }
            if (compositeService.getPromotedService() != null) {
                compositeService.getPromotedService().setUnresolved(true);
            }
        }
        for (Reference reference : clone.getReferences()) {
            CompositeReference compositeReference = (CompositeReference)reference;
            for (ComponentReference ref : compositeReference.getPromotedReferences()) {
                // Force the promoted reference to be rebuilt against the clone
                ref.setUnresolved(true);
            }
        }
        
        // clone the wires
        clone.wires = new ArrayList<Wire>();
        for (Wire wire : getWires()) {
            clone.wires.add((Wire)wire.clone());
        }
        
        // Clone the includes 
        clone.includes = new ArrayList<Composite>();
        for (Composite included : getIncludes()) {
            clone.includes.add((Composite)included.clone());
        }
        return clone;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public List<Component> getComponents() {
        return components;
    }
    
    public Component getComponent(String name) {
        Component component = null;
        
        for (Component tmp : getComponents()){
            if (tmp.getName().equals(name)){
                component = tmp;
                break;
            }
        }
        return component;
    }

    public List<Composite> getIncludes() {
        return includes;
    }
    public List<Composite> getFusedIncludes() {
        return fusedIncludes;
    }

    public QName getName() {
        return name;
    }

    public List<Wire> getWires() {
        return wires;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
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

    public void setName(QName name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return String.valueOf(getName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Composite) {
            if (getName() != null) {
                return getName().equals(((Composite)obj).getName());
            } else {
                return ((Composite)obj).getName() == null;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Composite (name=" + getName() + ")";
    }

    @Override
    public String getContributionURI() {
        return contributionURI;
    }

    @Override
    public void setContributionURI(String contributionURI) {
        this.contributionURI = contributionURI;
    }
}
