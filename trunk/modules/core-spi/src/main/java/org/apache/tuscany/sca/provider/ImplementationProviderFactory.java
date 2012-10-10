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

package org.apache.tuscany.sca.provider;

import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * An interface for factories that create implementation providers.  
 * 
 * @version $Rev: 937310 $ $Date: 2010-04-23 15:27:50 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface ImplementationProviderFactory<M extends Implementation> extends ProviderFactory<M> {
    
    /**
     * Creates a new implementation provider for the given
     * component.
     * 
     * @param component The runtime component
     * @param Implementation The implementation type
     * @return The implementation provider
     */
    ImplementationProvider createImplementationProvider(RuntimeComponent component, M Implementation);
    
}
