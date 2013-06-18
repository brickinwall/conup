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

import org.apache.tuscany.sca.core.factory.InstanceWrapper;
import org.apache.tuscany.sca.core.scope.AbstractScopeContainer;
import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.TargetDestructionException;
import org.apache.tuscany.sca.core.scope.TargetResolutionException;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * A scope context which manages stateless atomic component instances in a non-pooled fashion.
 *
 * @version $Rev: 967109 $ $Date: 2010-07-23 22:30:46 +0800 (周五, 23 七月 2010) $
 */
public class StatelessScopeContainer<KEY> extends AbstractScopeContainer<KEY> {

    public StatelessScopeContainer(RuntimeComponent component) {
        super(Scope.STATELESS, component);
    }

    @Override
    public  InstanceWrapper getWrapper(KEY contextId) throws TargetResolutionException {
        if (lifecycleState != RUNNING) {
            throw new TargetResolutionException("scope container not running, lifecycleState=" + lifecycleState);
        }
        InstanceWrapper ctx = createInstanceWrapper();
        ctx.start();
        return ctx;
    }

    @Override
    public  InstanceWrapper getAssociatedWrapper(KEY contextId)
        throws TargetResolutionException {
        return getWrapper(contextId);
    }

    @Override
    public  void returnWrapper(InstanceWrapper wrapper, KEY contextId)
        throws TargetDestructionException {
        wrapper.stop();
    }
    
}
