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

package org.apache.tuscany.sca.binding.websocket.runtime;

import org.apache.tuscany.sca.binding.websocket.WebsocketBinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostHelper;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * Factory for reference and service binding providers.
 */
public class WebsocketBindingProviderFactory implements BindingProviderFactory<WebsocketBinding> {

    private ExtensionPointRegistry extensionPoints;
    private ServletHost servletHost;

    public WebsocketBindingProviderFactory(ExtensionPointRegistry extensionPoints) {
        this.extensionPoints = extensionPoints;
        this.servletHost = ServletHostHelper.getServletHost(extensionPoints);
    }

    public Class<WebsocketBinding> getModelType() {
        return WebsocketBinding.class;
    }

    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference endpoint) {
        return new WebsocketReferenceBindingProvider(endpoint);
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint endpoint) {
        return new WebsocketServiceBindingProvider(extensionPoints, endpoint, servletHost);
    }

}
