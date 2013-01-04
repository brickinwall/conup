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

package org.apache.tuscany.sca.provider;

import java.util.Collection;
import java.util.List;


/**
 * An extension point for provider factories. Holds all of the provider
 * factories from loaded extension points. Allows a provider factory
 * to be located based on a given model type. Hence the runtime can 
 * generate runtime artifacts from the in memory assembly model. 
 *
 * @version $Rev: 938098 $ $Date: 2010-04-26 16:42:28 +0100 (Mon, 26 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface ProviderFactoryExtensionPoint {


    /**
     * Add a provider factory.
     * 
     * @param providerFactory The provider factory
     */
    void addProviderFactory(ProviderFactory providerFactory);

    /**
     * Remove a provider factory.
     * 
     * @param providerFactory The provider factory
     */
    void removeProviderFactory(ProviderFactory providerFactory);

    /**
     * Returns the provider factory associated with the given model type.
     * @param modelType A model type
     * @return The provider factory associated with the given model type
     */
    ProviderFactory getProviderFactory(Class<?> modelType);
    
    /**
     * Get a list of registered PolicyProviderFactory
     * @return a list of registered PolicyProviderFactory
     */
    List<PolicyProviderFactory> getPolicyProviderFactories();
    
    /**
     * Get a collection of provider factories by the factory type 
     * @param <P>
     * @param factoryType The factory type such as BindingProviderFactory
     * @return a collection of provider factories of the factory type
     */
    <P extends ProviderFactory> Collection<P> getProviderFactories(Class<P> factoryType);

    
}
