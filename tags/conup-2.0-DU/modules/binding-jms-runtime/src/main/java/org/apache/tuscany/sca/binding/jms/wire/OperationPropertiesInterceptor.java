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
package org.apache.tuscany.sca.binding.jms.wire;

import java.util.List;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.core.invocation.InterceptorAsyncImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

public class OperationPropertiesInterceptor extends InterceptorAsyncImpl {
    private Invoker next;
    private JMSBinding jmsBinding;
    private RuntimeComponentService service;
    private List<Operation> serviceOperations;
          
    public OperationPropertiesInterceptor(JMSBinding jmsBinding, RuntimeEndpoint endpoint) {
        super();
        this.jmsBinding = jmsBinding;
        this.service = (RuntimeComponentService) endpoint.getService();
        this.serviceOperations = service.getInterfaceContract().getInterface().getOperations();
    }

    public Invoker getNext() {
        return next;
    }

    public void setNext(Invoker next) {
        this.next = next;
    }

    public Message invoke(Message msg) {
        return next.invoke(invokeRequest(msg));
    }

    public Message invokeRequest(Message msg) {           
        //  TODO - could probably optimize this better 
        String operationName = msg.getOperation().getName();
        String operationNameOverride = jmsBinding.getOpNameFromNativeOperationName(operationName);
        
        if (operationNameOverride != null) {
            for (Operation op : serviceOperations) {
                if (op.getName().equals(operationNameOverride)) {
                    msg.setOperation(op);
                    break;
                }
            }
        }
        return msg;
    }
    
	public Message processRequest(Message msg) {
		return invokeRequest(msg);
	} // end method processRequest

	public Message processResponse(Message msg) {
		return msg;
	} // end method processResponse
}
