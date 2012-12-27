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

import java.util.List;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;

/**
 * A model resolver implementation that considers Exports in a list of contributions.
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class DefaultImportAllModelResolver implements ModelResolver {
    
    private Import import_;
    private List<Contribution> contributions;
    
    public DefaultImportAllModelResolver(Import import_, List<Contribution> contributions) {
        this.import_ = import_;
        this.contributions = contributions;
    }

    public void addModel(Object resolved, ProcessorContext context) {
        throw new IllegalStateException();
    }

    public Object removeModel(Object resolved, ProcessorContext context) {
        throw new IllegalStateException();
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        
        //TODO optimize and cache results of the resolution later
        
        // Go over all available contributions
        for (Contribution contribution : contributions) {
            
            // Go over all exports in the contribution
            for (Export export : contribution.getExports()) {
                
                    // If the export matches the export, try to resolve the model object
                    if (import_.match(export)) {
                        Object resolved = export.getModelResolver().resolveModel(modelClass, unresolved, context);
                        
                        // Return the resolved model object
                        if (resolved instanceof Base) {
                            if (!((Base)resolved).isUnresolved()) {
                                return modelClass.cast(resolved);
                            }
                        }
                    }
            }
        }

        // Model object was not resolved
        return unresolved;
    }

}
