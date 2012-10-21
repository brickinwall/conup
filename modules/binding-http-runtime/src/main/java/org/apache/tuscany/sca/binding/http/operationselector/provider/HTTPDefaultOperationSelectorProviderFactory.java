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

package org.apache.tuscany.sca.binding.http.operationselector.provider;

import org.apache.tuscany.sca.binding.http.operationselector.HTTPDefaultOperationSelector;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.OperationSelectorProvider;
import org.apache.tuscany.sca.provider.OperationSelectorProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * 
 * @version $Rev$ $Date$
*/
public class HTTPDefaultOperationSelectorProviderFactory implements OperationSelectorProviderFactory<HTTPDefaultOperationSelector>{

    public HTTPDefaultOperationSelectorProviderFactory(ExtensionPointRegistry extensionPoints) {
    }
    
    @Override
    public Class<HTTPDefaultOperationSelector> getModelType() {
        return HTTPDefaultOperationSelector.class;
    }

    @Override
    public OperationSelectorProvider createReferenceOperationSelectorProvider(RuntimeEndpointReference endpointReference) {
        return null;
    }

    @Override
    public OperationSelectorProvider createServiceOperationSelectorProvider(final RuntimeEndpoint endpoint) {
        return new OperationSelectorProvider(){
            @Override
            public Interceptor createInterceptor() {
                return new HTTPDefaultServiceOperationSelectorInterceptor(endpoint);
            }

            @Override
            public String getPhase() {
                return Phase.SERVICE_BINDING_OPERATION_SELECTOR;
            }};
    }

}
