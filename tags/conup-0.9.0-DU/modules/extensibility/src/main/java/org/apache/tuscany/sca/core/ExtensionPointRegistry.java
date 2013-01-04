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

import org.apache.tuscany.sca.extensibility.ServiceDiscovery;


/**
 * The registry for the Tuscany core extension points. As the point of contact
 * for all extension artifacts this registry allows loaded extensions to find
 * all other parts of the system and register themselves appropriately.
 * 
 *
 * @version $Rev: 937291 $ $Date: 2010-04-23 14:41:24 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface ExtensionPointRegistry extends LifeCycleListener {

    /**
     * Add an extension point to the registry
     * @param extensionPoint The instance of the extension point
     *
     * @throws IllegalArgumentException if extensionPoint is null
     */
    void addExtensionPoint(Object extensionPoint);

    /**
     * Get the extension point by the interface
     * @param extensionPointType The lookup key (extension point interface)
     * @return The instance of the extension point
     *
     * @throws IllegalArgumentException if extensionPointType is null
     */
    <T> T getExtensionPoint(Class<T> extensionPointType);

    /**
     * Remove an extension point
     * @param extensionPoint The extension point to remove
     *
     * @throws IllegalArgumentException if extensionPoint is null
     */
    void removeExtensionPoint(Object extensionPoint);
    
    /**
     * Get an instance of the ServiceDiscovery
     * @return an instance of the ServiceDiscovery associated with the environment
     */
    ServiceDiscovery getServiceDiscovery();
}
