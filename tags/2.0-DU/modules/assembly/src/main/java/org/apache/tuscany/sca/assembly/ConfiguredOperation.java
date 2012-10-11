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

import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents an Operation. Typically Operation elements are used when there is a
 * need to apply certain facets such as intents and policysets only to a specific
 * operation provided by a service or reference.
 *
 * @tuscany.spi.extension.asclient
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public interface ConfiguredOperation extends Base, PolicySubject {
    /**
     * Returns the name of the operation.
     * 
     * @return the name of the operation
     */
    String getName();

    /**
     * Sets the name of the operation.
     * 
     * @param name the name of the operation
     */
    void setName(String name);
    
    /**
     * Returns the name of the service or reference to which this operation belongs.
     * This method is particularly useful when operation elements are specified under implementation
     * elements and it is necessary to identify which of the various services provided by the 
     * implementation is referred to by the operation element in question
     * 
     * @return the name of the contract to which this operation belongs
     */
    String getContractName();
    
    /**
     * Sets the name of the service or reference to which this operation belongs.
     * 
     * @param contractName the name of the contract to which this operation belongs
     */
    void setContractName(String contractName);
}
