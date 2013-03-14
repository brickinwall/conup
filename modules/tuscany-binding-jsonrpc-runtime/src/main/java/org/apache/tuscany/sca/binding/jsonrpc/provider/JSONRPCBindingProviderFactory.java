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

package org.apache.tuscany.sca.binding.jsonrpc.provider;

import org.apache.tuscany.sca.binding.jsonrpc.JSONRPCBinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostHelper;
import org.apache.tuscany.sca.host.http.client.HttpClientFactory;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * JSON-RPC Provider Factory
 * 
 * @version $Rev: 1164605 $ $Date: 2011-09-03 00:17:38 +0800 (周六, 03 九月 2011) $ 
 */
public class JSONRPCBindingProviderFactory implements BindingProviderFactory<JSONRPCBinding> {

    private MessageFactory messageFactory;
    private ServletHost servletHost;
    private HttpClientFactory httpClientFactory;

    public JSONRPCBindingProviderFactory(ExtensionPointRegistry extensionPoints) {
        this.servletHost = ServletHostHelper.getServletHost(extensionPoints);
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        messageFactory = modelFactories.getFactory(MessageFactory.class);
        this.httpClientFactory = HttpClientFactory.getInstance(extensionPoints);
    }
    
    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference endpointReference) {
        
        return new JSONRPCReferenceBindingProvider(httpClientFactory, endpointReference);
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint endpoint) {
        return new JSONRPCServiceBindingProvider(endpoint, messageFactory, servletHost);
    }

    public Class<JSONRPCBinding> getModelType() {
        return JSONRPCBinding.class;
    }

}
