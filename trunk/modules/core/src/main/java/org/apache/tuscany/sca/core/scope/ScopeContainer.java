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
package org.apache.tuscany.sca.core.scope;

import org.apache.tuscany.sca.core.factory.InstanceWrapper;
import org.apache.tuscany.sca.runtime.RuntimeComponent;


/**
 * Manages the lifecycle and visibility of instances associated with a an {@link RuntimeComponent}.
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 22:27:50 +0800 (周五, 23 四月 2010) $
 * @param <KEY> the type of IDs that this container uses to identify its contexts.
 * For example, for COMPOSITE scope this could be the URI of the composite component,
 * or for HTTP Session scope it might be the HTTP session ID.
 * 
 * @tuscany.spi.extension.asclient
 */
public interface ScopeContainer<KEY> {

    /**
     * Returns the Scope that this container supports.
     *
     * @return the Scope that this container supports
     */
    Scope getScope();

    /**
     * Start a new context with the supplied ID.
     *
     * @param contextId an ID that uniquely identifies the context.
     */
    void startContext(KEY contextId);

    /**
     * Stop the context with the supplied ID.
     *
     * @param contextId an ID that uniquely identifies the context.
     */
    void stopContext(KEY contextId);

    /**
     * Returns an instance wrapper associated with the current scope context, creating one if necessary
     * @param contextId the id for the scope context
     *
     * @return the wrapper for the target instance
     * @throws TargetResolutionException if there was a problem instantiating the target instance
     */
    InstanceWrapper getWrapper(KEY contextId) throws TargetResolutionException;
     
    /**
     * Allows a component to be registered against more than one context id. This is required in the
     * case of stateful callbacks where we want to identify the originating client component instance 
     * as the callback target but we don't want to reuse the clients original conversation id
     * 
     * @param existingContextId  an id that identifies an existing component instance
     * @param newContextId a new id against which this component will also be registered
     * @throws TargetResolutionException
     */
    void addWrapperReference(KEY existingContextId, KEY newContextId) 
        throws TargetResolutionException;   

    /**
     * Register an existing instance against a context id.  This is needed
     * for a stateful callback where the service reference for the forward call
     * contains a callback object that is not a service reference.
     * 
     * @param wrapper the instance wrapper for the instance to be registered
     * @param contextId the id for the scope context
     * @throws TargetResolutionException
     */
    void registerWrapper(InstanceWrapper wrapper, KEY contextId) 
        throws TargetResolutionException;   

    /**
     * Returns an implementation instance associated with the current scope context.
     * If no instance is found, a {@link TargetNotFoundException} is thrown.
     * @param contextId the id for the scope context
     *
     * @return the wrapper for the target instance
     * @throws TargetResolutionException if there was a problem instantiating the target instance
     */
    InstanceWrapper getAssociatedWrapper(KEY contextId)
        throws TargetResolutionException;

    /**
     * Return a wrapper after use (for example, after invoking the instance).
     * @param wrapper the wrapper for the target instance being returned
     * @param contextId the id for the scope context
     *
     * @throws TargetDestructionException if there was a problem returning the target instance
     */
    void returnWrapper(InstanceWrapper wrapper, KEY contextId)
        throws TargetDestructionException;

    /**
     * Removes an identified component implementation instance associated with the current 
     * context from persistent storage
     *
     * @param contextId the identifier of the context to remove. 
     */
    void remove(KEY contextId) 
        throws TargetDestructionException;     

    /* A configuration error state */
    int CONFIG_ERROR = -1;
    /* Has not been initialized */
    int UNINITIALIZED = 0;
    /* In the process of being configured and initialized */
    int INITIALIZING = 1;
    /* Instantiated and configured */
    int INITIALIZED = 2;
    /* Configured and initialized */
    int RUNNING = 4;
    /* In the process of being shutdown */
    int STOPPING = 5;
    /* Has been shutdown and removed from the composite */
    int STOPPED = 6;
    /* In an error state */
    int ERROR = 7;

    /**
     * Returns the lifecycle state
     *
     * @see #UNINITIALIZED
     * @see #INITIALIZING
     * @see #INITIALIZED
     * @see #RUNNING
     * @see #STOPPING
     * @see #STOPPED
     */
    int getLifecycleState();

    /**
     * Starts the Lifecycle.
     */
    void start();

    /**
     * Stops the Lifecycle.
     */
    void stop();

}
