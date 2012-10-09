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

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.builder.BuilderExtensionPoint;
import org.apache.tuscany.sca.assembly.builder.ContractBuilder;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.policy.ExtensionType;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * The assembly model object for an endpoint.
 *
 * @version $Rev: 1195400 $ $Date: 2011-10-31 10:34:23 +0000 (Mon, 31 Oct 2011) $
 */
public class EndpointImpl implements Endpoint {
    private static final long serialVersionUID = 7344399683703812593L;

    protected transient ExtensionPointRegistry registry;
    protected transient BuilderExtensionPoint builders;
    protected transient ContractBuilder contractBuilder;
    protected boolean unresolved;
    protected String uri;
    protected String deployedURI;
    protected Component component;
    protected ComponentService service;
    protected Binding binding;
    protected InterfaceContract interfaceContract;
    protected List<EndpointReference> callbackEndpointReferences = new ArrayList<EndpointReference>();
    protected List<PolicySet> policySets = new ArrayList<PolicySet>();
    protected List<Intent> requiredIntents = new ArrayList<Intent>();
    protected boolean remote = false;
    protected String specVersion = Base.SCA11_NS; 

    protected EndpointImpl(ExtensionPointRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    public Component getComponent() {
        resolve();
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
        reset();
    }

    public ComponentService getService() {
        resolve();
        return service;
    }

    public void setService(ComponentService service) {
        this.service = service;
        reset();
    }

    public Binding getBinding() {
        resolve();
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
        reset();
    }

    public InterfaceContract getComponentServiceInterfaceContract() {
        resolve();
        if (interfaceContract == null && service != null) {
            interfaceContract = service.getInterfaceContract();
        }
        return interfaceContract;
    }

    public void setInterfaceContract(InterfaceContract interfaceContract) {
        this.interfaceContract = interfaceContract;
    }

    /**
     * Get the services callbacl enpoint references that
     * represent endpoint references from which callbacks
     * originate
     *
     * @return callbackEndpoint the reference callback endpoint
     */
    public List<EndpointReference> getCallbackEndpointReferences() {
        resolve();
        return callbackEndpointReferences;
    }

    public List<PolicySet> getPolicySets() {
        resolve();
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        resolve();
        return requiredIntents;
    }

    public ExtensionType getExtensionType() {
        getBinding();
        if (binding instanceof PolicySubject) {
            return ((PolicySubject)binding).getExtensionType();
        }
        return null;
    }

    public void setExtensionType(ExtensionType type) {
        throw new UnsupportedOperationException();
    }

    public String toStringWithoutHash() {
        String output = "Endpoint: ";

        if (getURI() != null) {
            output += " URI = " + getURI();
        }

        if (unresolved) {
            output += " [Unresolved]";
        }

        return output;
    }

    public String toString() {
        return "(@" + this.hashCode() + ")" + toStringWithoutHash();
    }

    public String getURI() {
        if (uri == null) {
            if (component != null && service != null && binding != null) {
                String bindingName = binding.getName();
                if (bindingName == null) {
                    bindingName = service.getName();
                }
                uri = component.getURI() + "#service-binding(" + service.getName() + "/" + bindingName + ")";
            } else if (component != null && service != null) {
                uri = component.getURI() + "#service(" + service.getName() + ")";
            } else if (component != null) {
                uri = component.getURI();
            }
        }
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    protected void resolve() {
    }

    protected void reset() {
        this.uri = null;
    }

    protected void setExtensionPointRegistry(ExtensionPointRegistry registry) {
        this.registry = registry;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public boolean matches(String serviceURI) {
        String[] parts1 = parseServiceURI(serviceURI);
        String[] parts2 = parseStructuralURI(getURI());
        for (int i = 0; i < parts1.length; i++) {
            if (parts1[i] == null || parts1[i].equals(parts2[i])) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse the service URI into an array of names. The service URI is in one of the following formats:
     * <ul>
     * <li>componentName
     * <li>componentName/serviceName
     * <li>componentName/serviceName/bindingName
     * </ul> 
     * @param serviceURI
     * @return
     */
    private static String[] parseServiceURI(String serviceURI) {
        if (serviceURI.startsWith("/")) {
            serviceURI = serviceURI.substring(1);
        }
        if (serviceURI.contains("#")) {
            return parseStructuralURI(serviceURI);
        }
        String[] names = new String[3];
        String[] segments = serviceURI.split("/");
        for (int i = 0; i < names.length && i < segments.length; i++) {
            names[i] = segments[i];
        }
        return names;
    }

    /**
     * Parse the structural URI into an array of parts (componentURI, serviceName, bindingName)
     * @param structuralURI
     * @return [0]: componentURI [1]: serviceName [2]: bindingName
     */
    private static String[] parseStructuralURI(String structuralURI) {
        String[] names = new String[3];
        int index = structuralURI.lastIndexOf('#');
        if (index == -1) {
            names[0] = structuralURI;
        } else {
            names[0] = structuralURI.substring(0, index);
            String str = structuralURI.substring(index + 1);
            if (str.startsWith("service-binding(") && str.endsWith(")")) {
                str = str.substring("service-binding(".length(), str.length() - 1);
                String[] parts = str.split("/");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid service-binding URI: " + structuralURI);
                }
                names[1] = parts[0];
                names[2] = parts[1];
            } else if (str.startsWith("service(") && str.endsWith(")")) {
                str = str.substring("service(".length(), str.length() - 1);
                // [rfeng] Deal with empty service name
                if (!"".equals(str)) {
                    names[1] = str;
                }
            } else {
                throw new IllegalArgumentException("Invalid structural URI: " + structuralURI);
            }
        }
        return names;
    }

    public boolean isAsyncInvocation() {
        if (service != null && service.getName().endsWith("_asyncCallback")) {
            // this is a response service at the reference component so don't create a
            // response reference. 
            return false;
        } // end if

        for (Intent intent : getRequiredIntents()) {
            if (intent.getName().getLocalPart().equals("asyncInvocation")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDeployedURI() {
        return deployedURI == null ? (binding == null ? null : binding.getURI()) : deployedURI;
    }

    @Override
    public void setDeployedURI(String deployedURI) {
        this.deployedURI = deployedURI;
    }
    
    @Override
    public String getSpecVersion() {
        return specVersion;
    }
    
    @Override
    public void setSpecVersion(String specVersion){
        this.specVersion = specVersion;
    }

}
