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
package org.apache.tuscany.sca.core.factory;

import org.apache.tuscany.sca.core.scope.TargetDestructionException;
import org.apache.tuscany.sca.core.scope.TargetInitializationException;


/**
 * Provides lifecycle management for an implementation instance associated with
 * a component for use by the component's associated {@link org.apache.tuscany.sca.core.scope.ScopeContainer}
 * 
 * @version $Rev: 967109 $ $Date: 2010-07-23 22:30:46 +0800 (周五, 23 七月 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface InstanceWrapper<T> {

    /**
     * @return
     */
    T getInstance();

    /**
     * @throws TargetInitializationException
     */
    void start() throws TargetInitializationException;

    /**
     * @throws TargetDestructionException
     */
    void stop() throws TargetDestructionException;

}
