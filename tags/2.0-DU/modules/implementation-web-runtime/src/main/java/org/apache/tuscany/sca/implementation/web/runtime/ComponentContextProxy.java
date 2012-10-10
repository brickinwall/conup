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

package org.apache.tuscany.sca.implementation.web.runtime;

import java.util.Collection;

import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.TuscanyComponentContext;
import org.apache.tuscany.sca.runtime.TuscanyServiceReference;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;

/**
 * Proxy ComponentContext wrappering a RuntimeComponent as the
 * RuntimeComponent ComponentContext has not been created till later 
 */
public class ComponentContextProxy implements TuscanyComponentContext {

    protected RuntimeComponent runtimeComponent;
    
    public ComponentContextProxy(RuntimeComponent runtimeComponent) {
        this.runtimeComponent = runtimeComponent;
    }
    
    protected TuscanyComponentContext getComponentContext() {
        return runtimeComponent.getComponentContext();
    }

    public <B> ServiceReference<B> cast(B target) throws IllegalArgumentException {
        return getComponentContext().cast(target);
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return getComponentContext().createSelfReference(businessInterface);
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return getComponentContext().createSelfReference(businessInterface, serviceName);
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        return getComponentContext().getProperty(type, propertyName);
    }

    public RequestContext getRequestContext() {
        return getComponentContext().getRequestContext();
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        return getComponentContext().getService(businessInterface, referenceName);
    }

    public <B> TuscanyServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        return getComponentContext().getServiceReference(businessInterface, referenceName);
    }

    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> businessInterface, String referenceName) {
        return getComponentContext().getServiceReferences(businessInterface, referenceName);
    }

    public <B> Collection<B> getServices(Class<B> businessInterface, String referenceName) {
        return getComponentContext().getServices(businessInterface, referenceName);
    }

    public String getURI() {
        return getComponentContext().getURI();
    }

}
