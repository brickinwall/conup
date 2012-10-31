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

import java.util.List;

import org.apache.tuscany.sca.implementation.java.introspect.JavaClassVisitor;


/**
 * Factory for the Java model
 * 
 * @version $Rev: 564429 $ $Date: 2007-08-10 00:49:11 +0100 (Fri, 10 Aug 2007) $
 */
public interface JavaImplementationFactory {

    /**
     * Creates a new Java implementation model.
     * 
     * @return
     */
    JavaImplementation createJavaImplementation();
    
    /**
     * Creates a new Java implementation model from an implementation class.
     * 
     * @param implementationClass The implementation class to introspect.
     * @return
     */
    JavaImplementation createJavaImplementation(Class<?> implementationClass) throws IntrospectionException;

    /**
     * Creates the contents of a Java implementation model from an implementation class.
     * 
     * @param implementationClass The implementation class to introspect.
     * @return
     */
    void createJavaImplementation(JavaImplementation javaImplementation, Class<?> implementationClass) throws IntrospectionException;

    /**
     * Registers the given visitor.
     * 
     * @param visitor
     */
    void addClassVisitor(JavaClassVisitor visitor);

    /**
     * Deregisters the given visitor.
     */
    void removeClassVisitor(JavaClassVisitor visitor);
    
    /**
     * Returns the list of visitors.
     * 
     * @return
     */
    List<JavaClassVisitor> getClassVisitors();

}
