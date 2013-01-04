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

package org.apache.tuscany.sca.binding.local;

import org.apache.tuscany.sca.binding.sca.transform.BindingSCATransformer;
import org.apache.tuscany.sca.binding.sca.transform.WSDLMediateTransformer;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.provider.SCABindingMapper;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
*
* @version $Rev$ $Date$
*/
public class XMLWFLocalSCAReferenceBindingProvider extends DefaultLocalSCAReferenceBindingProvider {

    public XMLWFLocalSCAReferenceBindingProvider(ExtensionPointRegistry extensionPoints,
                                                 RuntimeEndpointReference endpointReference,
                                                 SCABindingMapper mapper) {
        super(extensionPoints, endpointReference, mapper);
    }
    
    protected BindingSCATransformer getBindingTransformer(Operation sourceOperation, Operation targetOperation) { 
        InterfaceContract bindingInterfaceContract = getWSDLBindingInterfaceContract();   
        if (!bindingInterfaceContract.getInterface().isRemotable()) {
            throw new IllegalStateException("This method should only have been called for a remotable interface.");
        }
        Operation wsdlBindingOperation = interfaceContractMapper.map(bindingInterfaceContract.getInterface(), sourceOperation);                        
        return new WSDLMediateTransformer(mediator, sourceOperation, wsdlBindingOperation, targetOperation);                
    }
}
