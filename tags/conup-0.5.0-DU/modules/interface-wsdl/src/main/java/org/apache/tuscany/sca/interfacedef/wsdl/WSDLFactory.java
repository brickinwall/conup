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

import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.monitor.Monitor;

/**
 * Factory for the WSDL model.
 * 
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public interface WSDLFactory {

    /**
     * Creates a new WSDL interface.
     * 
     * @return a new WSDL interface
     */
    WSDLInterface createWSDLInterface();
    
    /**
     * Creates a new WSDL interface from a WSDL portType.
     *
     * @param portType the portType to inspect
     * @return a WSDLInterface corresponding to the WSDL portType
     */
    WSDLInterface createWSDLInterface(PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver, Monitor monitor) throws InvalidInterfaceException;

    /**
     * Creates the contents of a WSDL interface from a WSDL portType.
     *
     * @param portType the portType to inspect
     * @return a WSDLInterface corresponding to the WSDL portType
     */
    void createWSDLInterface(WSDLInterface wsdlInterface, PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver, Monitor monitor) throws InvalidInterfaceException;

    /**
     * Creates a new WSDL definition.
     * 
     * @return a new WSDL definition
     */
    WSDLDefinition createWSDLDefinition();
    
    /**
     * Creates a new WSDL interface contract.
     * 
     * @return
     */
    WSDLInterfaceContract createWSDLInterfaceContract();
    
}
