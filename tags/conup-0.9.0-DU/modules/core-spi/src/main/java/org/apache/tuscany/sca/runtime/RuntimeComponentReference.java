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

package org.apache.tuscany.sca.runtime;

import org.apache.tuscany.sca.assembly.ComponentReference;

/**
 * The runtime component reference. Provides the bridge between the 
 * assembly model representation of a component reference and its runtime 
 * realization
 * 
 * @version $Rev: 1057650 $ $Date: 2011-01-11 14:15:07 +0000 (Tue, 11 Jan 2011) $
 * @tuscany.spi.extension.asclient
 */
public interface RuntimeComponentReference extends ComponentReference {
    /**
     * Set the owning component
     * @param component
     */
    void setComponent(RuntimeComponent component);
    
    /**
     * Get the owning component
     * @return the owning component
     */
    RuntimeComponent getComponent();
}
