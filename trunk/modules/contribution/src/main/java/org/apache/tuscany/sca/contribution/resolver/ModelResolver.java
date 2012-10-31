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

package org.apache.tuscany.sca.contribution.resolver;

import org.apache.tuscany.sca.contribution.processor.ProcessorContext;

/**
 * A model resolver, responsible for resolving models in the scope of an
 * SCA contribution. 
 * 
 * SCA Assemblies reference artifacts of a wide variety of types. These
 * include:
 * <ul>
 * <li> Reference from one SCA composite to another SCA composite
 * <li> Reference to PolicySet files
 * <li> Reference to interface definition files, either WSDL or Java interfaces
 * <li> Reference to XSD files
 * <li> Reference to any of a wide variety of implementation artifact files,
 * including Java classes, BPEL scripts, C++ DLLs and classes, PHP scripts
 * </ul>
 * 
 * In the SCA assemblies, these various artifacts are referenced using either
 * QNames or logical URIs. Model resolvers are used to resolve these references
 * and get the in-memory models representing the referenced artifacts.
 * 
 * @version $Rev: 937321 $ $Date: 2010-04-23 16:01:54 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface ModelResolver {

    /**
     * Resolve the model representing an artifact.
     * 
     * @param modelClass the type of artifact
     * @param unresolved the unresolved model
     * @param context The context
     * @return the resolved model
     */
    <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context);
    
    /**
     * Add a resolved model.
     * 
     * @param resolved The model
     * @param context 
     */
    void addModel(Object resolved, ProcessorContext context);
    
    /**
     * Remove a resolved model.
     * 
     * @param resolved
     * @param context 
     * @return The removed model, or null if the model was not removed
     */
    Object removeModel(Object resolved, ProcessorContext context);
    
}
