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

package org.apache.tuscany.sca.binding.sca.provider;

import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.invocation.InvokerAsyncResponse;
import org.apache.tuscany.sca.provider.EndpointAsyncProvider;
import org.apache.tuscany.sca.provider.EndpointProvider;
import org.apache.tuscany.sca.provider.SCABindingMapper;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * The service binding provider for the remote sca binding implementation. Relies on the
 * binding-ws-axis implementation for providing a remote message endpoint
 *
 * @version $Rev: 1228150 $ $Date: 2012-01-06 12:35:01 +0000 (Fri, 06 Jan 2012) $
 */
public class DelegatingSCAServiceBindingProvider implements EndpointAsyncProvider {

    // private static final Logger logger = Logger.getLogger(DelegatingSCAServiceBindingProvider.class.getName());

    private ServiceBindingProvider provider;
    private RuntimeEndpoint endpoint;
    private RuntimeEndpoint delegateEndpoint;
    private boolean started = false;

    public DelegatingSCAServiceBindingProvider(RuntimeEndpoint endpoint, SCABindingMapper mapper) {
        this.endpoint = endpoint;
        this.delegateEndpoint = mapper.map(endpoint);
        if (delegateEndpoint != null) {
            endpoint.setDelegateEndpoint(delegateEndpoint);
            provider = delegateEndpoint.getBindingProvider();    
        }

    }

    @Override
    public InterfaceContract getBindingInterfaceContract() {
        return provider.getBindingInterfaceContract();
    }

    @Override
    public boolean supportsOneWayInvocation() {
        return provider.supportsOneWayInvocation();
    }
    
    @Override
    public void configure() {  
        if (provider instanceof EndpointProvider){
            ((EndpointProvider)provider).configure();
        }
    }    
    
    @Override
    public boolean supportsNativeAsync() {
        if (provider instanceof EndpointAsyncProvider){
            return ((EndpointAsyncProvider)provider).supportsNativeAsync();
        } else {
            return false;
        }
    }
    
    @Override
    public InvokerAsyncResponse createAsyncResponseInvoker() {
        if (provider instanceof EndpointAsyncProvider){
            return ((EndpointAsyncProvider)provider).createAsyncResponseInvoker();
        } else {
            return null;
        }
    }

    public void start() {
        if (started) {
            return;
        } else {
            provider.start();
            // Set the resolved binding URI back to the binding.sca
            ((SCABinding)endpoint.getBinding()).setDelegateBindingType(delegateEndpoint.getBinding().getType().toString());
            ((SCABinding)endpoint.getBinding()).setDelegateBindingURI(delegateEndpoint.getBinding().getURI());
            endpoint.setDeployedURI(delegateEndpoint.getDeployedURI());
            started = true;
        }
    }

    public void stop() {
        if (!started) {
            return;
        }
        try {
            provider.stop();
        } finally {
            started = false;
        }
    }

    public ServiceBindingProvider getProviderDelegate() {
        return provider;
    }
    
    public RuntimeEndpoint getDelegateEndpoint(){
        return delegateEndpoint;
    }    

}
