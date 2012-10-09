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
package org.apache.tuscany.sca.assembly;

import java.util.List;

/**
 * Represents composite reference.
 * 
 * @version $Rev: 791550 $ $Date: 2009-07-06 18:39:44 +0100 (Mon, 06 Jul 2009) $
 */
public interface CompositeReference extends Reference {
    /**
     * Returns the promoted components. For each promoted component/reference, 
     * they have the same index in the component and reference list. 
     *  
     * @return the promoted components
     */
    List<Component> getPromotedComponents();

    /**
     * Returns the promoted component references.
     * 
     * @return the promoted component references
     */
    List<ComponentReference> getPromotedReferences();

}
