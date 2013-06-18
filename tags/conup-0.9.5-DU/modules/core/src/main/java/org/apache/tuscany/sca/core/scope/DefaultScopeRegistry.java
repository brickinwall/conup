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

import org.apache.tuscany.sca.core.scope.impl.CompositeScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.impl.ScopeRegistryImpl;
import org.apache.tuscany.sca.core.scope.impl.StatelessScopeContainerFactory;

/**
 * A default scope registry implementation.
 *
 * @version $Rev: 819068 $ $Date: 2009-09-26 07:37:29 +0800 (周六, 26 九月 2009) $
 */
public class DefaultScopeRegistry extends ScopeRegistryImpl implements ScopeRegistry {

    public DefaultScopeRegistry() {
        ScopeContainerFactory[] factories =
            new ScopeContainerFactory[] {new CompositeScopeContainerFactory(), new StatelessScopeContainerFactory()};
        for (ScopeContainerFactory f : factories) {
            register(f);
        }
    }
}
