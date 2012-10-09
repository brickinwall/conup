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

package org.apache.tuscany.sca.interfacedef.java.impl;

import java.lang.reflect.Method;

import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;

/**
 * Represents a Java operation. 
 *
 * @version $Rev: 1063125 $ $Date: 2011-01-25 03:38:57 +0000 (Tue, 25 Jan 2011) $
 */
public class JavaOperationImpl extends OperationImpl implements JavaOperation {

    private Method method;
    private String action;
    private boolean isAsyncServer = false;
    private boolean hasReturnTypeVoid = false;
    
    public Method getJavaMethod() {
        return method;
    }

    public void setJavaMethod(Method method) {
        this.method = method;
    }

    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((method == null) ? 0 : method.hashCode());
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
        final JavaOperationImpl other = (JavaOperationImpl)obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }
    
    /**
     * Sets whether this operation has async server style
     * @param isAsync - "true" marks this operation as async server style
     */
    public void setAsyncServer( boolean isAsync ) {
    	isAsyncServer = isAsync;
    }
    
    /** 
     * Indicates whether this operation is async server style
     * @return - true if the operation is async server style
     */
    public boolean isAsyncServer() {
    	return isAsyncServer;
    }

    @Override
    public String toString() {
        return method == null ? "null" : method.toGenericString();
    }

    @Override
    public boolean hasReturnTypeVoid() {
        return hasReturnTypeVoid;
    }
    
    @Override
    public void setReturnTypeVoid(boolean flag) {
        hasReturnTypeVoid = flag;
    }

}
