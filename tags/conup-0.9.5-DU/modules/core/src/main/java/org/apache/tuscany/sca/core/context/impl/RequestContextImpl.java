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
package org.apache.tuscany.sca.core.context.impl;

import java.util.List;

import javax.security.auth.Subject;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.context.ThreadMessageContext;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;

/**
 * @version $Rev: 1298513 $ $Date: 2012-03-09 02:59:27 +0800 (周五, 09 三月 2012) $
 */
public class RequestContextImpl implements RequestContext {

    public RequestContextImpl(RuntimeComponent component) {
    }

    public Subject getSecuritySubject() {
        
        Message msgContext = ThreadMessageContext.getMessageContext();
        
        if (msgContext == null){
            // message in thread context could be null if the user has 
            // spun up a new thread inside their component implementation 
            return null;
        }
        
        Subject subject = null;
        for (Object header : msgContext.getHeaders().values()){
            if (header instanceof Subject){
                subject  = (Subject)header;
                break;
            }
        }
        return subject;
    }

    public String getServiceName() {
        Message msgContext = ThreadMessageContext.getMessageContext();
        
        if (msgContext != null &&
            msgContext.getTo() != null){
            return msgContext.getTo().getService().getName();
        } else {
            // message in thread context could be null (or the default message where to == null) 
            // if the user has spun up a new thread inside their component implementation 
            return null;
        }
    }

    public <B> ServiceReference<B> getServiceReference() {
        Message msgContext = ThreadMessageContext.getMessageContext();
        if (msgContext == null || 
            msgContext.getTo() == null){
            // message in thread context could be null (or the default message where to == null)
            // if the user has spun up a new thread inside their component implementation 
            return null;
        }
        // FIXME: [rfeng] Is this the service reference matching the caller side?
        RuntimeEndpoint to = (RuntimeEndpoint) msgContext.getTo();
        RuntimeComponent component = (RuntimeComponent) to.getComponent();
        
        ServiceReference<B> callableReference = component.getComponentContext().getServiceReference(null, to);
        
        return callableReference;
    }

    public <CB> CB getCallback() {
        ServiceReference<CB> cb = getCallbackReference(); 
        if (cb == null) {
            return null;
        }
        return cb.getService();
    }

    @SuppressWarnings("unchecked")
    public <CB> ServiceReference<CB> getCallbackReference() {
        Message msgContext = ThreadMessageContext.getMessageContext();
        if (msgContext == null || 
            msgContext.getTo() == null){
            // message in thread context could be null (or the default message where to == null)
            // if the user has spun up a new thread inside their component implementation 
            return null;
        }
        
        Endpoint to = msgContext.getTo();
        RuntimeComponentService service = (RuntimeComponentService) to.getService();
        RuntimeComponentReference callbackReference = (RuntimeComponentReference)service.getCallbackReference();
        if (callbackReference == null) {
            return null;
        }
        JavaInterface javaInterface = (JavaInterface) callbackReference.getInterfaceContract().getInterface();
        Class<CB> javaClass = (Class<CB>)javaInterface.getJavaClass();
        List<EndpointReference> wires = callbackReference.getEndpointReferences();
        ServiceReferenceImpl ref = new CallbackServiceReferenceImpl(javaClass, wires);

        return ref;
    }
}
