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

package org.apache.tuscany.sca.binding.atom.provider;

import org.apache.http.client.HttpClient;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.binding.atom.AtomBinding;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.host.http.client.HttpClientFactory;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * Implementation of the Atom binding provider.
 *
 * @version $Rev: 1304128 $ $Date: 2012-03-22 23:19:17 +0000 (Thu, 22 Mar 2012) $
 */
class AtomReferenceBindingProvider implements ReferenceBindingProvider {
    private HttpClientFactory httpClientFactory;
    private EndpointReference endpointReference;
    private RuntimeComponentReference reference;
    private AtomBinding binding;
    private String authorizationHeader = null;
    private HttpClient httpClient;
    private Mediator mediator;
    private DataType<?> itemClassType;
    private DataType<?> itemXMLType;
    private boolean supportsFeedEntries;

    /**
     * Constructs a new AtomReferenceBindingProvider
     * @param component
     * @param reference
     * @param binding
     * @param mediator
     */
    AtomReferenceBindingProvider(HttpClientFactory httpClientFactory,
                                 EndpointReference endpointReference,
                                 Mediator mediator) {
        this.httpClientFactory = httpClientFactory;
        this.endpointReference = endpointReference;
        this.reference = (RuntimeComponentReference)endpointReference.getReference();
        this.binding = (AtomBinding)endpointReference.getBinding();
        this.mediator = mediator;

        // Prepare authorization header
        // TUSCANY-3735: Don't send authorization header by default as this can cause problems.
        // Commented out the following two lines until we have a better way to control this.
        //String authorization = "admin" + ":" + "admin";
        //authorizationHeader = "Basic " + new String(Base64.encodeBase64(authorization.getBytes()));

    }

    public Invoker createInvoker(Operation operation) {
        
        String operationName = operation.getName();            
        String uri = endpointReference.getDeployedURI();
        if (operationName.equals("get")) { 

            // Determine the collection item type
            itemXMLType = new DataTypeImpl<Class<?>>(String.class.getName(), String.class, String.class);
            DataType<XMLType> outputType = operation.getOutputType().getLogical().get(0);
            itemClassType = outputType;
            if (itemClassType.getPhysical() == org.apache.abdera.model.Entry.class) {
                supportsFeedEntries = true;
            }

            return new AtomBindingInvoker.GetInvoker(operation, uri, httpClient, authorizationHeader, this);

        } else if (operationName.equals("post")) {
            return new AtomBindingInvoker.PostInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("put")) {
            return new AtomBindingInvoker.PutInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("delete")) {
            return new AtomBindingInvoker.DeleteInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("getFeed") || operationName.equals("getAll")) {
            return new AtomBindingInvoker.GetAllInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("postMedia")) {
            return new AtomBindingInvoker.PostMediaInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("putMedia")) {
            return new AtomBindingInvoker.PutMediaInvoker(operation, uri, httpClient, authorizationHeader, this);
        } else if (operationName.equals("query")) {
            return new AtomBindingInvoker.QueryInvoker(operation, uri, httpClient, authorizationHeader, this);
        }

        return new AtomBindingInvoker(operation, uri, httpClient, authorizationHeader, this);
    }

    public InterfaceContract getBindingInterfaceContract() {
        return reference.getInterfaceContract();
    }

    public void start() {

        // Configure the HTTP client credentials
        // TUSCANY-3735: Don't use authentication by default as this can cause problems.
        // Commented out the following four lines until we have a better way to control this.
        //Credentials credentials = new UsernamePasswordCredentials("admin", "admin");
        //httpClient.getParams().setAuthenticationPreemptive(true);
        //URI uri = URI.create(binding.getURI());
        //httpClient.getState().setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);

        // Find the get operation on the reference interface
        // Create an HTTP client
        httpClient = httpClientFactory.createHttpClient();
    }

    public void stop() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }    	
    }
    
    public boolean supportsOneWayInvocation() {
        return false;
    }

    /**
     * Returns the mediator.
     * @return
     */
    Mediator getMediator() {
        return mediator;
    }

    /**
     * Returns the item class type.
     * @return
     */
    DataType<?> getItemClassType() {
        return itemClassType;
    }

    /**
     * Returns the item XML type.
     * @return
     */
    DataType<?> getItemXMLType() {
        return itemXMLType;
    }
    
    /**
     * Returns true if the invoker should work with Atom
     * feed entries.
     * @return
     */
    boolean supportsFeedEntries() {
        return supportsFeedEntries;
    }

}
