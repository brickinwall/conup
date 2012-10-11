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
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.implementation.spring.context.SpringApplicationContextAccessor;
import org.apache.tuscany.sca.implementation.spring.context.SpringContextWrapper;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.springframework.context.ApplicationContext;

/**
 * A provider class for runtime Spring implementation instances
 * @version $Rev: 1083940 $ $Date: 2011-03-21 20:28:30 +0000 (Mon, 21 Mar 2011) $ 
 */
public class SpringImplementationProvider implements ImplementationProvider {
    private RuntimeComponent component;
    private SpringImplementationWrapper implementation;
    private SpringApplicationContextAccessor contextAccessor;

    // A Spring application context object
    private SpringContextWrapper springContext;

    /**
     * Constructor for the provider - takes a component definition and a Spring implementation
     * description
     * @param component - the component in the assembly
     * @param implementation - the implementation
     */
    public SpringImplementationProvider(RuntimeComponent component,
                                        SpringImplementationWrapper implementation,
                                        ProxyFactory proxyService,
                                        PropertyValueFactory propertyValueObjectFactory,
                                        SpringApplicationContextAccessor contextAccessor) {
        super();
        this.component = component;
        this.implementation = implementation;
        this.contextAccessor = contextAccessor;

    } // end constructor

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        return new SpringInvoker(component, springContext, service, operation);
    }

    public boolean supportsOneWayInvocation() {
        return false;
    }

    /**
     * Start this Spring implementation instance
     */
    public void start() {
        ApplicationContext parentApplicationContext =
            (contextAccessor == null) ? null : contextAccessor.getParentApplicationContext(component);
        implementation.setParentApplicationContext(parentApplicationContext);
        springContext = new SpringContextWrapper(implementation, implementation.getResource());
        springContext.start();
    }

    /**
     * Stop this implementation instance
     */
    public void stop() {
        springContext.close();
    }

} // end class SpringImplementationProvider
