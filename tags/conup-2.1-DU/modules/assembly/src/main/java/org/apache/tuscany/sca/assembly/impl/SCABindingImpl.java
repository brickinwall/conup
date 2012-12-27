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

import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * The assembly mode object for an SCA binding.
 *
 * @version $Rev: 1228150 $ $Date: 2012-01-06 12:35:01 +0000 (Fri, 06 Jan 2012) $
 */
public class SCABindingImpl implements SCABinding, Extensible, PolicySubject {
    private String name;
    private String uri;
    private List<Object> extensions = new ArrayList<Object>();
    private List<Extension> attributeExtensions = new ArrayList<Extension>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private ExtensionType extensionType;
    private WireFormat wireFormat;
    private String delegateBindingType;
    private String delegateBindingURI;

    /**
     * Constructs a new SCA binding.
     */
    protected SCABindingImpl() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getters for the binding URI. The computed URI for the
     * service that the reference is targeting or which the service represents
     * depending on whether the biding is associated with a reference or
     * service
     *
     * @return the binding URI
     */
    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public List<Object> getExtensions() {
        return extensions;
    }

    public List<Extension> getAttributeExtensions() {
        return attributeExtensions;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(ExtensionType intentAttachPointType) {
        this.extensionType = intentAttachPointType;
    }

    public void setPolicySets(List<PolicySet> policySets) {
        this.policySets = policySets;
    }

    public void setRequiredIntents(List<Intent> intents) {
        this.requiredIntents = intents;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public QName getType() {
        return TYPE;
    }
    
    public WireFormat getRequestWireFormat() {
        return wireFormat;
    }
    
    public void setRequestWireFormat(WireFormat wireFormat) {  
        this.wireFormat = wireFormat;
    }
    
    public WireFormat getResponseWireFormat() {
        return wireFormat;
    }
    
    public void setResponseWireFormat(WireFormat wireFormat) {
        this.wireFormat = wireFormat;
    }
    
    public OperationSelector getOperationSelector() {
        return null;
    }
    
    public void setOperationSelector(OperationSelector operationSelector) {
    } 
    
    public String getDelegateBindingType() {
        return delegateBindingType;
    }
    
    public void setDelegateBindingType(String delegateBindingType) {
        this.delegateBindingType = delegateBindingType;
    }
    
    public String getDelegateBindingURI() {
        return delegateBindingURI;
    }
    
    public void setDelegateBindingURI(String delegateBindingURI) {
        this.delegateBindingURI = delegateBindingURI;
    }
}
