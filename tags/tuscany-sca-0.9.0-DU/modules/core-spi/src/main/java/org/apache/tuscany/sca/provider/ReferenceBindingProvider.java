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

package org.apache.tuscany.sca.provider;

import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;

/**
 * A reference binding implementation can options implement this
 * interface to tie into the Tuscany SCA runtime
 * 
 * @version $Rev: 938086 $ $Date: 2010-04-26 16:18:59 +0100 (Mon, 26 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface ReferenceBindingProvider extends RuntimeProvider {
    /**
     * Create an invoker for the reference binding in the invocation chain. The
     * invoker is responsible for making the outbound invocation over the
     * binding protocol.
     * 
     * @param operation The operation that the interceptor will handle
     * @return An invoker that handles the invocation logic, null should be
     *         returned if no invoker is required
     */
    Invoker createInvoker(Operation operation);

    /**
     * Get the effective interface contract imposed by the binding. For example,
     * it will be interface contract introspected from the WSDL portType used by
     * the endpoint for a WebService binding.
     * 
     * @return The effective interface contract, if null is returned, the
     *         interface contract for the component reference will be used
     */
    InterfaceContract getBindingInterfaceContract();

    /**
     * For bindings that invoke one-way operations asynchronously, there is no
     * need to perform a thread switch before calling the invoker. This method
     * indicates whether the binding has this capability.
     * 
     * @return true if the binding invoker is able to invoke one-way operations
     *         asynchronously, false if all invocations are synchronous
     */
    boolean supportsOneWayInvocation();
}
