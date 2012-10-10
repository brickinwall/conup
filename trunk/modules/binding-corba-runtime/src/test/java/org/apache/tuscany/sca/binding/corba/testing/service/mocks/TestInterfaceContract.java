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

package org.apache.tuscany.sca.binding.corba.testing.service.mocks;

import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;

/**
 * Mock InterfaceContract implementation. Only few methods needs to be
 * implemented.
 */
public class TestInterfaceContract implements InterfaceContract {

    private Interface iface;

    public Interface getCallbackInterface() {
        return null;
    }

    public Interface getInterface() {
        return iface;
    }

    public InterfaceContract makeUnidirectional(boolean isCallback) {
        return null;
    }

    public void setCallbackInterface(Interface callbackInterface) {

    }

    public void setInterface(Interface callInterface) {
        this.iface = callInterface;
    }

    @Override
    public Object clone() {
        return null;
    }
    
    // By default there is no normalized contract
    // as only Java needs it
    public InterfaceContract getNormalizedWSDLContract() {
        return null;
    }
    
    // By default there is no normalized contract
    // as only Java needs it
    public void setNormalizedWSDLContract(
            InterfaceContract wsdlInterfaceContract) {
        // do nothing
    }

}
