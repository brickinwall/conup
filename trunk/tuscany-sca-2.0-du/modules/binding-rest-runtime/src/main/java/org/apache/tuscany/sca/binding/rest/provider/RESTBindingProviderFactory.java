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

package org.apache.tuscany.sca.binding.rest.provider;

import org.apache.tuscany.sca.binding.rest.RESTBinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostHelper;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;


/**
 * Factory for REST binding providers.
 *
 * @version $Rev: 962390 $ $Date: 2010-07-09 03:22:14 +0100 (Fri, 09 Jul 2010) $
 */
public class RESTBindingProviderFactory implements BindingProviderFactory<RESTBinding> {
    private ExtensionPointRegistry extensionPoints;
    private MessageFactory messageFactory;
    private ServletHost servletHost;

    public RESTBindingProviderFactory(ExtensionPointRegistry extensionPoints) {
        this.extensionPoints = extensionPoints;
        this.servletHost = ServletHostHelper.getServletHost(extensionPoints);
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        messageFactory = modelFactories.getFactory(MessageFactory.class);
    }

    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference endpointReference) {
        return new RESTReferenceBindingProvider(extensionPoints, endpointReference);
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint endpoint) {
        return new RESTServiceBindingProvider(endpoint, extensionPoints, messageFactory, servletHost);
    }

    public Class<RESTBinding> getModelType() {
        return RESTBinding.class;
    }
}
