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

package org.apache.tuscany.sca.implementation.osgi.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.implementation.osgi.ServiceDescription;
import org.osgi.framework.Constants;

/**
 * The OSGi RFC 119 description of a remote OSGi service
 */
public class ServiceDescriptionImpl implements ServiceDescription {
    private Map<String, Object> properties = new HashMap<String, Object>();

    protected ServiceDescriptionImpl() {
        super();
    }

    public List<String> getInterfaces() {
        return Arrays.asList((String[])properties.get(Constants.OBJECTCLASS));
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String toString() {
        return "endpoint-description: " + properties;
    }
}
