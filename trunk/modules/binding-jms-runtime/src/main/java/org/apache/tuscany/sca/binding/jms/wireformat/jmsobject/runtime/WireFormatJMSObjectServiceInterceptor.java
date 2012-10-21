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
package org.apache.tuscany.sca.binding.jms.wireformat.jmsobject.runtime;

import java.util.HashMap;

import javax.jms.Session;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.context.JMSBindingContext;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessor;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessorUtil;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.binding.jms.provider.ObjectMessageProcessor;
import org.apache.tuscany.sca.binding.jms.wireformat.WireFormatJMSObject;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.invocation.InterceptorAsyncImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

/**
 * Policy handler to handle PolicySet related to Logging with the QName
 * {http://tuscany.apache.org/xmlns/sca/1.1/impl/java}LoggingPolicy
 *
 * @version $Rev: 1159761 $ $Date: 2011-08-19 20:55:46 +0100 (Fri, 19 Aug 2011) $
 */
public class WireFormatJMSObjectServiceInterceptor extends InterceptorAsyncImpl {
    private Invoker next;
    private RuntimeEndpoint endpoint;
    private JMSResourceFactory jmsResourceFactory;
    private JMSBinding jmsBinding;
    private JMSMessageProcessor requestMessageProcessor;
    private JMSMessageProcessor responseMessageProcessor;
    private HashMap<String,Class<?>> singleArgMap;
    private boolean wrapSingle;

    public WireFormatJMSObjectServiceInterceptor(ExtensionPointRegistry registry, JMSResourceFactory jmsResourceFactory, 
            RuntimeEndpoint endpoint, HashMap<String, Class<?>> singleArgMap, boolean wrapSingle) {
        super();
        this.jmsBinding = (JMSBinding) endpoint.getBinding();
        this.endpoint = endpoint;
        this.jmsResourceFactory = jmsResourceFactory;
        this.requestMessageProcessor = JMSMessageProcessorUtil.getRequestMessageProcessor(registry, jmsBinding);
        this.responseMessageProcessor = JMSMessageProcessorUtil.getResponseMessageProcessor(registry, jmsBinding);
        this.singleArgMap = singleArgMap;
        this.wrapSingle = wrapSingle;
    }

    public Message invoke(Message msg) {
        if (jmsBinding.getRequestWireFormat() instanceof WireFormatJMSObject){
            msg = invokeRequest(msg);
        }
        
        msg = getNext().invoke(msg);
        
        //if it's oneway return back
        Operation operation = msg.getOperation();
        if (operation != null && operation.isNonBlocking()) {
            return msg;
        }

        if (jmsBinding.getResponseWireFormat() instanceof WireFormatJMSObject){
            msg = invokeResponse(msg);
        }
        
        return msg;
    }
    
    public Message invokeRequest(Message msg) {
        // get the jms context
        JMSBindingContext context = msg.getBindingContext();
        javax.jms.Message jmsMsg = context.getJmsMsg();
        Object requestPayload;
        
        // If the service interface has a single argument then we need 
        // to check if the object from the wire is expected  
        // to be unwrapped or not
        // 
        Class<?> argType = this.singleArgMap.get(msg.getOperation().getName());
        if (argType == null) {
            requestPayload = requestMessageProcessor.extractPayloadFromJMSMessage(jmsMsg);    
        }else {
            requestPayload = ((ObjectMessageProcessor)requestMessageProcessor).extractPayloadFromJMSMessageForSingleParamOperation(jmsMsg, argType, wrapSingle);
        }
        
        if (requestPayload != null && requestPayload.getClass().isArray()) {
            msg.setBody(requestPayload);
        } else if (requestPayload == null) {
            msg.setBody(new Object[0]);
        } else {
            msg.setBody(new Object[] {requestPayload});
        }
          
        return msg;
    }
    
    public Message invokeResponse(Message msg) {
        // get the jms context
        JMSBindingContext context = msg.getBindingContext();
        
        // The Binding Context may be null on an asynchronous response - in which case, create a new one
        if(context == null) {
            context = createBindingContext();
            msg.setBindingContext(context);
        }

        Session session = context.getJmsResponseSession();

        javax.jms.Message responseJMSMsg = null;
        if (msg.isFault()) {
            responseJMSMsg = responseMessageProcessor.createFaultMessage(session, (Throwable)msg.getBody());
        } else {
            responseJMSMsg = responseMessageProcessor.insertPayloadIntoJMSMessage(session, msg.getBody());
        } 
    
        msg.setBody(responseJMSMsg);
        
        return msg;
    }        

    public Invoker getNext() {
        return next;
    }

    public void setNext(Invoker next) {
        this.next = next;
    }
    
    private JMSBindingContext createBindingContext() {
        JMSBindingContext context = new JMSBindingContext();
        context.setJmsResourceFactory(jmsResourceFactory);
        return context;
    }

    public Message processRequest(Message msg) {
		return invokeRequest(msg);
	} // end method processRequest

	public Message processResponse(Message msg) {
		return invokeResponse(msg);
	} // end method processResponse

}
