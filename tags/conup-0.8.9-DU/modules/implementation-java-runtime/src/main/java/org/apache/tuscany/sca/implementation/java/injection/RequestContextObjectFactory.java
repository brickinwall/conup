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
package org.apache.tuscany.sca.implementation.java.injection;

import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.oasisopen.sca.RequestContext;

/**
 * Creates instances of RequestContext for injection on component implementation instances
 * 
 * @version $Rev: 738490 $ $Date: 2009-01-28 14:07:54 +0000 (Wed, 28 Jan 2009) $
 */
public class RequestContextObjectFactory implements ObjectFactory<RequestContext> {
    private RequestContextFactory factory;
    private RuntimeComponent component;

    public RequestContextObjectFactory(RequestContextFactory factory, RuntimeComponent component) {
        this.factory = factory;
        this.component = component;
    }

    public RequestContext getInstance() throws ObjectCreationException {
        return factory.createRequestContext(component);
    }
}
