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

package org.apache.tuscany.sca.runtime;

import org.apache.tuscany.sca.assembly.EndpointReference;

/**
 * Parameters for the EndPointReference
 * 
 * @version $Rev: 796867 $ $Date: 2009-07-22 21:19:26 +0100 (Wed, 22 Jul 2009) $
 */
public interface ReferenceParameters extends Cloneable {
    /**
     * Get the callback ID
     * @return the callbackID
     */
    Object getCallbackID();

    /**
     * Set the callback ID
     * @param callbackID the callbackID to set
     */
    void setCallbackID(Object callbackID);

    /**
     * Get the ID for the non-ServiceReference callback object
     * @return
     */
    Object getCallbackObjectID();

    /**
     * Set the ID for the non-ServiceReference callback object
     * @param callbackObjectID
     */
    void setCallbackObjectID(Object callbackObjectID);

    EndpointReference getCallbackReference();

    void setCallbackReference(EndpointReference callback);

    Object clone() throws CloneNotSupportedException;
}
