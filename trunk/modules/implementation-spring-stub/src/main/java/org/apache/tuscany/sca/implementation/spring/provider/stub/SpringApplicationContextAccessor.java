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

package org.apache.tuscany.sca.implementation.spring.provider.stub;

/**
 * A utility to receive the parent Spring application context 
 */
public interface SpringApplicationContextAccessor {
    /**
     * Get the parent Spring application context for the hosting environment. This will be used as the parent 
     * application context for implementation.spring components
     * @return The parent application context
     */
    Object getParentApplicationContext();

    /**
     * Set the root Spring application context. This is particually useful for Spring web integration where Spring
     * creates WebApplicationContext and keeps it in the ServletContext
     * @param parentApplicationContext The parent application context
     */
    void setParentApplicationContext(Object parentApplicationContext);
}
