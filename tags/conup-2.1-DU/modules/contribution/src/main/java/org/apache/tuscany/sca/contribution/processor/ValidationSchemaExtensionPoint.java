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

import java.util.List;


/**
 * An extension point for XML schemas used for validation.
 *
 * @version $Rev: 758911 $ $Date: 2009-03-26 22:52:27 +0000 (Thu, 26 Mar 2009) $
 */
public interface ValidationSchemaExtensionPoint {
    /**
     * Set the flag to control if schema validation should be enabled
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Test the schema validation is enabled
     * @return
     */
    boolean isEnabled();

    /**
     * Add a schema.
     *
     * @param uri the URI of the schema
     */
    void addSchema(String uri);

    /**
     * Remove a schema.
     *
     * @param uri the URI of the schema
     */
    void removeSchema(String uri);

    /**
     * Returns the list of schemas registered in the extension point.
     * @return the list of schemas
     */
    List<String> getSchemas();

}
