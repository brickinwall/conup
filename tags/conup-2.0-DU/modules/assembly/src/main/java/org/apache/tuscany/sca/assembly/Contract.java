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

import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.policy.PolicySubject;

/**
 * Represents a contract. A contract can be either a service or a reference.
 * 
 * @version $Rev: 1127498 $ $Date: 2011-05-25 13:39:38 +0100 (Wed, 25 May 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface Contract extends AbstractContract, PolicySubject, Cloneable {

    /**
     * Returns the bindings supported by this contract.
     * 
     * @return the bindings supported by this contract
     */
    List<Binding> getBindings();
    
    /**
     * Return the named binding
     * 
     * @return the named binding or null if it can't be found
     */
    Binding getBinding(String name);    

    /**
     * Returns a binding of the specified type or null if there is no such
     * binding configured on this contract.
     * 
     * @param <B> the binding type
     * @param bindingClass the binding type class
     * @return the binding or null if there is no binding of the specified type
     */
    <B> B getBinding(Class<B> bindingClass);

    /**
     * Returns a callback binding of the specified type or null if there is no such
     * callback binding configured on this contract.
     * 
     * @param <B> the callback binding type
     * @param bindingClass the callback binding type class
     * @return the callback binding or null if there is no callback binding of the specified type
     */
    <B> B getCallbackBinding(Class<B> bindingClass);

    /**
     * Returns a callback definition of the bindings to use for callbacks.
     * 
     * @return a definition of the bindings to use for callbacks
     */
    Callback getCallback();

    /**
     * Sets a callback definition of the bindings to use for callbacks
     * 
     * @param callback a definition of the bindings to use for callbacks
     */
    void setCallback(Callback callback);

    /**
     * Returns a clone of the contract.
     * 
     * @return a clone of the reference
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Returns the interface contract given a binding. Important in the case where
     * a reference with multiplicity > 1 has been promoted and has it's list of 
     * resolved bindings extended by a promoting reference. Here the binding
     * from the promoting reference may need the interface contract from the 
     * promoting reference and not the promoted reference.
     * TODO - remove this wrinkle with better endpoint support.  
     * 
     * @param binding the binding for which the interface contract is required
     * @return the interface contract
     */
    InterfaceContract getInterfaceContract(Binding binding);
    
    /**
     * A flag to tell if there are any binding elements specified for this service or reference. 
     * If true, they override the the bindings in the corresponding reference/service in the 
     * component type or promoted reference/service.  
     * @return
     */
    boolean isOverridingBindings();
    
    /**
     * Set the flag depending on if there are any binding elements specified for this service or 
     * reference.
     * @param overridingBindings true if there are binding elements specified
     */
    void setOverridingBindings(boolean overridingBindings);

}
