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

package org.apache.tuscany.sca.context;

/**
 * An extension point for context factories. 
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface ContextFactoryExtensionPoint {
    
    /**
     * Add a context factory extension.
     * 
     * @param factory The factory to add
     */
    void addFactory(Object factory);
    
    /**
     * Remove a context factory extension.
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
