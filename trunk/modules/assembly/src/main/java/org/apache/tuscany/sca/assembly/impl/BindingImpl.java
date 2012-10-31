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

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Base Binding implementation class
 */
public abstract class BindingImpl extends ExtensibleImpl implements Binding {
    protected QName type;

    private String name;
    private String uri;
    private ExtensionType extensionType;
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();


    protected BindingImpl(QName type) {
        super();
        this.type = type;
    }

    public QName getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }
    
    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(ExtensionType type) {
        this.extensionType = type;
    }
    
    public OperationSelector getOperationSelector() {
        return null;
    }

    public WireFormat getRequestWireFormat() {
        return null;
    }

    public WireFormat getResponseWireFormat() {
        return null;
    }

    public void setOperationSelector(OperationSelector operationSelector) {
    }

    public void setRequestWireFormat(WireFormat wireFormat) {
    }

    public void setResponseWireFormat(WireFormat wireFormat) {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getType()).append(" (");
        buf.append("uri=").append(getURI());
        buf.append(",name=").append(getName());
        buf.append(")");
        return buf.toString();
    }

}
