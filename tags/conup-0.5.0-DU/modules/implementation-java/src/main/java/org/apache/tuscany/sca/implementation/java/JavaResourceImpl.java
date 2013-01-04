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
package org.apache.tuscany.sca.implementation.java;

/**
 * A resource dependency declared by a Java component implementation
 * 
 * @version $Rev: 1228143 $ $Date: 2012-01-06 12:19:29 +0000 (Fri, 06 Jan 2012) $
 */
public class JavaResourceImpl {
    private JavaElementImpl element;
    private String mappedName;
    private boolean optional;
    private ResourceHost resourceHost;

    public JavaResourceImpl(JavaElementImpl element) {
        this.element = element;
    }
    
    public JavaResourceImpl(JavaElementImpl element, ResourceHost resourceHost) {
        this.element = element;
        this.resourceHost = resourceHost;
    }

    /**
     * The name of the resource
     * 
     * @return the name of the resource
     */
    public String getName() {
        return element.getName();
    }

    /**
     * Returns the URI of the resource
     * 
     * @return the URI of the resource
     */
    public String getMappedName() {
        return mappedName;
    }

    /**
     * Sets the resource URI
     */
    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    /**
     * If true, the resource is optional
     * 
     * @return true if the resource is optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Sets whether the resource is optional
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * @return the element
     */
    public JavaElementImpl getElement() {
        return element;
    }

    /**
     * @return the resourceHost
     */
    public ResourceHost getResourceHost() {
        return resourceHost;
    }
}
