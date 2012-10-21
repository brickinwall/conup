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

package org.apache.tuscany.sca.binding.jms.wireformat.jmsdefault.runtime;

import java.util.HashMap;
import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.provider.xml.XMLHelper;
import org.apache.tuscany.sca.binding.jms.provider.xml.XMLHelperFactory;
import org.apache.tuscany.sca.binding.jms.wireformat.WireFormatJMSDefault;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.binding.ws.WebServiceBindingFactory;
import org.apache.tuscany.sca.binding.ws.wsdlgen.BindingWSDLGenerator;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class WireFormatJMSDefaultReferenceProvider implements WireFormatProvider {
    private ExtensionPointRegistry registry;
    private RuntimeEndpointReference endpointReference;
    private ComponentReference reference;
    private JMSBinding binding;
    private InterfaceContract interfaceContract;
    private XMLHelper xmlHelper;
    private HashMap<String, Boolean> inputWrapperMap;
    private HashMap<String, Object> outputWrapperMap;

    public WireFormatJMSDefaultReferenceProvider(ExtensionPointRegistry registry, RuntimeEndpointReference endpointReference) {
        super();
        this.registry = registry;
        this.endpointReference = endpointReference;
        this.binding = (JMSBinding) endpointReference.getBinding();

        this.xmlHelper = XMLHelperFactory.createXMLHelper(registry);
        this.inputWrapperMap = new HashMap<String, Boolean>();
        this.outputWrapperMap = new HashMap<String, Object>();

        // configure the reference based on this wire format

        // currently maintaining the message processor structure which
        // contains the details of jms message processing so set the message
        // type here if not set explicitly in SCDL
        if (this.binding.getRequestWireFormat() instanceof WireFormatJMSDefault){
            this.binding.setRequestMessageProcessorName(JMSBindingConstants.DEFAULT_MP_CLASSNAME);
        }
        if (this.binding.getResponseWireFormat() instanceof WireFormatJMSDefault){
            this.binding.setResponseMessageProcessorName(JMSBindingConstants.DEFAULT_MP_CLASSNAME);
        }

        this.reference = endpointReference.getReference();
        // TODO - can be null if it's a $self$ reference. Need to decide if 
        //        that's valid
        if (reference.getReference() == null){
            interfaceContract = reference.getInterfaceContract();
            return;
        }
        
        List<Operation> opList = reference.getReference().getInterfaceContract().getInterface().getOperations();

        // Go through each operation and add wrapper info

        // set the binding interface contract to represent the WSDL for the
        // xml messages that will be sent
        // I think we have to check for asIs because the Java2WSDL will blow up when using javax.jms.Message
        if (reference.getInterfaceContract() != null && !isAsIs()) {
            WebServiceBindingFactory wsFactory = registry.getExtensionPoint(WebServiceBindingFactory.class);
            WebServiceBinding wsBinding = wsFactory.createWebServiceBinding();
            BindingWSDLGenerator.generateWSDL(endpointReference.getComponent(), reference, wsBinding, registry, null);
            interfaceContract = wsBinding.getBindingInterfaceContract();
            interfaceContract.getInterface().resetDataBinding(XMLHelperFactory.createXMLHelper(registry).getDataBindingName());

            List<Operation> wsdlOpList = interfaceContract.getInterface().getOperations();

            for (Operation op : opList) {

                String name = op.getName();
                Operation matchingWsdlOp = null;

                // find the matching wsdlop
                for (Operation wsdlOp : wsdlOpList) {
                    if (name.equals(wsdlOp.getName())) {
                        matchingWsdlOp = wsdlOp;

                        break;
                    }
                }

                // only add operations that need to be wrapped/unwrapped
                // we need to look at the wsdl interface to determine if the op is wrapped or not

                // TODO - not sure we really support viewing the input/output as separately wrapped 
                // like the separate code paths imply.  Not sure how many @OneWay tests we have, this might 
                // not be an issue.  

                if (matchingWsdlOp.isInputWrapperStyle()) {
                    if (op.getInputType().getLogical().size() == 1) {
                        this.inputWrapperMap.put(name, true);
                    } else {
                        this.inputWrapperMap.put(name, false);
                    }
                } else {
                    this.inputWrapperMap.put(name, false);
                }

                if (matchingWsdlOp.isOutputWrapperStyle()) {
                    // we only need to know what the wrapper is on the deserialization
                    // might need to change this when there input/output wrapper style is different
                    ElementInfo ei = op.getOutputWrapper().getWrapperElement();
                    this.outputWrapperMap.put(name, xmlHelper.createWrapper(ei.getQName()));
                } 
            }
        } else {
            interfaceContract = reference.getReference().getInterfaceContract();
        }

    }

    protected boolean isAsIs() {
        InterfaceContract ic = reference.getInterfaceContract();
        if (ic.getInterface().getOperations().size() != 1) {
            return false;
        }

        List<DataType> inputDataTypes = ic.getInterface().getOperations().get(0).getInputType().getLogical();

        if (inputDataTypes.size() != 1) {
            return false;
        }

        Class<?> inputType = inputDataTypes.get(0).getPhysical();

        if (javax.jms.Message.class.isAssignableFrom(inputType)) {
            return true;
        }
        return false;
    }

    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract){
        
        if (this.interfaceContract != null &&
            !isAsIs()) {
            if (this.binding.getRequestWireFormat() instanceof WireFormatJMSDefault){
                // set the request data transformation
                interfaceContract.getInterface().resetInterfaceInputTypes(this.interfaceContract.getInterface());
            }
            if (this.binding.getResponseWireFormat() instanceof WireFormatJMSDefault){
                // set the response data transformation
                interfaceContract.getInterface().resetInterfaceOutputTypes(this.interfaceContract.getInterface());
            }
        }
        
        return interfaceContract;
    }   

    public Interceptor createInterceptor() {
        return new WireFormatJMSDefaultReferenceInterceptor(registry, null, endpointReference, inputWrapperMap, outputWrapperMap);
    }

    public String getPhase() {
        return Phase.REFERENCE_BINDING_WIREFORMAT;
    }

    public InterfaceContract getWireFormatInterfaceContract() {
        return interfaceContract;
    }
}
