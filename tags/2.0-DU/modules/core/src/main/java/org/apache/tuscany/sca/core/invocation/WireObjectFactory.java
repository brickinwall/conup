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
package org.apache.tuscany.sca.core.invocation;

import org.apache.tuscany.sca.core.context.impl.ServiceReferenceImpl;
import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.core.invocation.impl.NoMethodForOperationException;
import org.apache.tuscany.sca.runtime.Invocable;

/**
 * Uses a wire to return an object instance
 * 
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class WireObjectFactory<T> implements ObjectFactory<T> {
    private Class<T> interfaze;
    private Invocable wire;
    private ProxyFactory proxyService;
    
    /**
     * Constructor.
     * 
     * @param interfaze the interface to inject on the client
     * @param wire the backing wire
     * @param proxyService the wire service to create the proxy
     * @throws NoMethodForOperationException
     */
    public WireObjectFactory(Class<T> interfaze, Invocable wire, ProxyFactory proxyService) {
        this.interfaze = interfaze;
        this.wire = wire;
        this.proxyService = proxyService;
    }

    public T getInstance() throws ObjectCreationException {
        return new ServiceReferenceImpl<T>(interfaze, wire, null).getProxy();
    }

}
