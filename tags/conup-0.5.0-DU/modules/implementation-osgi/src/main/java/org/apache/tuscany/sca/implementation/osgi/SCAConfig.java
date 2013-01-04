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

package org.apache.tuscany.sca.implementation.osgi;

import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * 
 */
public interface SCAConfig {
    /**
     * Returns the target namespace for this SCA Definition
     * @return the target namespace
     */
    String getTargetNamespace();

    /**
     * Sets the target names for this SCA Definition.
     * 
     * @param ns the target namespace for this SCA Definition
     */
    void setTargetNamespace(String ns);

    /**
     * Returns a list of domain wide Policy Intents
     * 
     * @return a list of domain wide Policy Intents 
     */
    List<Intent> getIntents();

    /**
     * Returns a list of domain wide PolicySets
     * 
     * @return a list of domain wide PolicySets 
     */
    List<PolicySet> getPolicySets();

    /**
     * Returns a list of domain wide binding definition objects
     * 
     * @return a list of domain wide binding definition objects 
     */
    List<Binding> getBindings();
}
