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

import org.apache.tuscany.sca.runtime.RuntimeComponent;


/**
 * Manages {@link ScopeContainer}s in the runtime
 *
 * @version $Rev: 644844 $ $Date: 2008-04-04 20:32:38 +0100 (Fri, 04 Apr 2008) $
 */
public interface ScopeRegistry {

    /**
     * Returns the scope container for the given scope or null if one not found
     *
     * @param scope the scope
     * @return the scope container for the given scope or null if one not found
     */
    ScopeContainer getScopeContainer(RuntimeComponent component);

    /**
     * @param factory
     */
    void register(ScopeContainerFactory factory);
}
