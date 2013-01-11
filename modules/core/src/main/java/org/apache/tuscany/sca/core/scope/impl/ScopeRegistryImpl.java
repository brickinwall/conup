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
package org.apache.tuscany.sca.core.scope.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.ScopeContainer;
import org.apache.tuscany.sca.core.scope.ScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.core.scope.ScopedImplementationProvider;
import org.apache.tuscany.sca.core.scope.ScopedRuntimeComponent;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * The default implementation of a scope registry
 * 
 * @version $Rev: 967109 $ $Date: 2010-07-23 22:30:46 +0800 (周五, 23 七月 2010) $
 */
public class ScopeRegistryImpl implements ScopeRegistry {
    private final Map<Scope, ScopeContainerFactory> scopeCache = new ConcurrentHashMap<Scope, ScopeContainerFactory>();

    public void register(ScopeContainerFactory factory) {
        scopeCache.put(factory.getScope(), factory);
    }

    public ScopeContainer getScopeContainer(RuntimeComponent runtimeComponent) {
        if (!(runtimeComponent instanceof ScopedRuntimeComponent)) {
            return null;
        }
        ScopedRuntimeComponent component = (ScopedRuntimeComponent)runtimeComponent;
        if (component.getScopeContainer() != null) {
            return component.getScopeContainer();
        }
        ImplementationProvider implementationProvider = component.getImplementationProvider();
        if (implementationProvider instanceof ScopedImplementationProvider) {
            ScopedImplementationProvider provider = (ScopedImplementationProvider)implementationProvider;
            Scope scope = provider.getScope();
            if (scope == null) {
                scope = Scope.STATELESS;
            } else if (scope.equals(Scope.INVALID)) {
            	return null;
            }
            ScopeContainerFactory factory = scopeCache.get(scope);
            ScopeContainer container = factory.createScopeContainer(component);
            component.setScopeContainer(container);
            return container;
        }
        return null;
    }

}
