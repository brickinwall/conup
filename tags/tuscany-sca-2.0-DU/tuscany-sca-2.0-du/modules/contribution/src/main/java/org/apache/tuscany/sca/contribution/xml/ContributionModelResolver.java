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

package org.apache.tuscany.sca.contribution.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;

/**
 * A Model Resolver for Contribution models.
 *
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class ContributionModelResolver implements ModelResolver {

    private Map<String, Contribution> map = new HashMap<String, Contribution>();
    
    public ContributionModelResolver(Contribution contribution, FactoryExtensionPoint modelFactories) {
    }

    public void addModel(Object resolved, ProcessorContext context) {
        Contribution contribution = (Contribution)resolved;
        map.put(contribution.getURI(), contribution);
    }
    
    public Object removeModel(Object resolved, ProcessorContext context) {
        return map.remove(((Contribution)resolved).getURI());
    }
    
    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        
        // Lookup a contribution for the given URI
        String uri = ((Contribution)unresolved).getURI();
        if (uri != null) {
            Contribution resolved = (Contribution) map.get(uri);
            if (resolved != null) {
                return modelClass.cast(resolved);
            }
            return unresolved;
        } else {
            
            // If no URI was specified, just return the first contribution
            if (!map.isEmpty()) {
                Contribution resolved = map.values().iterator().next();
                return modelClass.cast(resolved);
            } else {
                return unresolved;
            }
        }
    }
    
}
