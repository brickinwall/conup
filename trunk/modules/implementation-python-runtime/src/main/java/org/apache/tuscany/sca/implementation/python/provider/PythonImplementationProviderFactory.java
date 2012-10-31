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

package org.apache.tuscany.sca.implementation.python.provider;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.invocation.ExtensibleProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.implementation.python.PythonImplementation;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * A factory for Python implementation providers.
 * 
 * @version $Rev$ $Date$
 */
public class PythonImplementationProviderFactory implements ImplementationProviderFactory<PythonImplementation> {
    final ProxyFactory pxFactory;

    public PythonImplementationProviderFactory(final ExtensionPointRegistry ep) {
        pxFactory = ExtensibleProxyFactory.getInstance(ep);
    }

    public ImplementationProvider createImplementationProvider(final RuntimeComponent comp, final PythonImplementation impl) {
        return new PythonImplementationProvider(comp, impl, pxFactory);
    }

    public Class<PythonImplementation> getModelType() {
        return PythonImplementation.class;
    }
}
