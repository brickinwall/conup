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
package org.apache.tuscany.sca.binding.ejb.provider;

import org.apache.tuscany.sca.binding.ejb.EJBBinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * A factory from creating the EJB binding provider.
 *
 * @version $Rev: 836009 $ $Date: 2009-11-13 21:49:15 +0000 (Fri, 13 Nov 2009) $
 */
public class EJBBindingProviderFactory implements BindingProviderFactory<EJBBinding> {

    public EJBBindingProviderFactory(ExtensionPointRegistry extensionPoints) {
    	// empty constructor
    }
    
    public ReferenceBindingProvider createReferenceBindingProvider(RuntimeEndpointReference endpointReference) {
    	return new EJBBindingReferenceBindingProvider((RuntimeComponent)endpointReference.getComponent(), 
    												  (RuntimeComponentReference)endpointReference.getReference(), 
    												  (EJBBinding)endpointReference.getBinding());
    }

    public ServiceBindingProvider createServiceBindingProvider(RuntimeEndpoint endpoint) {
    	// Service Binding not supported for EJB Binding
    	return null;
    }

    public Class<EJBBinding> getModelType() {
        return EJBBinding.class;
    }
}
