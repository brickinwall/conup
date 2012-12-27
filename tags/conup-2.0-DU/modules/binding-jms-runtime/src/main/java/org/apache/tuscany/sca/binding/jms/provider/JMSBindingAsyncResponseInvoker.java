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

package org.apache.tuscany.sca.binding.jms.provider;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.invocation.InvokerAsyncResponse;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * @version $Rev: 1057651 $ $Date: 2011-01-11 14:16:43 +0000 (Tue, 11 Jan 2011) $
 */
public class JMSBindingAsyncResponseInvoker implements InvokerAsyncResponse {
	
	RuntimeEndpoint endpoint;

    public JMSBindingAsyncResponseInvoker(ExtensionPointRegistry extensionPoints,
                                          RuntimeEndpoint endpoint) {
    	this.endpoint = endpoint;
    } // end constructor
    
    public void invokeAsyncResponse(Message msg) {
         // Deliberately left null since in JMS the TransportServiceInterceptor does all the work
    } // end method invokeAsyncResponse
    
} // end class JMSBindingAsyncResponseInvoker