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

package org.apache.tuscany.sca.contribution.java.impl;

import java.util.List;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;

/**
 * A JavaImport specific model resolver. This model resolver is temporary
 * and provides the ContributionClassLoader with the list of exporting
 * contributions that it currently needs.
 * 
 * FIXME Remove this class after the ContributionClassLoader is simplified
 * and cleaned up.
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class JavaImportModelResolver implements ModelResolver {

    private ModelResolver modelResolver;
    private List<Contribution> contributions;
    
    public JavaImportModelResolver(List<Contribution> contributions, ModelResolver modelResolver) {
        this.modelResolver = modelResolver;
        this.contributions = contributions;
    }

    public List<Contribution> getExportContributions() {
        return contributions;
    }
    
    public void addModel(Object resolved, ProcessorContext context) {
        modelResolver.addModel(resolved, context);
    }

    public Object removeModel(Object resolved, ProcessorContext context) {
        return modelResolver.removeModel(resolved, context);
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        return modelResolver.resolveModel(modelClass, unresolved, context);
    }
}
