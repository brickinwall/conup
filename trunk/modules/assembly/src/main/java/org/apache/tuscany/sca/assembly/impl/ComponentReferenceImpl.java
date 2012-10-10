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

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.CompositeReference;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;

/**
 * Represents a component reference
 * 
 * @version $Rev: 791550 $ $Date: 2009-07-06 18:39:44 +0100 (Mon, 06 Jul 2009) $
 */
public class ComponentReferenceImpl extends ReferenceImpl implements ComponentReference, Cloneable {
    private Reference reference;
    private Boolean autowire;
    private boolean nonOverridable;
    private List<CompositeReference> promotedAs = new ArrayList<CompositeReference>();
    private ComponentService callbackService;
    private boolean isPromoted = false;

    /**
     * Constructs a new component reference.
     */
    protected ComponentReferenceImpl() {
        // Set multiplicity to null so that by default it'll inherit from the Reference
        setMultiplicity(null);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
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

    public List<CompositeReference> promotedAs() {
        return promotedAs;
    }

    public ComponentService getCallbackService() {
        return callbackService;
    }

    public void setCallbackService(ComponentService callbackService) {
        this.callbackService = callbackService;
    }

    /**
     * Use endpoint information to work out what the interface contract for the
     * binding is. 
     */
    @Override
    public InterfaceContract getInterfaceContract(Binding binding) {
        InterfaceContract interfaceContract = null;

        if (interfaceContract == null) {
            interfaceContract = getInterfaceContract();
        }
        return interfaceContract;
    }

    public boolean isNonOverridable() {
        return nonOverridable;
    }

    public void setNonOverridable(boolean nonOverridable) {
        this.nonOverridable = nonOverridable;
    }

    public void setPromoted(boolean isPromoted) {
        this.isPromoted = isPromoted;
    } // end method setPromoted

    public boolean isPromoted() {
        return isPromoted;
    }
} // end class ComponentReferenceImpl
