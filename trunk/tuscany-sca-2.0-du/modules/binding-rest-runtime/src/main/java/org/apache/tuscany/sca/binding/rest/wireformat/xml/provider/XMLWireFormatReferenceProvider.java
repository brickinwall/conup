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

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;

/**
 * XML wire format Reference Provider.
 * 
 * @version $Rev: 939194 $ $Date: 2010-04-29 05:49:51 +0100 (Thu, 29 Apr 2010) $
*/
public class XMLWireFormatReferenceProvider implements WireFormatProvider {
    private ExtensionPointRegistry extensionPoints;
    private RuntimeEndpointReference endpointReference;

    public XMLWireFormatReferenceProvider(ExtensionPointRegistry extensionPoints,RuntimeEndpointReference endpointReference ) {
        this.extensionPoints = extensionPoints;
        this.endpointReference = endpointReference;
    }
    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract) {
        return null;
    }

    public Interceptor createInterceptor() {
        return null;
    }

    public String getPhase() {
        return Phase.REFERENCE_BINDING_WIREFORMAT;
    }

}
