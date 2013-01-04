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
package org.apache.tuscany.sca.assembly;

import java.io.Serializable;
import java.util.List;

import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents a service endpoint which is what results from having a configured
 * binding applied to a component service. In a deployed application an endpoint
 * will relate directly to a physical endpoint, for example, a HTTP URL or a
 * JMS destination.
 *
 * @version $Rev: 1195400 $ $Date: 2011-10-31 10:34:23 +0000 (Mon, 31 Oct 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface Endpoint extends Base, PolicySubject, Cloneable, Serializable {
    
    /**
     * Get the structural URI of the service binding
     * @return structural URI of the service/binding
     */
    String getURI();

    /**
     * Set the structural URI of the service binding
     * @param uri &lt;componentURI&gt;#service-binding(serviceName/bindingName)
     * or &lt;componentURI&gt;#service(serviceName)
     */
    void setURI(String uri);
    
    /**
     * Get the deployed URI
     * @return The deployed URI
     */
    String getDeployedURI();
    /**
     * Set the deployed URI
     * @param deployedURI
     */
    void setDeployedURI(String deployedURI);

    /**
     * Get the component model object
     *
     * @return component
     */
    Component getComponent();

    /**
     * Set the component model object
     *
     * @param component
     */
    void setComponent(Component component);

    /**
     * Get the service model object
     *
     * @return service
     */
    ComponentService getService();

    /**
     * Set the service model object
     *
     * @param service
     */
    void setService(ComponentService service);

    /**
     * Get the resolved target binding
     *
     * @return target binding
     */
    Binding getBinding();

    /**
     * Set the binding
     *
     * @param  binding
     */
    void setBinding(Binding binding);

    /**
     * Returns the interface contract defining the interface
     *
     * @return the interface contract
     */
    InterfaceContract getComponentServiceInterfaceContract();

    /**
     * Get the service's callback endpoint references that
     * represent endpoint references from which callbacks
     * originate
     *
     * @return callbackEndpoint the reference callback endpoint
     */
    List<EndpointReference> getCallbackEndpointReferences();

    /**
     * Returns true if this endpoint represents a serivce that is not
     * running in this JVM
     * 
     * @return true if the endpoint is remote
     */
    boolean isRemote();

    /**
     * Set true if this endpoint represents a serivce that is not
     * running in this JVM
     * 
     * @param remote set to true if the endpoint is remote
     */
    void setRemote(boolean remote);

    /**
     * Supports endpoint cloning
     *
     * @return endpoint
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Tests if the URI of this endpoint matches the service URI
     * @param serviceName the service URI to test
     * @return true if the endpoint URI matches the service URI
     */
    boolean matches(String serviceURI);
    
    /**
     * When true this endpoint is able to process the invocation
     * asynchronously. The forward call is effectively one-way
     * and the response will arrive asynchronously
     * 
     * @return true if the service is asynchronous
     */
    boolean isAsyncInvocation();    
    
    /** 
     * Get the spec version for this endpoint. This is intended to help 
     * interop scenarios where having an OASIS endpoint talk to an 
     * OSOA endpoint implies some restrictions. 
     * 
     * @return the spec version, namespace for either SCA-1.0 or SCA-1.1 
     */
    String getSpecVersion();
    
    /** 
     * Set the spec version for this endpoint. This is intended to help 
     * interop scenarios where having an OASIS endpoint talk to an 
     * OSOA endpoint implies some restrictions. If unset it defaults
     * to the namespace for SCA-1.1
     * 
     * @param specVersion the spec version, namspace for either SCA-1.0 or SCA-1.1
     */    
    void setSpecVersion(String specVersion);
    
}
