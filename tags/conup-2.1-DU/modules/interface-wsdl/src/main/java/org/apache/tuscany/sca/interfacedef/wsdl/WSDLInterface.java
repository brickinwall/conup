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
package org.apache.tuscany.sca.interfacedef.wsdl;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.interfacedef.Interface;


/**
 * Represents a WSDL interface.
 *
 * @version $Rev: 937321 $ $Date: 2010-04-23 16:01:54 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 * TODO check this spi tag is valid (indicated by access from implementation.bpel)
 */
public interface WSDLInterface extends Interface, Base {

    /**
     * Returns the name of the WSDL interface.
     *
     * @return the name of the WSDL interface
     */
    QName getName();

    /**
     * Sets the name of the WSDL interface.
     *
     * @param interfaceName the name of the WSDL interface
     */
    void setName(QName interfaceName);

    /**
     * Returns the WSDL interface portType.
     *
     * @return the WSDL interface portType
     */
    PortType getPortType();

    /**
     * Sets the WSDL interface portType
     *
     * @param portType the WSDL interface portType
     */
    void setPortType(PortType portType);
    
    /**
     * Gets the Interface of the Callback for this WSDL interface
     * @return the WSDLInterface of the Callback, null if there is no Callback
     */
    WSDLInterface getCallbackInterface();
    
    /**
     * Sets the interface of the Callback for this WSDL interface
     * @param callback - the WSDLInterface of the Callback
     */
    void setCallbackInterface(WSDLInterface callback);

    WSDLDefinition getWsdlDefinition();

    void setWsdlDefinition(WSDLDefinition wsdlDefinition);

}
