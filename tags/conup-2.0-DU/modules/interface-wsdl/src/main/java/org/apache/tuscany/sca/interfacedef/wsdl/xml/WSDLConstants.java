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
package org.apache.tuscany.sca.interfacedef.wsdl.xml;

import javax.xml.namespace.QName;

/**
 * Constants for WSDL.
 *
 * @version $Rev: 889531 $ $Date: 2009-12-11 08:26:48 +0000 (Fri, 11 Dec 2009) $
 */
public interface WSDLConstants {
    String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    String INTERFACE_WSDL = "interface.wsdl";
    QName INTERFACE_WSDL_QNAME = new QName(SCA11_NS, "interface.wsdl");
    String INTERFACE = "interface";
    String CALLBACK_INTERFACE = "callbackInterface";
    String REMOTABLE = "remotable";
    String WSDL_LOCATION = "wsdlLocation";
    String WSDLI_NS = "http://www.w3.org/2004/08/wsdl-instance";
    QName WSDL_LOCATION_QNAME = new QName(WSDLI_NS, WSDL_LOCATION); 

}
