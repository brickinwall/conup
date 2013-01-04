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

package org.apache.tuscany.sca.binding.jms.wireformat.jmstextxml.runtime;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.binding.jms.provider.xml.XMLHelperFactory;
import org.apache.tuscany.sca.binding.jms.wireformat.WireFormatJMSTextXML;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.binding.ws.WebServiceBindingFactory;
import org.apache.tuscany.sca.binding.ws.wsdlgen.BindingWSDLGenerator;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.xml.DOMDataBinding;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

/**
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class WireFormatJMSTextXMLServiceProvider implements WireFormatProvider {
    private ExtensionPointRegistry registry;
    private RuntimeEndpoint endpoint;
    private JMSBinding binding;
    private JMSResourceFactory jmsResourceFactory;
    private InterfaceContract interfaceContract; 

    public WireFormatJMSTextXMLServiceProvider(ExtensionPointRegistry registry, 
                                               RuntimeEndpoint endpoint, 
                                               JMSResourceFactory jmsResourceFactory) {
        super();
        this.endpoint = endpoint;
        this.binding = (JMSBinding)endpoint.getBinding();
        this.jmsResourceFactory = jmsResourceFactory;
        this.registry = registry;
        
        // configure the service based on this wire format
        
        // currently maintaining the message processor structure which 
        // contains the details of jms message processing so set the message
        // type here if not set explicitly in SCDL
        if (this.binding.getRequestWireFormat() instanceof WireFormatJMSTextXML){
            this.binding.setRequestMessageProcessorName(JMSBindingConstants.XML_MP_CLASSNAME);
        }
        if (this.binding.getResponseWireFormat() instanceof WireFormatJMSTextXML){
            this.binding.setResponseMessageProcessorName(JMSBindingConstants.XML_MP_CLASSNAME);
        }
        
        // create a local interface contract that is configured specifically to 
        // deal with the data format that this wire format is expecting to send to 
        // and receive from the databinding interceptor. The request/response parts of 
        // this interface contract will be copied into the binding interface contract
        // as required
        WebServiceBindingFactory wsFactory = registry.getExtensionPoint(WebServiceBindingFactory.class);
        WebServiceBinding wsBinding = wsFactory.createWebServiceBinding();
        BindingWSDLGenerator.generateWSDL(endpoint.getComponent(), endpoint.getService(), wsBinding, registry, null);
        interfaceContract = wsBinding.getBindingInterfaceContract();
        interfaceContract.getInterface().resetDataBinding(XMLHelperFactory.createXMLHelper(registry).getDataBindingName());
    }
    
    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract){
        
        if (this.interfaceContract != null) {
            if (this.binding.getRequestWireFormat() instanceof WireFormatJMSTextXML){
                // set the request data transformation
                interfaceContract.getInterface().resetInterfaceInputTypes(this.interfaceContract.getInterface());
            }
            if (this.binding.getResponseWireFormat() instanceof WireFormatJMSTextXML){
                // set the response data transformation
                interfaceContract.getInterface().resetInterfaceOutputTypes(this.interfaceContract.getInterface());
            }
        }
        
        return interfaceContract;
    }
    
    public Interceptor createInterceptor() {
        return new WireFormatJMSTextXMLServiceInterceptor(registry, jmsResourceFactory, endpoint);
    }

    public String getPhase() {
        return Phase.SERVICE_BINDING_WIREFORMAT;
    }

    public InterfaceContract getWireFormatInterfaceContract() {
        return interfaceContract;
    }

}
