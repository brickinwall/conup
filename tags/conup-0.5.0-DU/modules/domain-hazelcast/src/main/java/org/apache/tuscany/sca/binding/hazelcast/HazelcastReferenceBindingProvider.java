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

package org.apache.tuscany.sca.binding.hazelcast;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;

public class HazelcastReferenceBindingProvider implements ReferenceBindingProvider {

    private ExtensionPointRegistry extensionsRegistry;
    private String serviceURI;
    private InterfaceContract interfaceContract;

    public HazelcastReferenceBindingProvider(ExtensionPointRegistry extensionsRegistry, String serviceURI, InterfaceContract interfaceContract) {
        this.extensionsRegistry = extensionsRegistry;
        this.serviceURI = serviceURI;
        this.interfaceContract = interfaceContract;
    }

    public Invoker createInvoker(Operation operation) {
        return new ReferenceInvoker(extensionsRegistry, serviceURI, operation);
    }

    public InterfaceContract getBindingInterfaceContract() {
        return interfaceContract;
    }

    public boolean supportsOneWayInvocation() {
        return false;
    }

    public void start() {
    }

    public void stop() {
    }

}
