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

package org.apache.tuscany.sca.binding.rest.wireformat.xml.provider;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.binding.rest.provider.JAXRSHelper;
import org.apache.tuscany.sca.binding.rest.wireformat.xml.XMLWireFormat;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.javabeans.SimpleJavaDataBinding;
import org.apache.tuscany.sca.databinding.xml.XMLStringDataBinding;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

/**
 * XML wire format service provider.
 *
 * @version $Rev: 1035091 $ $Date: 2010-11-14 22:34:32 +0000 (Sun, 14 Nov 2010) $
*/
public class XMLWireFormatServiceProvider implements WireFormatProvider {
    private static final String DATABABINDING = XMLStreamReader.class.getName();

    private ExtensionPointRegistry extensionPoints;
    private RuntimeEndpoint endpoint;

    private InterfaceContract serviceContract;
    private Binding binding;

    private boolean jaxrs;

    public XMLWireFormatServiceProvider(ExtensionPointRegistry extensionPoints, RuntimeEndpoint endpoint) {
        this.extensionPoints = extensionPoints;
        this.endpoint = endpoint;
        this.binding = endpoint.getBinding();
        this.jaxrs = isJAXRSResource();
    }

    private boolean isJAXRSResource() {
        Interface interfaze = endpoint.getComponentServiceInterfaceContract().getInterface();
        if (interfaze instanceof JavaInterface) {
            if (JAXRSHelper.isJAXRSResource(((JavaInterface)interfaze).getJavaClass())) {
                return true;
            }
        }
        return false;
    }

    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract) {
        serviceContract = interfaceContract;

        if (!jaxrs) {
            boolean configureInput = binding.getRequestWireFormat() != null;
            boolean configureOutput = binding.getRequestWireFormat() != null || binding.getResponseWireFormat() != null;

            //set XML databinding
            setDataBinding(serviceContract.getInterface(), configureInput, configureOutput);
        }

        return serviceContract;
    }

    public Interceptor createInterceptor() {
        if (jaxrs) {
            return null;
        }

        if( (binding.getRequestWireFormat() != null && binding.getRequestWireFormat() instanceof XMLWireFormat) ||
            (binding.getResponseWireFormat() != null && binding.getResponseWireFormat() instanceof XMLWireFormat) ){
            return new XMLWireFormatInterceptor(extensionPoints, endpoint);
        }
        return null;
    }

    public String getPhase() {
        return Phase.SERVICE_BINDING_WIREFORMAT;
    }


    /**
     * Utility method to reset data binding for the interface contract
     * @param interfaze
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    private void setDataBinding(Interface interfaze, boolean configureInput, boolean configureOutput) {
        List<Operation> operations = interfaze.getOperations();
        for (Operation operation : operations) {
            // handle input types
            if (configureInput) {
                operation.setDataBinding(DATABABINDING);
                DataType<List<DataType>> inputType = operation.getInputType();
                if (inputType != null) {
                    List<DataType> logical = inputType.getLogical();
                    for (DataType inArg : logical) {
                        if (!SimpleJavaDataBinding.NAME.equals(inArg.getDataBinding())) {
                            inArg.setDataBinding(DATABABINDING);
                        }
                    }
                }
            }

            // handle output types
            if (configureOutput) {
                List<DataType> outputTypes = operation.getOutputType().getLogical();
                for ( DataType outputType : outputTypes ) {
                	if (outputType != null) {
                		outputType.setDataBinding(XMLStringDataBinding.NAME);
                	}
                }
            }
        }
    }
}
