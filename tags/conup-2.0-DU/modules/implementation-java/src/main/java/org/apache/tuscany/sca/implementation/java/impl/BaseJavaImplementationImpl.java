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
package org.apache.tuscany.sca.implementation.java.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.implementation.java.BaseJavaImplementation;

/**
 * Represents a Java implementation.
 *
 * @version $Rev: 1136391 $ $Date: 2011-06-16 13:13:44 +0100 (Thu, 16 Jun 2011) $
 */
abstract class BaseJavaImplementationImpl extends ImplementationImpl implements BaseJavaImplementation {

    private String className;
    private Class<?> javaClass;

    protected BaseJavaImplementationImpl(QName type) {
        super(type);
    }

    public String getName() {
        if (isUnresolved()) {
            return className;
        } else if (javaClass != null) {
            return javaClass.getName();
        } else {
            return null;
        }
    }

    public void setName(String className) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.className = className;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class<?> javaClass) {
        this.javaClass = javaClass;
        if (this.className == null) {
            this.className = javaClass.getName();
        }
    }

    @Override
    public String toString() {
        return getType() + " (class=" + getName() + ")";
    }

/* TUSCANY-3876 - disable implementation model sharing so that 
 *                we can get implementation policy modelled on an
 *                impementation by implementation basis rather than 
 *                storing it on the component
    @Override
    public int hashCode() {
        return String.valueOf(getName()).hashCode();
    }
*/    

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BaseJavaImplementation) {
            if (getName() != null) {
                return getName().equals(((BaseJavaImplementation)obj).getName());
            } else {
                return ((BaseJavaImplementation)obj).getName() == null;
            }
        } else {
            return false;
        }
    }

    @Override
    public void setType(QName type) {
       this.type = type;
    }
}
