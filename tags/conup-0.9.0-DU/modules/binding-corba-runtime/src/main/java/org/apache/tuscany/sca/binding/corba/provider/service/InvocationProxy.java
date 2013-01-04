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

package org.apache.tuscany.sca.binding.corba.provider.service;

import java.util.List;

/**
 * @version $Rev: 697647 $ $Date: 2008-09-22 01:52:08 +0100 (Mon, 22 Sep 2008) $
 * Target proxy interface for service bindings
 */
public interface InvocationProxy {

    /**
     * Gets operations types for target
     * @param operationName
     * @return
     */
    public OperationTypes getOperationTypes(String operationName);
    
    /**
     * Invokes target operation
     * @param operationName
     * @param arguments
     * @return
     * @throws InvocationException
     */
    public Object invoke(String operationName, List<Object> arguments) throws InvocationException;
    
}
