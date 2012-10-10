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

/**
 * Represents a service. Services are used to publish services provided by
 * implementations, so that they are addressable by other components.
 * 
 * @version $Rev: 984702 $ $Date: 2010-08-12 10:29:25 +0100 (Thu, 12 Aug 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface Service extends AbstractService, Contract {
    /**
     * Returns the endpoints implied by this service.
     * 
     * Endpoints represent configured bindings for a service. Hence a service
     * with two bindings will expose two Endpoints. Where a promoted service has 
     * new binding configuration applied by a promoting component Endpoints are
     * introduced to represent these new bindings.  
     * 
     * @return the endpoints implied by this service
     */
    List<Endpoint> getEndpoints();
    
    
    /**
     * Returns true if this service was defined based on JAXWS annotations 
     * in a Java implementation
     * 
     * @return
     */
    boolean isJAXWSService();
    
    /**
     * Allows the JAXWSService flag to be set. It should be set true if the service
     * configuration has been affected by JAXWS annotations. 
     * 
     * @param isJAXWSService
     */
    void setJAXWSService(boolean isJAXWSService);
}
