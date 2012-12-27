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
package org.apache.tuscany.sca.interfacedef.wsdl.impl;

import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractImpl;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterfaceContract;

/**
 * Represents a WSDL interface contract.
 * 
 * @version $Rev: 1149451 $ $Date: 2011-07-22 05:12:56 +0100 (Fri, 22 Jul 2011) $
 */
public class WSDLInterfaceContractImpl extends InterfaceContractImpl implements WSDLInterfaceContract {
    private String location;
    
    protected WSDLInterfaceContractImpl() {
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public WSDLInterfaceContractImpl clone() throws CloneNotSupportedException {
        return (WSDLInterfaceContractImpl) super.clone();
    }
    
    public InterfaceContract getNormalizedWSDLContract() {
        return this;
    }
    
    public void setNormalizedWSDLContract(InterfaceContract wsdlInterfaceContract) {
        // do nothing as this already is a WSDL contract
    }
    
    @Override
    public String toString() {
    	Interface intf = getInterface();
    	Interface cbIntf = getCallbackInterface();
    	StringBuilder b = new StringBuilder(128);
    	b.append("Interface: " + (intf == null ? "null" : intf.toString()));
    	b.append(", Callback Interface: " + (cbIntf == null ? "null" : cbIntf.toString()));
    	return b.toString();
    }
}
