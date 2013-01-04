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
package org.apache.tuscany.sca.contribution.processor;

import org.apache.tuscany.sca.contribution.resolver.ModelResolver;

/**
 * Base interface for artifact processors.
 * 
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public interface ArtifactProcessor<M> {
    
    /**
     * Resolve references from this model to other models. For example references
     * from a composite to another one, or references from a composite to a WSDL
     * model.
     * 
     * @param model The model to resolve
     * @param resolver The resolver to use to resolve referenced models
     * @param context The context for the processor
     */
    void resolve(M model, ModelResolver resolver, ProcessorContext context) throws ContributionResolveException;
    
    /**
     * Returns the type of model handled by this artifact processor.
     * 
     * @return The type of model handled by this artifact processor
     */
    Class<M> getModelType(); 
    
}
