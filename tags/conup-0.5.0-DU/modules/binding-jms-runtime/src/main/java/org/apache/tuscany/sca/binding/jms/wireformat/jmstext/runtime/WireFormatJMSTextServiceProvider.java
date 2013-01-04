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

package org.apache.tuscany.sca.binding.jms.wireformat.jmstext.runtime;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.binding.jms.wireformat.WireFormatJMSText;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

/**
 * @version $Rev: 1159781 $ $Date: 2011-08-19 21:46:35 +0100 (Fri, 19 Aug 2011) $
 */
public class WireFormatJMSTextServiceProvider implements WireFormatProvider {
    private ExtensionPointRegistry registry;
    private RuntimeEndpoint endpoint;
    private JMSBinding binding;
    private InterfaceContract interfaceContract;
    private JMSResourceFactory jmsResourceFactory; 

    public WireFormatJMSTextServiceProvider(ExtensionPointRegistry registry,
                                            RuntimeEndpoint endpoint, 
                                            JMSResourceFactory jmsResourceFactory) {
        super();
        this.registry = registry;
        this.endpoint = endpoint;
        this.jmsResourceFactory = jmsResourceFactory;
        this.binding = (JMSBinding)endpoint.getBinding();
        
        // configure the service based on this wire format
        
        // currently maintaining the message processor structure which 
        // contains the details of jms message processing however override 
        // any message processors specified in the SCDL in this case
 
        // this wire format doubles up as the execution logic for user defined
        // message processors so check the processor name is still set to default 
        // before overwriting
        
        if ((this.binding.getRequestWireFormat() instanceof WireFormatJMSText) &&
            (this.binding.getRequestMessageProcessorName().equals(JMSBindingConstants.DEFAULT_MP_CLASSNAME))){
            this.binding.setRequestMessageProcessorName(JMSBindingConstants.TEXT_MP_CLASSNAME);
        }
        if ((this.binding.getResponseWireFormat() instanceof WireFormatJMSText) &&
            (this.binding.getResponseMessageProcessorName().equals(JMSBindingConstants.DEFAULT_MP_CLASSNAME))){
            this.binding.setResponseMessageProcessorName(JMSBindingConstants.TEXT_MP_CLASSNAME);
        }   
        
        // just point to the reference interface contract so no 
        // databinding transformation takes place
        interfaceContract = endpoint.getService().getService().getInterfaceContract();
    }
        
    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract){
        
        if (this.interfaceContract != null ) {
            if (this.binding.getRequestWireFormat() instanceof WireFormatJMSText){
                // set the request data transformation
                interfaceContract.getInterface().resetInterfaceInputTypes(this.interfaceContract.getInterface());
            }
            if (this.binding.getResponseWireFormat() instanceof WireFormatJMSText){
                // set the response data transformation
                interfaceContract.getInterface().resetInterfaceOutputTypes(this.interfaceContract.getInterface());
            }
        }
        
        return interfaceContract;
    }    

    /**
     */
    public Interceptor createInterceptor() {
        return new WireFormatJMSTextServiceInterceptor(registry, jmsResourceFactory, endpoint);
    }

    /**
     */
    public String getPhase() {
        return Phase.SERVICE_BINDING_WIREFORMAT;
    }

    public InterfaceContract getWireFormatInterfaceContract() {
        return interfaceContract;
    }
}
