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
 * The extension point for the Tuscany core utility extensions.
 *
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface UtilityExtensionPoint extends LifeCycleListener {

    /**
     * Add a utility to the extension point
     * @param utility The instance of the utility
     *
     * @throws IllegalArgumentException if utility is null
     */
    void addUtility(Object utility);
    
    /**
     * Add a utility to the extension point for a given key
     * @param utility The instance of the utility
     *
     * @throws IllegalArgumentException if utility is null
     */
    void addUtility(Object key, Object utility);

    /**
     * Get the utility by the interface
     * @param utilityType The lookup key (utility interface)
     * @return The instance of the utility
     *
     * @throws IllegalArgumentException if utilityType is null
     */
    <T> T getUtility(Class<T> utilityType);

    /**
     * Get an instance of the utility by the interface and key
     * @param utilityType The lookup key (utility interface)
     * @param key A key associated with the utility, if it is null,
     * then the utilityType is used as the key
     * @return The instance of the utility
     *
     * @throws IllegalArgumentException if utilityType is null
     */
    <T> T getUtility(Class<T> utilityType, Object key);

    /**
     * Remove a utility
     * @param utility The utility to remove
     *
     * @throws IllegalArgumentException if utility is null
     */
    void removeUtility(Object utility);
}
