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

package org.apache.tuscany.sca.interfacedef.java;

import java.lang.reflect.Method;

import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * Represents a Java operation.
 *
 * @version $Rev: 1063125 $ $Date: 2011-01-25 03:38:57 +0000 (Tue, 25 Jan 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface JavaOperation extends Operation {

    /**
     * Returns the Java method defining the operation.
     * @return the Java method
     */
    Method getJavaMethod();
    
    /**
     * Sets the Java method defining the operation.
     * @param method the Java method
     */
    void setJavaMethod(Method method);

    /**
     * Returns the JAX-WS @WebMethod action parameter.
     * @return the action value
     */
    String getAction();
    
    /**
     * Sets the JAX-WS @WebMethod action parameter.
     * @param action the action value
     */
    void setAction(String action);
    
    /**
     * Sets whether this operation has async server style
     * @param isAsync - "true" marks this operation as async server style
     */
    public void setAsyncServer( boolean isAsync );
    
    /** 
     * Indicates whether this operation is async server style
     * @return - true if the operation is async server style
     */
    public boolean isAsyncServer();
    
    /** 
     * Indicates whether the underlying Java method has void return type.
     * @return - true if the Java method has void return type.
     */
    public boolean hasReturnTypeVoid();
    
    /**
     * Sets whether the underlying Java method has void return type.
     * @param flag - "true" marks this operation as having void return type.
     */
    public void setReturnTypeVoid(boolean flag);

}
