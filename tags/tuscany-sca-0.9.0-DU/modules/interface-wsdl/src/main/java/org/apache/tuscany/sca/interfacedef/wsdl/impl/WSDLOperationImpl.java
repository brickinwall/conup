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

import javax.wsdl.Operation;

import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLOperation;

/**
 * @version $Rev: 685532 $ $Date: 2008-08-13 14:03:50 +0100 (Wed, 13 Aug 2008) $
 */
public class WSDLOperationImpl extends OperationImpl implements WSDLOperation {
    private Operation operation;

    public WSDLOperationImpl() {
        super();
    }

    /**
     * @see org.apache.tuscany.sca.interfacedef.wsdl.WSDLOperation#getWsdlOperation()
     */
    public Operation getWsdlOperation() {
        return operation;
    }

    /**
     * @see org.apache.tuscany.sca.interfacedef.wsdl.WSDLOperation#setWsdlOperation(javax.wsdl.Operation)
     */
    public void setWsdlOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WSDLOperationImpl other = (WSDLOperationImpl)obj;
        if (operation == null) {
            if (other.operation != null)
                return false;
        } else if (!operation.equals(other.operation))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return operation == null ? null : operation.toString();
    }

}
