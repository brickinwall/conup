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

package org.apache.tuscany.sca.databinding.sdo;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;


/**
 * A default implementation of an artifact resolver, based on a map.
 *
 * @version $Rev: 909092 $ $Date: 2010-02-11 17:44:00 +0000 (Thu, 11 Feb 2010) $
 */
public class TestModelResolver implements ModelResolver {
    private static final long serialVersionUID = -7826976465762296634L;
    
    private Map<Object, Object> map = new HashMap<Object, Object>();
    
    public TestModelResolver() {
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved, ProcessorContext context) {
        Object resolved = map.get(unresolved);
        if (resolved != null) {
            
            // Return the resolved object
            return modelClass.cast(resolved);
            
        } else {
            
            // Return the unresolved object
            return unresolved;
        }
    }
    
    public void addModel(Object resolved, ProcessorContext context) {
        map.put(resolved, resolved);
    }
    
    public Object removeModel(Object resolved, ProcessorContext context) {
        return map.remove(resolved);
    }
    
}
