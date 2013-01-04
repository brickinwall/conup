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
package org.apache.tuscany.sca.assembly;

import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Interface contracts define one or more business functions. These business
 * functions are provided by services and are used by references.
 * 
 * @version $Rev: 938419 $ $Date: 2010-04-27 13:28:09 +0100 (Tue, 27 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface AbstractContract extends Base, Extensible, PolicySubject {

    /**
     * Returns the name of the contract.
     * 
     * @return the name of the contract
     */
    String getName();

    /**
     * Sets the name of the contract.
     * 
     * @param name the name of the contract
     */
    void setName(String name);

    /**
     * Returns the interface contract defining the interface and callback
     * interface for the contract.
     * 
     * @return the interface contract
     */
    InterfaceContract getInterfaceContract();

    /**
     * Sets the interface contract defining the interface and callback
     * interface for the contract.
     * 
     * @param interfaceContract the interface contract
     */
    void setInterfaceContract(InterfaceContract interfaceContract);

    /**
     * Returns true if this contract is a reference or service created internally
     * to handle a callback interface of another contract, false otherwise.
     * 
     * @return true for a callback contract, false otherwise
     */
    boolean isForCallback();

    /**
     * Sets a flag indicating whether this is a callback contract.
     * 
     * @param isCallback true for a callback contract, false otherwise
     */
    void setForCallback(boolean isCallback);

}
