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

/**
 * Base interface for all assembly model objects.
 *
 * @version $Rev: 937321 $ $Date: 2010-04-23 16:01:54 +0100 (Fri, 23 Apr 2010) $
 * @tuscany.spi.extension.inheritfrom
 */
public interface Base {
    String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    String SCA11_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";

    /**
     * Returns true if the model element is unresolved.
     *
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     *
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);

}
