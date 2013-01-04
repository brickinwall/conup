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

package org.apache.tuscany.sca.binding.rest.operationselector.rpc.provider;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.OperationSelectorProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * RPC operation selector Reference Provider.
 * 
 * @version $Rev: 943376 $ $Date: 2010-05-12 06:29:01 +0100 (Wed, 12 May 2010) $
*/
public class RPCOperationSelectorReferenceProvider implements OperationSelectorProvider {
    private ExtensionPointRegistry extensionPoints;
    private RuntimeEndpointReference endpointReference;

    public RPCOperationSelectorReferenceProvider(ExtensionPointRegistry extensionPoints, RuntimeEndpointReference endpointReference ) {
        this.extensionPoints = extensionPoints;
        this.endpointReference = endpointReference;
    }
    
    public Interceptor createInterceptor() {
        return null;
    }

    public String getPhase() {
        return Phase.SERVICE_BINDING_OPERATION_SELECTOR;
    }

}
