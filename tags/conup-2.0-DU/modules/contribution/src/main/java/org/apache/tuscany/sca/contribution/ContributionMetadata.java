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

package org.apache.tuscany.sca.contribution;

import java.util.List;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Extensible;

/**
 * The representation of SCA contribution metadata.
 *
 * @version $Rev: 892324 $ $Date: 2009-12-18 17:55:54 +0000 (Fri, 18 Dec 2009) $
 */
public interface ContributionMetadata extends Base, Extensible {
    
    /**
     * Returns the spec version of the contribution metadata used to build this model
     *
     * @return the spec version used to build this model
     */
    String getSpecVersion();

    /**
     * Sets the spec version of the contribution metadata used to build this model
     *
     * @param specVersion the spec version used to build this model
     */
    void setSpecVersion(String specVersion);

    /**
     * Returns a list of exports based on the contribution metadata.
     *
     * @return The list of exports
     */
    List<Export> getExports();

    /**
     * Returns a list of imports based on the contribution metadata.
     *
     * @return The list of imports
     */
    List<Import> getImports();

    /**
     * Returns the list of deployable based on the contribution metadata.
     *
     * @return The list of deployable composites
     */
    List<Composite> getDeployables();

}