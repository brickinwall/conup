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
package org.apache.tuscany.sca.implementation.spring;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a <bean> element in a Spring application-context
 * - this has id and className attributes
 * - plus zero or more property elements as children
 *
 * @version $Rev: 987670 $ $Date: 2010-08-21 00:42:07 +0100 (Sat, 21 Aug 2010) $
 */
public class SpringBeanElement {

    private String id;
    private String className = null;
    private boolean innerBean = false;
    private boolean abstractBean = false;
    private boolean parentAttribute = false;
    private boolean factoryBeanAttribute = false;
    private boolean factoryMethodAttribute = false;

    private List<SpringPropertyElement> properties = new ArrayList<SpringPropertyElement>();
    private List<SpringConstructorArgElement> constructorargs = new ArrayList<SpringConstructorArgElement>();

    public SpringBeanElement() {
    }
    
    public SpringBeanElement(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SpringPropertyElement> getProperties() {
        return properties;
    }

    public void addProperty(SpringPropertyElement property) {
        properties.add(property);
    }

    public List<SpringConstructorArgElement> getCustructorArgs() {
        return constructorargs;
    }

    public void addCustructorArgs(SpringConstructorArgElement args) {
        constructorargs.add(args);
    }

    public boolean isInnerBean() {
        return innerBean;
    }

    public void setInnerBean(boolean innerBean) {
        this.innerBean = innerBean;
    }

    public boolean isAbstractBean() {
        return abstractBean;
    }

    public void setAbstractBean(boolean abstractBean) {
        this.abstractBean = abstractBean;
    }

    public boolean hasParentAttribute() {
        return parentAttribute;
    }

    public void setParentAttribute(boolean parentAttribute) {
        this.parentAttribute = parentAttribute;
    }

    public boolean hasFactoryBeanAttribute() {
        return factoryBeanAttribute;
    }

    public void setFactoryBeanAttribute(boolean factoryBeanAttribute) {
        this.factoryBeanAttribute = factoryBeanAttribute;
    }

    public boolean hasFactoryMethodAttribute() {
        return factoryMethodAttribute;
    }

    public void setFactoryMethodAttribute(boolean factoryMethodAttribute) {
        this.factoryMethodAttribute = factoryMethodAttribute;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpringBeanElement [id=").append(id).append(", className=").append(className)
            .append(", innerBean=").append(innerBean).append(", abstractBean=").append(abstractBean)
            .append(", parentAttribute=").append(parentAttribute).append(", factoryBeanAttribute=")
            .append(factoryBeanAttribute).append(", factoryMethodAttribute=").append(factoryMethodAttribute)
            .append(", properties=").append(properties).append(", constructorargs=").append(constructorargs)
            .append("]");
        return builder.toString();
    }

} // end class SpringBeanElement
