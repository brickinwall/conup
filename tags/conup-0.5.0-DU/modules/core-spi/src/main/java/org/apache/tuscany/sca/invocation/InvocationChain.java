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
package org.apache.tuscany.sca.invocation;

import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * A wire consists of 1..n invocation chains associated with the operations of its source service contract.
 * <p/>
 * Invocation chains may contain <code>Interceptors</code> that process invocations.
 * <p/>
 * A <code>Message</code> is used to pass data associated with an invocation through the chain.
 *
 * @version $Rev: 1057650 $ $Date: 2011-01-11 14:15:07 +0000 (Tue, 11 Jan 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface InvocationChain {
    /**
     * Returns the target operation for this invocation chain.
     *
     * @return The target operation for this invocation chain
     */
    Operation getTargetOperation();

    /**
     * Updates the target operation for this invocation chain.
     *
     * @param operation The new target operation for this invocation chain
     */
    void setTargetOperation(Operation operation);
    
    /**
     * Returns the source operation for this invocation chain.
     *
     * @return The source operation for this invocation chain
     */    
    Operation getSourceOperation();

    /**
     * Updates the source operation for this invocation chain.
     *
     * @param operation The new source operation for this invocation chain
     */
    void setSourceOperation(Operation operation);

    /**
     * Adds an interceptor to the end of the chain. For reference side, it will be added to
     * Phase.REFERENCE. For service side, it will be added to Phase.SERVICE 
     *
     * @param interceptor The interceptor to add
     */
    void addInterceptor(Interceptor interceptor);
    
    /**
     * Add an interceptor to the end of the given phase
     * @param phase - the phase
     * @param interceptor - the interceptor
     */
    void addInterceptor(String phase, Interceptor interceptor);
    
    /**
     * Adds an interceptor to the head of the chain
     * @param interceptor - the interceptor
     */
    void addHeadInterceptor(Interceptor interceptor);
    
    /**
     * Adds an interceptor to the head of the given phase
     * @param phase - the phase
     * @param interceptor - the interceptor
     */
    void addHeadInterceptor(String phase, Interceptor interceptor);

    /**
     * Adds an invoker to the end of the chain
     *
     * @param invoker The invoker to add
     */
    void addInvoker(Invoker invoker);

    /**
     * Returns the first invoker in the chain.
     *
     * @return The first invoker in the chain
     */
    Invoker getHeadInvoker();
    
    /**
     * Returns the last invoker in the chain.
     *
     * @return The last invoker in the chain
     */
    Invoker getTailInvoker();    
    
    /**
     * Get the first invoker that is on the same or later phase 
     * @param phase
     * @return The first invoker that is on the same or later phase
     */
    Invoker getHeadInvoker(String phase);

    /**
     * Indicate if the data can be passed in by reference as they won't be mutated.
     * @return true if pass-by-reference is allowed
     */
    boolean allowsPassByReference();
    /**
     * Force the invocation to allow pass-by-reference
     * @param allowsPBR
     */
    void setAllowsPassByReference(boolean allowsPBR);
    
    /** 
     * Returns true if this chain must be able to support async 
     * invocation. This will be as a consequence of the EPR/EP 
     * detecting the asyncInvocation intent. The flag is set on
     * construction and used as an internal guard against non
     * async interceptors being added to a chain that expect to 
     * be able to handle async calls
     * 
     * @return true is the chain supports async invocation.
     */
    boolean isAsyncInvocation();
}
