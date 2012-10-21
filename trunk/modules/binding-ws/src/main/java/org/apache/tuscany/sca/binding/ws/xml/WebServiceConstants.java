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
package org.apache.tuscany.sca.binding.ws.xml;

import javax.xml.namespace.QName;

/**
 * Constants for the Web Services Binding.
 *
 * @version $Rev: 953965 $ $Date: 2010-06-12 10:03:08 +0100 (Sat, 12 Jun 2010) $
 */
public interface WebServiceConstants {
	String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
	
    String BINDING_WS = "binding.ws";
    QName BINDING_WS_QNAME = new QName(SCA11_NS, BINDING_WS);
    
    String WSDL_ELEMENT = "wsdlElement";
    QName WSDL_ELEMENT_QNAME = new QName(SCA11_NS, WSDL_ELEMENT);
    
    String WSDL_LOCATION = "wsdlLocation";
    String WSDLI_NS = "http://www.w3.org/ns/wsdl-instance";
    
    QName WSDL_LOCATION_QNAME = new QName(WSDLI_NS, WSDL_LOCATION); 
    
    String NAME = "name"; 
    String URI = "uri"; 
    String END_POINT_REFERENCE = "EndpointReference";
    

}
