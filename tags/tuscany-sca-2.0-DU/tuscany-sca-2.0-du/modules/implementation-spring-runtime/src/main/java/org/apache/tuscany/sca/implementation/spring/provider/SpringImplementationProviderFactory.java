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
package org.apache.tuscany.sca.implementation.spring.provider;

import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.invocation.ExtensibleProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.implementation.spring.SpringImplementation;
import org.apache.tuscany.sca.implementation.spring.context.SpringApplicationContextAccessor;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * ImplementationProviderFactory for Spring implementation type
 * @version $Rev: 1083940 $ $Date: 2011-03-21 20:28:30 +0000 (Mon, 21 Mar 2011) $ 
 *
 */
public class SpringImplementationProviderFactory implements ImplementationProviderFactory<SpringImplementation> {
    private ProxyFactory proxyFactory;
    private PropertyValueFactory propertyFactory;
    private SpringApplicationContextAccessor contextAccessor;

    /**
     * Simple constructor
     *
     */
    public SpringImplementationProviderFactory(ExtensionPointRegistry registry) {
        super();
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        contextAccessor = utilities.getUtility(SpringApplicationContextAccessor.class);
        proxyFactory = ExtensibleProxyFactory.getInstance(registry);
        propertyFactory = utilities.getUtility(PropertyValueFactory.class);
    }

    /**
     * Returns a SpringImplementationProvider for a given component and Spring implementation
     * @param component the component for which implementation instances are required
     * @param implementation the Spring implementation with details of the component
     * implementation
     * @return the SpringImplementationProvider for the specified component
     */
    public ImplementationProvider createImplementationProvider(RuntimeComponent component,
                                                               SpringImplementation implementation) {

        SpringImplementationWrapper tie =
            new SpringImplementationWrapper(implementation, component, propertyFactory);
        return new SpringImplementationProvider(component, tie, proxyFactory, propertyFactory, contextAccessor);
    }

    /**
     * Returns the class of the Spring implementation
     */
    public Class<SpringImplementation> getModelType() {
        return SpringImplementation.class;
    }

} // end class SpringImplementationProviderFactory
