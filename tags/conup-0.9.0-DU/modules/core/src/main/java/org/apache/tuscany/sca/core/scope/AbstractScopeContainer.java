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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tuscany.sca.core.factory.InstanceWrapper;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * Implements functionality common to scope contexts.
 * 
 * @version $Rev: 1132629 $ $Date: 2011-06-06 21:26:21 +0800 (周一, 06 六月 2011) $
 */
public abstract class AbstractScopeContainer<KEY> implements ScopeContainer<KEY> {
    protected Map<KEY, InstanceWrapper<?>> wrappers = new ConcurrentHashMap<KEY, InstanceWrapper<?>>();
    protected final Scope scope;

    protected RuntimeComponent component;
    protected volatile int lifecycleState = UNINITIALIZED;
    
    private static String scopeStateStrings[] = {"CONFIG_ERROR", 
                                                 "UNINITIALIZED",
                                                 "INITIALIZING",
                                                 "INITIALIZED",
                                                 "NOT USED",
                                                 "RUNNING",
                                                 "STOPPING",
                                                 "STOPPED",
                                                 "ERROR"};
    
    public AbstractScopeContainer(Scope scope, RuntimeComponent component) {
        this.scope = scope;
        this.component = component;
    }

    protected void checkInit() {
        if (getLifecycleState() != RUNNING) {
            throw new IllegalStateException("Scope container not running. Current state is [" + 
                                            scopeStateStrings[getLifecycleState() + 1] + 
                                            "]");
        }
    }

    /**
     * Creates a new physical instance of a component, wrapped in an
     * InstanceWrapper.
     * 
     * @param component the component whose instance should be created
     * @return a wrapped instance that has been injected but not yet started
     * @throws TargetResolutionException if there was a problem creating the
     *             instance
     */
    protected InstanceWrapper createInstanceWrapper() throws TargetResolutionException {
        ImplementationProvider implementationProvider = component.getImplementationProvider();
        if (implementationProvider instanceof ScopedImplementationProvider) {
            return ((ScopedImplementationProvider)implementationProvider).createInstanceWrapper();
        }
        return null;
    }

    public InstanceWrapper getAssociatedWrapper(KEY contextId) throws TargetResolutionException {
        return getWrapper(contextId); // TODO: what is this method supposed to do diff than getWrapper? 
    }

    public Scope getScope() {
        return scope;
    }

    public InstanceWrapper getWrapper(KEY contextId) throws TargetResolutionException {
        return wrappers.get(contextId);
    }
    
    public void addWrapperReference(KEY existingContextId, KEY newContextId) 
      throws TargetResolutionException {
        // do nothing here. the conversational scope container implements this
    }

    public void registerWrapper(InstanceWrapper wrapper, KEY contextId) throws TargetResolutionException { 
        // do nothing here. the conversational scope container implements this
    }

    protected boolean isEagerInit() {
        ImplementationProvider implementationProvider = ((RuntimeComponent)component).getImplementationProvider();
        if (implementationProvider instanceof ScopedImplementationProvider) {
            return ((ScopedImplementationProvider)implementationProvider).isEagerInit();
        }
        return false;
    }

    public void returnWrapper(InstanceWrapper wrapper, KEY contextId) throws TargetDestructionException {
    }
    
    /**
     * Default implementation of remove which does nothing 
     * 
     * @param contextId the identifier of the context to remove. 
     */
    public void remove(KEY contextId) 
        throws TargetDestructionException {
    }    

    public synchronized void start() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != UNINITIALIZED && lifecycleState != STOPPED) {
            throw new IllegalStateException("Scope must be in UNINITIALIZED or STOPPED state but the current state is [" + 
                                            scopeStateStrings[lifecycleState + 1] + 
                                            "]. Did you try to start the same node twice?");
        }
        setLifecycleState(RUNNING);
    }

    public void startContext(KEY contextId) {
        if(isEagerInit()) {
            try {
                getWrapper(contextId);
            } catch (TargetResolutionException e) {
                // 
            }
        }
    }

    public synchronized void stop() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != RUNNING) {
            throw new IllegalStateException("Scope in wrong state. Current state is [" + 
                                            scopeStateStrings[lifecycleState + 1] + 
                                            "]");
        }
        setLifecycleState(STOPPED);
    }

    public void stopContext(KEY contextId) {
        wrappers.remove(contextId);
    }

    @Override
    public String toString() {
        return "In state [" + scopeStateStrings[lifecycleState + 1] + ']';
    }

    public RuntimeComponent getComponent() {
        return component;
    }

    public void setComponent(RuntimeComponent component) {
        this.component = component;
    }

    public int getLifecycleState() {
        return lifecycleState;
    }

    /**
     * Set the current state of the Lifecycle.
     *
     * @param lifecycleState the new state
     */
    protected void setLifecycleState(int lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

}
