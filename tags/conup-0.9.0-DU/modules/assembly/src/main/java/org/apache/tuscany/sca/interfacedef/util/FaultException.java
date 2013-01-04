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

package org.apache.tuscany.sca.interfacedef.util;

import javax.xml.namespace.QName;

/**
 * The generic java exception to wrap service faults
 * 
 * @version $Rev: 937995 $ $Date: 2010-04-26 11:55:14 +0100 (Mon, 26 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public class FaultException extends Exception {
    private static final long serialVersionUID = -8002583655240625792L;
    private transient Object faultInfo; // FIXME: How to serialize it?
    private QName faultName;

    /**
     * @param message
     * @param faultInfo
     */
    public FaultException(String message, Object faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public FaultException(String message, Object faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * @return the faultInfo
     */
    public Object getFaultInfo() {
        return faultInfo;
    }

    public QName getFaultName() {
        return faultName;
    }

    public void setFaultName(QName logical) {
        this.faultName = logical;
    }

    public boolean isMatchingType(Object type) {
        if (faultName == null) {
            return false;
        }

        if ((type instanceof QName) && faultName.equals(type)) {
            return true;
        }
        if (type instanceof XMLType && faultName.equals(((XMLType)type).getElementName())) {
            return true;
        }
        return false;
    }

}
