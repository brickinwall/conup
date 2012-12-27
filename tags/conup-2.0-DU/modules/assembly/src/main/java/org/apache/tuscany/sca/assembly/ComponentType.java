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
 * Describes an implementation and represents its configurable aspects.
 * 
 * @version $Rev: 937321 $ $Date: 2010-04-23 16:01:54 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.asclient
 */
public interface ComponentType extends Base, PolicySubject {

    /**
     * Returns the URI of this component type.
     * @return the URI of the component type
     */
    String getURI();

    /**
     * Sets the URI of this component type.
     * @param uri the URI of the component type
     */
    void setURI(String uri);

    /**
     * Returns a list of services that are offered.
     * 
     * @return a list of services that are offered
     */
    List<Service> getServices();
    
    /**
     * Return a service by name
     * 
     * @param name the service name
     * @return service the service
     */
    Service getService(String name);
   
    /**
     * Returns the list of reference types that are used.
     * 
     * @return the list of reference types that are used
     */
    List<Reference> getReferences();
    
    /**
     * Return a reference by name
     * 
     * @param name the reference name
     * @return reference the reference
     */
    Reference getReference(String name);

    /**
     * Returns the list of properties that can be set.
     * 
     * @return the list of properties that can be set
     */
    List<Property> getProperties();
    
    /**
     * Return a property by name
     * 
     * @param name the property name
     * @return property the property
     */
    Property getProperty(String name);

}
