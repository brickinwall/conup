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
package org.apache.tuscany.sca.binding.jms.headers;

import java.util.Map;

import javax.jms.JMSException;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessor;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessorUtil;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.invocation.InterceptorAsyncImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;

public class HeaderServiceInterceptor extends InterceptorAsyncImpl {

    private Invoker next;
    private JMSBinding jmsBinding;
    private JMSMessageProcessor responseMessageProcessor;

    public HeaderServiceInterceptor(ExtensionPointRegistry extensions, JMSBinding jmsBinding) {
        super();
        this.jmsBinding = jmsBinding;
        this.responseMessageProcessor = JMSMessageProcessorUtil.getResponseMessageProcessor(extensions, jmsBinding);
    }

    public Message invoke(Message msg) {
    	msg = invokeRequest( msg );
        return invokeResponse(next.invoke(msg));
    }
    
    public Message invokeRequest(Message tuscanyMsg) {
    	
    	try {
    	
	    	javax.jms.Message jmsMsg = tuscanyMsg.getBody();
	        
	        // Handle MESSAGE_ID field of the JMS message, which is used to correlate async callbacks
	        String msgID = (String)jmsMsg.getObjectProperty("MESSAGE_ID");
	        if( msgID != null ) {
	        	tuscanyMsg.getHeaders().put("MESSAGE_ID", msgID);
	        } // end if 
	        //
        
		} catch (JMSException e) {
		    throw new JMSBindingException(e);
		} // end try
        
        return tuscanyMsg;
    } // end method invokeRequest
    
    public Message invokeResponse(Message tuscanyMsg) {
        try {

            javax.jms.Message jmsMsg = tuscanyMsg.getBody();
            
            Operation operation = tuscanyMsg.getOperation();
            String operationName = operation.getName();
            
            responseMessageProcessor.setOperationName(operationName, jmsMsg);

            for (String propName : jmsBinding.getPropertyNames()) {
                Object value = jmsBinding.getProperty(propName);
                jmsMsg.setObjectProperty(propName, value);
            } // end for
    
            Map<String, Object> operationProperties = jmsBinding.getOperationProperties(operationName);
            if (operationProperties != null) {
                for (String propName : operationProperties.keySet()) {
                    Object value = operationProperties.get(propName);
                    jmsMsg.setObjectProperty(propName, value);
                } // end for
            } // end if
            
            // Put a "RELATES_TO" property into the response message
            String relatesTo = (String)tuscanyMsg.getHeaders().get("RELATES_TO");
            if( relatesTo != null ) {
            	jmsMsg.setStringProperty("RELATES_TO", relatesTo);
            } // end if
            
            return tuscanyMsg;

        } catch (JMSException e) {
            throw new JMSBindingException(e);
        } // end try 
    } // end method invokeResponse

    public Invoker getNext() {
        return next;
    } // end method getNext

    public void setNext(Invoker next) {
        this.next = next;
    }

	public Message processRequest(Message msg) {
		return invokeRequest(msg);
	} // end method processRequest

	public Message processResponse(Message msg) {
		return invokeResponse(msg);
	} // end method processResponse

} // end class
