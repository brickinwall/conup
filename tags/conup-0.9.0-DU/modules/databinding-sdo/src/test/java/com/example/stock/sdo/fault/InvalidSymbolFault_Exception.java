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

package com.example.stock.sdo.fault;

import javax.xml.namespace.QName;

import com.example.stock.sdo.InvalidSymbolFault;

/**
 * Hand-crafted java exception for SDO fault
 *
 * @version $Rev: 656176 $ $Date: 2008-05-14 10:25:53 +0100 (Wed, 14 May 2008) $
 */
public class InvalidSymbolFault_Exception extends Exception {
    private static final long serialVersionUID = 8602157311925253920L;
    
    /**
     * Generated QName for the fault element
     */
    public static final QName FAULT_ELEMENT = new QName("http://www.example.com/stock", "InvalidSymbolFault");
    /**
     * Java type that goes as soapenv:Fault detail element.
     */
    private InvalidSymbolFault faultInfo;

    /**
     * @param faultInfo
     * @param message
     */
    public InvalidSymbolFault_Exception(String message, InvalidSymbolFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * @param faultInfo
     * @param message
     * @param cause
     */
    public InvalidSymbolFault_Exception(String message, InvalidSymbolFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * @return returns fault bean:
     *         org.apache.tuscany.sca.test.exceptions.impl.jaxb.InvalidSymbolFault
     */
    public InvalidSymbolFault getFaultInfo() {
        return faultInfo;
    }

}
