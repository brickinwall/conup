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

package org.apache.tuscany.sca.binding.corba.provider;

import org.apache.tuscany.sca.binding.corba.CorbaBinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.host.corba.ExtensibleCorbaHost;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * @version $Rev: 928346 $ $Date: 2010-03-28 06:27:50 +0100 (Sun, 28 Mar 2010) $
 */
public class CorbaBindingProviderFactory implements BindingProviderFactory<CorbaBinding> {
    private ExtensibleCorbaHost host;
    
    public CorbaBindingProviderFactory(ExtensionPointRegistry registry) {
        host = ExtensibleCorbaHost.getInstance(registry);
    }
    /**
     * @see org.apache.tuscany.sca.provider.BindingProviderFactory#createReferenceBindingProvider(org.apache.tuscany.sca.runtime.RuntimeComponent, org.apache.tuscany.sca.runtime.RuntimeComponentReference, org.apache.tuscany.sca.assembly.Binding)
     */
    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference epr) {
        return new CorbaReferenceBindingProvider(epr, host);
    }

    /**
     * @see org.apache.tuscany.sca.provider.BindingProviderFactory#createServiceBindingProvider(org.apache.tuscany.sca.runtime.RuntimeComponent, org.apache.tuscany.sca.runtime.RuntimeComponentService, org.apache.tuscany.sca.assembly.Binding)
     */
    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint ep) {
        return new CorbaServiceBindingProvider(ep, host);
    }

    /**
     * @see org.apache.tuscany.sca.provider.ProviderFactory#getModelType()
     */
    public Class<CorbaBinding> getModelType() {
        return CorbaBinding.class;
    }

}
