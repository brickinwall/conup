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
package org.apache.tuscany.sca.implementation.java.introspect.impl;

import org.apache.tuscany.sca.implementation.java.IntrospectionException;

/**
 * Thrown when a service type specified by an {@link org.oasisopen.sca.annotation.Service} annotation is invalid, e.g. it is
 * not an interface
 *
 * @version $Rev: 967109 $ $Date: 2010-07-23 15:30:46 +0100 (Fri, 23 Jul 2010) $
 */
public class InvalidServiceTypeException extends IntrospectionException {
    private static final long serialVersionUID = -1076466639416644386L;
    private Class<?> serviceType;

    public InvalidServiceTypeException(String message) {
        super(message);
    }
    
    public InvalidServiceTypeException(String message, Class<?> clazz) {
        super(message);
        this.serviceType = clazz;
    }

    /**
     * @return the serviceType
     */
    public Class<?> getServiceType() {
        return serviceType;
    }    
}
