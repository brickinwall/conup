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

import java.util.List;

import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents a callback object describing the bindings to use for callbacks.
 * 
 * @version $Rev: 956599 $ $Date: 2010-06-21 15:31:11 +0100 (Mon, 21 Jun 2010) $
 */
public interface Callback extends Base, Extensible, PolicySubject {

    /**
     * Returns the bindings supported for callbacks.
     * 
     * @return the bindings supported for callbacks
     */
    List<Binding> getBindings();
    
    /**
     * Returns the contract that holds this callback
     * 
     * @return the contract that holds this callback
     */
    Contract getParentContract();
    
    /**
     * Sets the contract that holds this callback
     * 
     * @param contract the contract that holds this callback
     */    
    void setParentContract(Contract contract);

}
