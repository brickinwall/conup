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

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Implementation;

/**
 * Represents a Java implementation.
 * 
 * @version $Rev: 983054 $ $Date: 2010-08-06 18:01:52 +0100 (Fri, 06 Aug 2010) $
 */
public interface BaseJavaImplementation extends Implementation, Extensible {

    /**
     * Returns the name of the Java implementation class.
     * 
     * @return the name of the Java implementation class
     */
    String getName();

    /**
     * Sets the name of the Java implementation class.
     * 
     * @param className the name of the Java implementation class
     */
    void setName(String className);

    /**
     * Returns the Java implementation class.
     * 
     * @return the Java implementation class
     */
    Class<?> getJavaClass();

    /**
     * Sets the Java implementation class.
     * 
     * @param javaClass the Java implementation class
     */
    void setJavaClass(Class<?> javaClass);
    
    /**
     * Customize the implementation type so that components are implemented using Java based framework such as
     * implementation.spring or implementation.jaxrs can leverage the introspection
     * @param type
     */
    void setType(QName type);

}
