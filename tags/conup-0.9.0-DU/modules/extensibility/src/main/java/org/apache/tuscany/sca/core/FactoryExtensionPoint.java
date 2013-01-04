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

package org.apache.tuscany.sca.core;

/**
 * An extension point for model factories. Model factories are provided to 
 * abstract the classes that represent artifacts in the assembly model away
 * from their creation mechanism. When the runtime needs to extend the model
 * as it reads in contributed artifacts it looks up the factory for the 
 * artifact required in this registry
 *
 * @version $Rev: 937291 $ $Date: 2010-04-23 14:41:24 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface FactoryExtensionPoint {
    
    /**
     * Add a model factory extension.
     * 
     * @param factory The factory to add
     */
    void addFactory(Object factory);
    
    /**
     * Remove a model factory extension.
     *  
     * @param factory The factory to remove
     */
    void removeFactory(Object factory); 
    
    /**
     * Get a factory implementing the given interface.
     * @param factoryInterface the lookup key (factory interface)
     * @return The factory
     */
    <T> T getFactory(Class<T> factoryInterface);

}
