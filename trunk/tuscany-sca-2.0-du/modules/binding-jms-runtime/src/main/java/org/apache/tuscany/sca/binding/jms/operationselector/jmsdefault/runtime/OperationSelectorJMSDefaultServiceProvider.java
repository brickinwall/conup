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

package org.apache.tuscany.sca.binding.jms.operationselector.jmsdefault.runtime;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.OperationSelectorProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

/**
 * @version $Rev: 836009 $ $Date: 2009-11-13 21:49:15 +0000 (Fri, 13 Nov 2009) $
 */
public class OperationSelectorJMSDefaultServiceProvider implements OperationSelectorProvider {
    private RuntimeEndpoint endpoint;
    private ExtensionPointRegistry registry;

    public OperationSelectorJMSDefaultServiceProvider(ExtensionPointRegistry registry, RuntimeEndpoint endpoint) {
        super();
        this.endpoint = endpoint;
        this.registry = registry;
    }

    /**
     */
    public Interceptor createInterceptor() {
        return new OperationSelectorJMSDefaultServiceInterceptor(registry, null, endpoint);
    }

    /**
     */
    public String getPhase() {
        return Phase.SERVICE_BINDING_OPERATION_SELECTOR;
    }

}
